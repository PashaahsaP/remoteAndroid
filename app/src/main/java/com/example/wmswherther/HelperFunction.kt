package com.example.wmswherther

import android.content.Context
import android.util.Log
import kotlinx.coroutines.delay
import java.io.File
import java.time.LocalDateTime

object HelperFunction {
    suspend fun <T> retryRequest(
        context:Context,
        retries: Int = 15,
        delayMs: Long = 2000,
        block: suspend () -> T
    ): T {
        repeat(retries - 1) {

            try {
                var result = block()
                return result
            } catch (e: Exception) {
                Log.e("RetryRequest", "Ошибка при выполнении запроса (попытка ${it+1}/$retries): ${e.message}", e)
                logToFile(context, e.message.toString());
                delay(delayMs) // подождать перед новой попыткой
            }
        }
        return block() // последняя попытка
    }
}
fun logToFile(context: Context, message: String) {
    val logFile = File(context.filesDir, "app_log.txt")
    logFile.appendText("${LocalDateTime.now()}: $message\n")
}