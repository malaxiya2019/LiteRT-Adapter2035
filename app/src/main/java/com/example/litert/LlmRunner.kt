package com.example.litert

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import java.io.File

class LlmRunner(private val context: Context) {

    private var llmInference: LlmInference? = null

    fun init() {
        val modelPath = "/sdcard/Download/gemma-4-E2B-it.litertlm"
        val file = File(modelPath)

        if (!file.exists()) {
            Log.e("LlmRunner", "模型文件不存在: \$modelPath")
            return
        }

        try {
            // 使用适配 0.10.x 的 BaseOptions 写法
            val options = LlmInference.LlmInferenceOptions.builder()
                .setBaseOptions(
                    com.google.mediapipe.tasks.core.BaseOptions.builder()
                        .setModelFilePath(modelPath)
                        .build()
                )
                .setMaxTokens(1024)
                .setTemperature(0.7f)
                .setTopK(40)
                .build()

            llmInference = LlmInference.createFromOptions(context, options)
            Log.i("LlmRunner", "模型加载成功")
        } catch (e: Exception) {
            Log.e("LlmRunner", "初始化失败: \${e.message}")
        }
    }

    fun generateSync(prompt: String): String {
        val formattedPrompt = "<start_of_turn>user\n\$prompt<end_of_turn>\n<start_of_turn>model\n"
        return try {
            llmInference?.generateResponse(formattedPrompt) ?: "模型未就绪"
        } catch (e: Exception) {
            "推理错误: \${e.message}"
        }
    }

    fun close() {
        llmInference?.close()
        llmInference = null
    }
}
