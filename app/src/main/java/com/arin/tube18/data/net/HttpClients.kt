package com.arin.tube18.data.net

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import java.io.InterruptedIOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object HttpClients {
    const val MOBILE_UA =
        "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"
    const val DESKTOP_UA =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"

    fun createPageClient(): OkHttpClient = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .retryOnConnectionFailure(true)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .cookieJar(AgeCookieJar())
        .addInterceptor(BrowserHeadersInterceptor())
        .addInterceptor(RetryInterceptor(maxRetries = 2))
        .build()

    fun createImageClient(base: OkHttpClient): OkHttpClient = base.newBuilder()
        .apply { interceptors().clear() }
        .addInterceptor(Interceptor { chain ->
            val req = chain.request().newBuilder()
                .header("User-Agent", MOBILE_UA)
                .header("Referer", "https://www.pornhub.com/")
                .build()
            chain.proceed(req)
        })
        .addInterceptor(RetryInterceptor(maxRetries = 1))
        .build()
}

/** Sends age_verified=1 and accessAgeDisclaimerPH=1 to the site, plus any cookies the site sets. */
class AgeCookieJar : CookieJar {
    private val store = ConcurrentHashMap<String, Cookie>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookies.forEach { c -> store["${c.domain}|${c.path}|${c.name}"] = c }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        val result = LinkedHashMap<String, Cookie>()
        store.values.forEach { c ->
            if (c.expiresAt > now && c.matches(url)) result[c.name] = c
        }
        if (url.host == "pornhub.com" || url.host.endsWith(".pornhub.com")) {
            AGE_COOKIES.forEach { (name, value) ->
                result[name] = Cookie.Builder()
                    .name(name)
                    .value(value)
                    .domain("pornhub.com")
                    .path("/")
                    .build()
            }
        }
        return result.values.toList()
    }

    companion object {
        val AGE_COOKIES = listOf("age_verified" to "1", "accessAgeDisclaimerPH" to "1")
    }
}

class BrowserHeadersInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val b = original.newBuilder()
        if (original.header("User-Agent") == null) b.header("User-Agent", HttpClients.MOBILE_UA)
        if (original.header("Accept") == null) {
            b.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
        }
        if (original.header("Accept-Language") == null) b.header("Accept-Language", "en-US,en;q=0.9")
        return chain.proceed(b.build())
    }
}

class RetryInterceptor(private val maxRetries: Int) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var attempt = 0
        var lastError: IOException? = null
        while (attempt <= maxRetries) {
            try {
                val response = chain.proceed(request)
                val retryable = response.code >= 500 || response.code == 429 || response.code == 408
                if (retryable && attempt < maxRetries) {
                    response.close()
                    backoff(attempt)
                    attempt++
                    continue
                }
                return response
            } catch (e: IOException) {
                if (chain.call().isCanceled()) throw e
                lastError = e
                if (attempt >= maxRetries) throw e
                backoff(attempt)
                attempt++
            }
        }
        throw lastError ?: IOException("Request failed")
    }

    private fun backoff(attempt: Int) {
        try {
            Thread.sleep(700L * (attempt + 1))
        } catch (e: InterruptedException) {
            throw InterruptedIOException("Interrupted while retrying")
        }
    }
}
