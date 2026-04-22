package com.example.litert

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.mediapipe.tasks.genai.llm.LlmInference
import com.google.mediapipe.tasks.genai.llm.LlmInferenceOptions
import kotlinx.coroutines.*
import java.io.File

class LlmRunner(private val context: Context) {

    private var llm: LlmInference? = null

    companion object {
        private const val TAG = "LlmRunner"

        // 👉 你的模型路径（重点）
        private const val MODEL_PATH = "/sdcard/Download/model.litertlm"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // =========================
    // ✅ 初始化
    // =========================
    suspend fun init() = withContext(Dispatchers.IO) {
        try {
            val file = File(MODEL_PATH)

            if (!file.exists()) {
                Log.e(TAG, "模型不存在: $MODEL_PATH")
                return@withContext
            }

            Log.d(TAG, "模型路径: ${file.absolutePath}")
            Log.d(TAG, "模型大小: ${file.length() / (1024 * 1024)} MB")

            val options = LlmInferenceOptions.builder()
                .setModelPath(file.absolutePath)
                .setMaxTokens(512)
                .build()

            llm = LlmInference.createFromOptions(context, options)

            Log.d(TAG, "LLM 初始化成功")

        } catch (e: Exception) {
            Log.e(TAG, "LLM 初始化失败", e)
        }
    }

    // =========================
    // ✅ 普通生成
    // =========================
    suspend fun generate(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            val instance = llm ?: return@withContext "模型未初始化"
            instance.generateResponse(prompt)
        } catch (e: Exception) {
            Log.e(TAG, "生成
