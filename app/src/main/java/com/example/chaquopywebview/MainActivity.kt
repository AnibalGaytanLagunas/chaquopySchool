package com.example.chaquopywebview

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.chaquo.python.Python
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val webView = findViewById<WebView>(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = WebViewClient()

        thread(name = "python-http-server") {
            try {
                val python = Python.getInstance()
                val server = python.getModule("server")
                val port = server.callAttr("start").toInt()
                runOnUiThread {
                    webView.loadUrl("http://127.0.0.1:$port/")
                }
            } catch (error: Exception) {
                val message = error.stackTraceToString()
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                runOnUiThread {
                    webView.loadDataWithBaseURL(
                        null,
                        """
                        <html>
                          <body style="font-family:sans-serif;padding:24px;background:#111;color:#eee">
                            <h1>No se pudo arrancar el servidor</h1>
                            <pre>$message</pre>
                          </body>
                        </html>
                        """.trimIndent(),
                        "text/html",
                        "utf-8",
                        null,
                    )
                }
            }
        }
    }
}
