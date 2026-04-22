package com.example.litert

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import java.io.File

class LlmRunner(private val context: Context) {

    private var llmInference: LlmInference? = null

    fun init() {
        val modelPath = "/sdcard/Download/gemma-4-E2B-it.litertlm"
        val file = File(modelPath)

        if (!file.exists()) {
            android.util.Log.e("LlmRunner", "模型文件不存在: \$modelPath")
            return
        }

        try {
            // 兼容性最强的配置方式：使用 .setModelFilePath 直接指向本地路径
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelFilePath(modelPath) // 如果编译还是不通过，请尝试改为 .setModelAssetPath
                .setMaxTokens(1024)
                .setTemperature(0.7f)
                .setTopK(40)
                .build()

            llmInference = LlmInference.createFromOptions(context, options)
            android.util.Log.i("LlmRunner", "Gemma 模型加载成功")
        } catch (e: Exception) {
            android.util.Log.e("LlmRunner", "加载失败: \${e.message}")
        }
    }

    fun generateSync(prompt: String): String {
        val formattedPrompt = "<start_of_turn>user\n\$prompt<end_of_turn>\n<start_of_turn>model\n"
        return try {
            llmInference?.generateResponse(formattedPrompt) ?: "模型未初始化"
        } catch (e: Exception) {
            "推理出错: \${e.message}"
        }
    }

    fun close() {
        llmInference?.close()
        llmInference = null
    }
}
