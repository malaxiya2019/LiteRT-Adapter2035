package com.example.litert

import kotlinx.serialization.Serializable

// 请求体
@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val stream: Boolean = false
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

// 响应体
@Serializable
data class ChatResponse(
    val id: String = "chatcmpl-${System.currentTimeMillis()}",
    val `object`: String = "chat.completion",
    val created: Long = System.currentTimeMillis() / 1000,
    val model: String,
    val choices: List<ChatChoice>
)

@Serializable
data class ChatChoice(
    val index: Int = 0,
    val message: ChatMessage,
    val finish_reason: String = "stop"
)
