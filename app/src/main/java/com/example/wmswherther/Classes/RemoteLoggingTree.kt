package com.example.wmswherther.Classes

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.HttpURLConnection
import java.net.URL

class RemoteLoggingTree(
    private val serverUrl: String,
    private val deviceId: String
) : Timber.Tree() {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Игнорируем слишком мелкие логи (например, VERBOSE), отправляем только важные
        if (priority < Log.INFO) return

        val priorityString = when (priority) {
            Log.INFO -> "INFO"
            Log.WARN -> "WARN"
            Log.ERROR -> "ERROR"
            else -> "DEBUG"
        }

        // Формируем JSON-строку для отправки
        val safeMessage = message.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")

        val safeException = (t?.let { Log.getStackTraceString(it) } ?: "")
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")

// 2. Формируем JSON ОДНОЙ СТРОКОЙ без физических переносов внутри шаблона
        val jsonPayload = "[{\"deviceId\":\"$deviceId\",\"tag\":\"${tag ?: "GLOBAL"}\",\"level\":\"$priorityString\",\"message\":\"$safeMessage\",\"exception\":\"$safeException\",\"timestamp\":${System.currentTimeMillis()}}]".trimIndent()

        // Отправляем на сервер асинхронно в фоновом потоке
        scope.launch {
            sendHttpLog(jsonPayload)
        }
    }

    private fun sendHttpLog(json: String) {
        try {
            val url = URL(serverUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true

            connection.outputStream.use { os ->
                val input = json.toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }

            // Читаем ответ, чтобы запрос физически выполнился
            val responseCode = connection.responseCode
            if (responseCode != 200 && responseCode != 201) {
                // Если сервер недоступен, пишем в стандартный LogCat драйвер напрямую
                Log.e("RemoteTree", "Server returned code $responseCode")
            }
        } catch (e: Exception) {
            Log.e("RemoteTree", "Failed to send log to server", e)
        }
    }
}