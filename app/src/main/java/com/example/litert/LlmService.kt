package com.example.litert

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

class LlmService(private val runner: LlmRunner) {

    fun startServer(port: Int = 8080) {
        embeddedServer(Netty, port = port) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            
            routing {
                // OpenClaw 调用的标准路径
                post("/v1/chat/completions") {
                    val request = call.receive<ChatRequest>()
                    
                    // 提取最后一条用户消息
                    val userPrompt = request.messages.lastOrNull()?.content ?: ""
                    
                    // 调用 Gemma 4 进行推理 (这里暂时使用同步生成，后续可优化为流式)
                    val aiResult = runner.generateSync(userPrompt) 
                    
                    val response = ChatResponse(
                        model = request.model,
                        choices = listOf(
                            ChatChoice(message = ChatMessage(role = "assistant", content = aiResult))
                        )
                    )
                    call.respond(response)
                }
            }
        }.start(wait = false)
    }
}
