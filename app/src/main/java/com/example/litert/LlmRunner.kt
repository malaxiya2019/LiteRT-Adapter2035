    fun init() {
        val modelPath = "/sdcard/Download/gemma-4-E2B-it.litertlm"
        val file = File(modelPath)

        if (!file.exists()) {
            android.util.Log.e("LlmRunner", "找不到模型文件")
            return
        }

        try {
            // --- 核心修复：改用 BaseOptions 设置路径 ---
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelFilePath(modelPath) // 如果直接设置报错，请尝试下方替换方案
                .setMaxTokens(1024)
                .setTemperature(0.7f)
                .setTopK(40)
                .build()
            
            // 如果上述依然报 setModelFilePath 错误，请彻底替换为下面这几行：
            /*
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
            */

            llmInference = LlmInference.createFromOptions(context, options)
            android.util.Log.i("LlmRunner", "加载成功")
        } catch (e: Exception) {
            android.util.Log.e("LlmRunner", "初始化失败: \${e.message}")
        }
    }
