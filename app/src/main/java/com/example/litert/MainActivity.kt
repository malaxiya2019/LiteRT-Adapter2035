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

// --- 核心模型：确保这里没有重复定义 ---
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
            text = "LiteRT Adapter 2026\nStatus: Running on Port 8080\nDevice: nubia Flip"
            textSize = 16f
            setPadding(48, 48, 48, 48)
        }
        setContentView(textView)

        // 初始化推理引擎
        llmRunner = LlmRunner(this)
        llmRunner.init()

        // 启动 API
        startServer()
    }

    private fun startServer() {
        // 使用标准的 Ktor 2.x 结构
        server = embeddedServer(Netty, port = 8080) {
            install(ContentNegotiation) {
                json(Json { 
                    ignoreUnknownKeys = true 
                })
            }
            routing {
                post("/v1/chat/completions") {
                    try {
                        val request = call.receive<ChatRequest>()
                        val prompt = request.messages.lastOrNull()?.content ?: ""
                        
                        // 执行同步推理
                        val answer = llmRunner.generateSync(prompt)
                        
                        val response = ChatResponse(
                            choices = listOf(Choice(Message("assistant", answer)))
                        )
                        call.respond(response)
                    } catch (e: Exception) {
                        call.respondText("Infer Error: \${e.message}")
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
