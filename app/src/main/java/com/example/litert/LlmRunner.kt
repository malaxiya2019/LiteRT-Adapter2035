package com.example.litert

import android.content.Context
import com.google.ai.edge.litert.flavors.genai.LlmInference
import java.io.File

class LlmRunner(private val context: Context) {

    private var llmInference: LlmInference? = null

    /**
     * 初始化推理引擎，直接读取 SD 卡下载目录中的模型文件
     */
    fun init() {
        // 1. 定义模型在手机存储中的绝对路径
        val modelPath = "/sdcard/Download/gemma-4-E2B-it.litertlm"
        val file = File(modelPath)

        // 2. 物理检查文件是否存在
        if (!file.exists()) {
            android.util.Log.e("LlmRunner", "找不到模型文件: $modelPath")
            return
        }

        try {
            // 3. 配置推理选项
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelFilePath(modelPath) // 关键：指向外部存储路径
                .setMaxTokens(1024)          // 最大生成长度
                .setTopK(40)                 // 采样参数
                .setTemperature(0.7f)        // 创造性参数
                .build()

            // 4. 创建推理实例
            llmInference = LlmInference.createFromOptions(context, options)
            android.util.Log.i("LlmRunner", "Gemma 4 模型加载成功！")
            
        } catch (e: Exception) {
            android.util.Log.e("LlmRunner", "模型初始化失败: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 同步生成方法：给 LlmService 调用，适配 OpenClaw
     */
    fun generateSync(prompt: String): String {
        // 转换成 Gemma 2/4 标准的 Chat 模板
        val formattedPrompt = "<start_of_turn>user\n$prompt<end_of_turn>\n<start_of_turn>model\n"
        
        return try {
            if (llmInference == null) {
                return "错误：模型尚未初始化，请检查 /sdcard/Download 路径下的文件。"
            }
            // 执行推理
            llmInference?.generateResponse(formattedPrompt) ?: "生成结果为空"
        } catch (e: Exception) {
            "推理过程中出错: ${e.message}"
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


