package com.example.litert

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import java.io.File

class LlmRunner(private val context: Context) {

    private var llmInference: LlmInference? = null

    /**
     * 初始化推理引擎，直接读取 SD 卡下载目录中的模型文件
     */
    fun init() {
        val modelPath = "/sdcard/Download/gemma-4-E2B-it.litertlm"
        val file = File(modelPath)

        if (!file.exists()) {
            android.util.Log.e("LlmRunner", "找不到模型文件: \$modelPath")
            return
        }

        try {
            // MediaPipe 0.10.11 的标准配置方式
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelFilePath(modelPath)
                .setMaxTokens(1024)
                .setTemperature(0.7f)
                .setTopK(40)
                .build()

            llmInference = LlmInference.createFromOptions(context, options)
            android.util.Log.i("LlmRunner", "Gemma 4 模型加载成功！")
            
        } catch (e: Exception) {
            android.util.Log.e("LlmRunner", "模型初始化失败: \${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 同步生成方法：适配 OpenClaw
     */
    fun generateSync(prompt: String): String {
        // Gemma 2/4 标准的 Chat 模板
        val formattedPrompt = "<start_of_turn>user\n\$prompt<end_of_turn>\n<start_of_turn>model\n"
        
        return try {
            val inference = llmInference
            if (inference == null) {
                "错误：模型尚未初始化，请检查路径及所有文件访问权限。"
            } else {
                // 注意：在 0.10.11 版本中，同步方法是 generateResponse
                inference.generateResponse(formattedPrompt) ?: "生成结果为空"
            }
        } catch (e: Exception) {
            "推理过程中出错: \${e.message}"
        }
    }

    /**
     * 释放资源
     */
    fun close() {
        llmInference?.close()
        llmInference = null
    }
}
