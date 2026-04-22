package com.example.litert

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var llmRunner: LlmRunner
    private var server: NettyApplicationEngine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val textView = TextView(this).apply {
            text = "LiteRT Adapter 正在运行\n端口: 8080"
            textSize = 16f
            setPadding(48, 48, 48, 48)
        }
        setContentView(textView)

        // 关键：在这里初始化 LlmRunner
        llmRunner = LlmRunner(this)
        llmRunner.init()

        startServer()
    }

    private fun startServer() {
        server = embeddedServer(Netty, port = 8080) {
            routing {
                post("/v1/chat/completions") {
                    try {
                        val body = call.receiveText()
                        val prompt = if (body.contains("\"content\":\"")) {
                            body.substringAfter("\"content\":\"").substringBefore("\"")
                        } else "Hello"
                        
                        val answer = llmRunner.generateSync(prompt)
                        
                        val jsonResponse = """
                            {
                              "choices": [{
                                "message": { "role": "assistant", "content": "$answer" }
                              }]
                            }
                        """.trimIndent()
                        
                        call.respondText(jsonResponse, io.ktor.http.ContentType.Application.Json)
                    } catch (e: Exception) {
                        call.respondText("Error: \${e.message}")
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
