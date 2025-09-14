package com.example.wmsRemote
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.wmsRemote.data.db.CatalogAtomy
import com.example.wmsRemote.databinding.ActivityPickerBinding
import com.example.wmsRemote.data.db.Cell
import com.example.wmsRemote.data.db.GoodsAtomy
import com.example.wmsRemote.data.db.MainDB
import com.example.wmswherther.HelperFunction
import com.example.wmswherther.data.db.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class PickerActivity : AppCompatActivity() {
    private var _binding: ActivityPickerBinding? = null
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding for ActivityMain")
    private lateinit var barcodeTextView: TextView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = MainDB.getDB(this)
        _binding = ActivityPickerBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        barcodeTextView = binding.etBarcode
        val client : Request = Request()
        val ip = "192.168.6.208"

        setNavigationBar()

        with(binding) {
            etTe.requestFocus()
            btnSave.setOnClickListener {
                //TODO when scaning twicly in the cell field,a record is added in db and the fields are cleared
                //TODO Validation for the cell field shoud be added
                val te = binding.etTe.text.toString()
                val barcode = binding.etBarcode.text.toString()
                val expiration = binding.etTime.text.toString()
                val cell= binding.etCell.text.toString()
                if(te != "" && barcode != "" && expiration != "" && isCell(cell)){
                    lifecycleScope.launch {
                        withContext(Dispatchers.Main){
                            swipe.isRefreshing = true
                        }
                        withContext(Dispatchers.IO) {
                            val cellId  = getCellId(cell, client, ip)
                            //TODO Check if the supplier record(atomy)  exist in table and the table is exist, should be added
                            val catalogId = getCatalogId(barcode, client, ip)
                            println(cellId)
                            var goods = GoodsAtomy(
                                Id = null,
                                catalogId = catalogId,
                                cellId = cellId,
                                amount = 1,
                                TE = te,
                                date = expiration,
                                createdAt = LocalDateTime.now().toString()

                            )

                            var result = HelperFunction.retryRequest(this@PickerActivity) { client.sendGoodsAtomy(ip, goods)}
                            println(result)
                        }
                        withContext(Dispatchers.Main){
                            clearField(binding.etTe, binding.etBarcode, binding.etTime, binding.etCell)
                            swipe.isRefreshing = false
                        }
                    }
                }else{
                    Toast.makeText(this@PickerActivity, "Что-то не так",Toast.LENGTH_SHORT).show()
                }


            }


            etTe.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus){
                    barcodeTextView = binding.etTe
                }
            }
            etTime.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus){
                    barcodeTextView = binding.etTime
                }
            }
            etBarcode.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus){
                    barcodeTextView = binding.etBarcode
                }
            }
            etCell.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus){
                    barcodeTextView = binding.etCell
                }
            }

        }


    }

    suspend fun getCatalogId(barcode: String, client: Request, ip: String): Int {
        var catalog = HelperFunction.retryRequest(this@PickerActivity) { client.getAtomyCatalogByBarcode(ip, barcode)}
        if(catalog.length() == 0){
            var catalogId = HelperFunction.retryRequest (this@PickerActivity){ client.sendAtomyCatalog(ip, CatalogAtomy(null, barcode, barcode, barcode))}
            return  catalogId.toInt()
        }else{
            return catalog["id"].toString().toInt()
        }
    }

    private suspend fun getCellId(cellName: String, client: Request, ip: String): Int {
        var cell = HelperFunction.retryRequest(this@PickerActivity){client.getCellByName(ip, cellName)}
        var cellId : Int = -1
        if(cell.length() == 0){
            var cellObj = HelperFunction.retryRequest(this@PickerActivity){ client.sendCell(ip, cellName)}
            cellId = cellObj["id"].toString().toInt()
        }else{
            cellId = cell["id"].toString().toInt()
        }
        return cellId
    }
    private fun clearField(etTe: EditText, etBarcode: EditText, etTime: EditText, etCell: EditText) {
        etTe.setText("")
        etBarcode.setText("")
        etTime.setText("")
        etCell.setText("")
    }
    private fun isCell(cell: String): Boolean {
        if (cell.length == 4 && cell[0] in 'A' .. 'Z' && cell[1].isDigit() && cell[2].isDigit() && cell[3].isDigit()){
            return true
        }
        return false
    }
    private fun setNavigationBar() {
        val window = window
        // Устанавливаем флаги для скрытия навигационных кнопок
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
    }

}
