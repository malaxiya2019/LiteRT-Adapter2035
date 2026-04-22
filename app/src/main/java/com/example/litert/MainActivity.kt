package com.example.litert

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// --- 数据模型定义 (只在这里定义一次) ---
@Serializable
data class ChatRequest(val model: String? = null, val messages: List<Message>)

@Serializable
data class Message(val role: String, val content: String)

@Serializable
data class ChatResponse(val choices: List<Choice>)

@Serializable
data class Choice(val message: Message)

class MainActivity : AppCompatActivity() {

    private lateinit var llmRunner: LlmRunner
    private var server: NettyApplicationEngine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val textView = TextView(this).apply {
            text = "LiteRT Adapter 运行中...\n端口: 8080\n模型: Gemma 4"
            textSize = 18f
            setPadding(40, 40, 40, 40)
        }
        setContentView(textView)

        // 1. 初始化本地推理引擎
        llmRunner = LlmRunner(this)
        llmRunner.init()

        // 2. 启动 API 服务
        startServer()
    }

    private fun startServer() {
        server = embeddedServer(Netty, port = 8080) {
            install(ContentNegotiation) {
                json(Json { 
                    ignoreUnknownKeys = true 
                    prettyPrint = true
                })
            }
            routing {
                post("/v1/chat/completions") {
                    try {
                        val request = call.receive<ChatRequest>()
                        val prompt = request.messages.lastOrNull()?.content ?: ""
                        
                        // 执行推理
                        val answer = llmRunner.generateSync(prompt)
                        
                        val response = ChatResponse(
                            choices = listOf(Choice(Message("assistant", answer)))
                        )
                        call.respond(response)
                    } catch (e: Exception) {
                        call.respondText("Error: ${e.message}")
                    }
                }
            }
        }.start(wait = false)
    }

    override fun onDestroy() {
        super.onDestroy()
        server?.stop(1000, 2000)
        llmRunner.close()
    }
}
