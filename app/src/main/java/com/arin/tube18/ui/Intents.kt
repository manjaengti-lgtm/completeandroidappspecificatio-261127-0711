package com.arin.tube18.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

fun openUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No browser found to open the link", Toast.LENGTH_SHORT).show()
    } catch (e: SecurityException) {
        Toast.makeText(context, "Couldn't open the link", Toast.LENGTH_SHORT).show()
    }
}

fun shareUrl(context: Context, title: String, url: String) {
    try {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, url)
        }
        val chooser = Intent.createChooser(send, "Share video").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No app available to share with", Toast.LENGTH_SHORT).show()
    }
}
