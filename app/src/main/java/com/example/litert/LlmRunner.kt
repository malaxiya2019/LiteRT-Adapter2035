package com.example.litert

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import java.io.File

class LlmRunner(private val context: Context) {

    private var llmInference: LlmInference? = null

    fun init() {
        // 模型文件必须放在此路径
        val modelPath = "/sdcard/Download/gemma-4-E2B-it.litertlm"
        val file = File(modelPath)

        if (!file.exists()) {
            android.util.Log.e("LlmRunner", "模型文件不存在: $modelPath")
            return
        }

        try {
            // 使用 MediaPipe 官方 API 结构
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelFilePath(modelPath)
                .setMaxTokens(1024)
                .setTemperature(0.7f)
                .setTopK(40)
                .build()

            llmInference = LlmInference.createFromOptions(context, options)
            android.util.Log.i("LlmRunner", "Gemma 4 加载成功")
        } catch (e: Exception) {
            android.util.Log.e("LlmRunner", "初始化失败: ${e.message}")
        }
    }

    fun generateSync(prompt: String): String {
        val formattedPrompt = "<start_of_turn>user\n$prompt<end_of_turn>\n<start_of_turn>model\n"
        return try {
            if (llmInference == null) return "模型未就绪"
            // MediaPipe 的同步调用方法为 generateResponse
            llmInference?.generateResponse(formattedPrompt) ?: "结果为空"
        } catch (e: Exception) {
            "推理错误: ${e.message}"
        }
    }
}
