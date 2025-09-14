package com.example.wmswherther

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.wmsRemote.databinding.ActivityLogBinding
import java.io.File


class LogActivity : AppCompatActivity() {
    private var _binding: ActivityLogBinding? = null
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding for ActivityLog")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityLogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.logText.text = readLogs(this@LogActivity)
        binding.btnClear.setOnClickListener {
            val logFile = File(this@LogActivity.filesDir, "app_log.txt")
            logFile.writeText("")

        }
    }
}
private fun readLogs(context:Context): String {
    val internalFile = File(context.filesDir, "app_log.txt")
   // val externalFile = File(context.getExternalFilesDir(null), "app_log.txt")

    val internalLogs = if (internalFile.exists()) internalFile.readText() else "Внутренний лог пуст\n"
    //val externalLogs = if (externalFile.exists()) externalFile.readText() else "Внешний лог пуст\n"

    return "=== INTERNAL LOG ===\n$internalLogs\n\n=== EXTERNAL LOG ===\n$"
}