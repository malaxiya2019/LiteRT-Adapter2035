// 在 LlmRunner.kt 类中
fun generateSync(prompt: String): String {
    // Gemma 官方 Prompt 模板
    val formattedPrompt = "<start_of_turn>user\n$prompt<end_of_turn>\n<start_of_turn>model\n"
    
    // 调用 LiteRT 原生同步接口
    return try {
        llmInference?.generateResponse(formattedPrompt) ?: "Model not ready"
    } catch (e: Exception) {
        "Inference failed: ${e.message}"
    }
}

