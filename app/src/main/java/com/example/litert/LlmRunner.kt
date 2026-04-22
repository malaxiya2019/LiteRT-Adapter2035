package com.example.litert

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import java.io.File

class LlmRunner(private val context: Context) {

    private var llmInference: LlmInference? = null

    fun init() {
        // 手机上的物理路径
        val modelPath = "/sdcard/Download/gemma-4-E2B-it.litertlm"
        val file = File(modelPath)

        if (!file.exists()) {
            android.util.Log.e("LlmRunner", "模型文件缺失，请检查: $modelPath")
            return
        }

        try {
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelFilePath(modelPath)
                .setMaxTokens(1024)
                .setTemperature(0.7f)
                .setTopK(40)
                .build()

            llmInference = LlmInference.createFromOptions(context, options)
            android.util.Log.i("LlmRunner", "Gemma 4 初始化成功！")
        } catch (e: Exception) {
            android.util.Log.e("LlmRunner", "初始化异常: ${e.message}")
        }
    }

    fun generateSync(prompt: String): String {
        // Gemma 模板
        val formattedPrompt = "<start_of_turn>user\n$prompt<end_of_turn>\n<start_of_turn>model\n"
        
        return try {
            if (llmInference == null) return "模型初始化失败"
            llmInference?.generateResponse(formattedPrompt) ?: "未生成内容"
        } catch (e: Exception) {
            "推理出错: ${e.message}"
        }
    }
}
