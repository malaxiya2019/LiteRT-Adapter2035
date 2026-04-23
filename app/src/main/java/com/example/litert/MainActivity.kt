package com.example.litert

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var llmRunner: LlmRunner
    private var server: NettyApplicationEngine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView = TextView(this).apply {
            text = "LiteRT Adapter 正在启动..."
            textSize = 16f
            setPadding(48, 48, 48, 48)
        }
        setContentView(textView)

        llmRunner = LlmRunner(this)

        // ✅ 正确初始化（协程）
        lifecycleScope.launch {
            llmRunner.init { progress ->
                runOnUiThread {
                    textView.text = "模型加载中: $progress%"
                }
            }

            textView.text = "LiteRT Adapter 已运行\n端口: 8080"

            startServer()
        }
    }

    private fun startServer() {
        server = embeddedServer(Netty, port = 8080) {
            routing {

                post("/v1/chat/completions") {

                    val body = call.receiveText()

                    val prompt = if (body.contains("\"content\":\"")) {
                        body.substringAfter("\"content\":\"").substringBefore("\"")
                    } else "Hello"

                    try {
                        // ✅ 正确调用（同步接口）
                        val answer = llmRunner.generate(prompt)

                        val jsonResponse = """
                        {
                          "choices": [{
                            "message": {
                              "role": "assistant",
                              "content": "$answer"
                            }
                          }]
                        }
                        """.trimIndent()

                        call.respondText(jsonResponse)

                    } catch (e: Exception) {
                        call.respondText("Error: ${e.message}")
                    }
                }
            }
        }.start(wait = false)
    }

    override fun onDestroy() {
        super.onDestroy()
        server?.stop(500, 1000)
        llmRunner.close()
    }
}
