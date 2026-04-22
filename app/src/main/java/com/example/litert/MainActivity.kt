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

@Serializable
data class ChatRequest(val model: String, val messages: List<Message>)

@Serializable
data class Message(val role: String, val content: String)

@Serializable
data class ChatResponse(val choices: List<Choice>)

@Serializable
data class Choice(val message: Message)

class MainActivity : AppCompatActivity() {

    private lateinit var llmRunner: LlmRunner
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 创建一个简单的布局显示状态
        val textView = TextView(this)
        textView.text = "LiteRT Adapter 正在启动...\n端口: 8080"
        setContentView(textView)

        // 1. 初始化推理引擎
        llmRunner = LlmRunner(this)
        llmRunner.init()

        // 2. 启动 Ktor 本地服务
        startServer()
    }

    private fun startServer() {
        embeddedServer(Netty, port = 8080) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            routing {
                post("/v1/chat/completions") {
                    try {
                        val request = call.receive<ChatRequest>()
                        val userPrompt = request.messages.lastOrNull()?.content ?: ""
                        
                        // 调用刚才修好的推理方法
                        val result = llmRunner.generateSync(userPrompt)
                        
                        val response = ChatResponse(
                            choices = listOf(Choice(Message("assistant", result)))
                        )
                        call.respond(response)
                    } catch (e: Exception) {
                        call.respondText("Server Error: ${e.message}")
                    }
                }
            }
        }.start(wait = false)
    }

    override fun onDestroy() {
        super.onDestroy()
        llmRunner.close()
        scope.cancel()
    }
}
