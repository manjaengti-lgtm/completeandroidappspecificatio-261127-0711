package com.arin.tube18.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.arin.tube18.data.SiteUrls
import com.arin.tube18.data.scrape.ScrapeException
import com.arin.tube18.ui.components.AppTopBar
import com.arin.tube18.ui.components.ErrorState
import com.arin.tube18.ui.components.FullScreenMessage
import com.arin.tube18.ui.openUrl
import com.arin.tube18.viewmodel.PlayerViewModel

/** Puts the embed's full-screen video view over the whole window and back. */
class FullscreenController(private val activity: Activity?) {
    var customView by mutableStateOf<View?>(null)
        private set
    private var callback: WebChromeClient.CustomViewCallback? = null

    val isFullscreen: Boolean get() = customView != null

    fun enter(view: View, cb: WebChromeClient.CustomViewCallback) {
        val act = activity
        val decor = act?.window?.decorView as? FrameLayout
        if (act == null || decor == null || customView != null) {
            cb.onCustomViewHidden()
            return
        }
        view.setBackgroundColor(android.graphics.Color.BLACK)
        decor.addView(
            view,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
        customView = view
        callback = cb
        WindowCompat.getInsetsController(act.window, decor).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
        act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
    }

    fun exit() {
        val view = customView ?: return
        customView = null
        val act = activity
        (act?.window?.decorView as? FrameLayout)?.removeView(view)
        val cb = callback
        callback = null
        if (act != null) {
            WindowCompat.getInsetsController(act.window, act.window.decorView)
                .show(WindowInsetsCompat.Type.systemBars())
            act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        try {
            cb?.onCustomViewHidden()
        } catch (e: Exception) {
            // WebView already gone
        }
    }
}

@Composable
fun PlayerScreen(vm: PlayerViewModel, onBack: () -> Unit) {
    val activity = LocalActivity.current
    val context = LocalContext.current
    val embedUrl = remember(vm.videoId) { SiteUrls.embedUrl(vm.videoId) }
    var online by remember { mutableStateOf(vm.isOnline()) }
    var progress by remember { mutableIntStateOf(0) }
    var pageError by remember { mutableStateOf<String?>(null) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    val fullscreen = remember(activity) { FullscreenController(activity) }

    fun destroyWebView() {
        webView?.let { wv ->
            wv.stopLoading()
            (wv.parent as? ViewGroup)?.removeView(wv)
            wv.destroy()
        }
        webView = null
    }

    BackHandler(enabled = fullscreen.isFullscreen) { fullscreen.exit() }

    DisposableEffect(Unit) {
        onDispose {
            fullscreen.exit()
            destroyWebView()
        }
    }

    Column(Modifier.fillMaxSize().background(Color.Black)) {
        AppTopBar(title = vm.title, onBack = onBack)
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .navigationBarsPadding()
        ) {
            val landscape = maxWidth > maxHeight
            Column(Modifier.fillMaxSize()) {
                val playerModifier = if (landscape) {
                    Modifier.fillMaxWidth().weight(1f)
                } else {
                    Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                }
                Box(playerModifier.background(Color.Black)) {
                    if (!online) {
                        ErrorState(
                            ScrapeException.Offline(),
                            onRetry = {
                                pageError = null
                                progress = 0
                                online = vm.isOnline()
                            }
                        )
                    } else {
                        AndroidView(
                            factory = { ctx ->
                                buildWebView(
                                    ctx = ctx,
                                    fullscreen = fullscreen,
                                    onProgress = { progress = it },
                                    onError = { pageError = it }
                                ).also { wv ->
                                    webView = wv
                                    wv.loadUrl(embedUrl)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                        if (pageError == null && progress < 100) {
                            LinearProgressIndicator(
                                progress = { progress / 100f },
                                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        val err = pageError
                        if (err != null) {
                            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.92f))) {
                                FullScreenMessage(
                                    icon = Icons.Filled.ErrorOutline,
                                    title = "The player couldn't load",
                                    message = "$err. The video may have been removed or blocked in your region, or your connection dropped.",
                                    actionLabel = "Retry",
                                    onAction = {
                                        if (vm.isOnline()) {
                                            pageError = null
                                            progress = 0
                                            webView?.loadUrl(embedUrl)
                                        } else {
                                            pageError = null
                                            destroyWebView()
                                            online = false
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                if (!landscape) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(MaterialTheme.colorScheme.background)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text(vm.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Use the player's own controls to seek, change volume and quality. " +
                                "Tap its full-screen button for landscape. Back leaves full-screen first.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(onClick = { openUrl(context, SiteUrls.watchUrl(vm.videoId)) }) {
                            Icon(Icons.Filled.OpenInBrowser, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Open in browser")
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun buildWebView(
    ctx: Context,
    fullscreen: FullscreenController,
    onProgress: (Int) -> Unit,
    onError: (String) -> Unit
): WebView {
    val wv = WebView(ctx)
    wv.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    wv.setBackgroundColor(android.graphics.Color.BLACK)
    wv.settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        mediaPlaybackRequiresUserGesture = true
        loadWithOverviewMode = true
        useWideViewPort = true
        javaScriptCanOpenWindowsAutomatically = false
        setSupportMultipleWindows(false)
    }
    val cookies = CookieManager.getInstance()
    cookies.setAcceptCookie(true)
    cookies.setAcceptThirdPartyCookies(wv, true)
    cookies.setCookie(SiteUrls.BASE, "age_verified=1; path=/")
    cookies.setCookie(SiteUrls.BASE, "accessAgeDisclaimerPH=1; path=/")
    cookies.flush()

    wv.webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            if (!request.isForMainFrame) return false
            val url = request.url
            val scheme = url.scheme?.lowercase()
            if (scheme != "http" && scheme != "https") return true
            if (url.path?.contains("/embed/") == true) return false
            return if (request.hasGesture()) {
                openUrl(view.context, url.toString())
                true
            } else {
                false
            }
        }

        override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
            onProgress(5)
        }

        override fun onPageFinished(view: WebView, url: String?) {
            onProgress(100)
        }

        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
            if (request.isForMainFrame) onError("Load error: ${error.description}")
        }

        override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
            if (request.isForMainFrame && errorResponse.statusCode >= 400) {
                onError("The site answered with error ${errorResponse.statusCode}")
            }
        }
    }
    wv.webChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            onProgress(newProgress)
        }

        override fun onShowCustomView(view: View, callback: CustomViewCallback) {
            fullscreen.enter(view, callback)
        }

        override fun onHideCustomView() {
            fullscreen.exit()
        }
    }
    return wv
}
