package com.example.wmsRemote
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.ScrollView
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.core.view.setPadding
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.wmsRemote.databinding.ActivityMainBinding
import com.example.wmsRemote.data.db.Cell
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.viewModel.AssemblyViewModel
import com.example.wmswherther.Fragments.MainFragment
import com.example.wmswherther.LogActivity
import com.example.wmswherther.viewModel.MainViewModel
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException
import com.opencsv.CSVReader
import java.io.BufferedReader
import java.io.FileReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.time.LocalDateTime


class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding for ActivityMain")
    private lateinit var getContent: ActivityResultLauncher<String>
    val viewModel: MainViewModel by viewModels()

    override fun onBackPressed() {
        val count = supportFragmentManager.backStackEntryCount

        if (count == 2) {
            viewModel.showMenu()
            super.onBackPressed()
        }

        if (viewModel.IsIncomeMenuActive.value == true) {
            // Если есть фрагменты в стеке
            val dialog = AlertDialog.Builder(this@MainActivity)
                .setTitle("Выход")
                .setMessage("Точно закрыть текущий экран?")
                .setPositiveButton("Да") { _, _ ->
                    viewModel.finishIncomeMenu()
                    super.onBackPressed()
                }
                .setNegativeButton("Нет", null)
                .create()
            dialog.show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = MainDB.getDB(this)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        setNavigationBar()
        var barcodeBuffer: StringBuilder = StringBuilder()

        viewModel.IsMenuActive.observe(this) { isActive ->
            if(isActive){
                binding.btnBack.visibility = View.GONE
            }else{
                binding.btnBack.visibility = View.VISIBLE
            }
        }
        viewModel.IsIncomeSessionActive.observe(this) { isActive ->
            if(isActive){
                binding.btnBarcode.visibility = View.VISIBLE
            }else{
                binding.btnBarcode.visibility = View.GONE
            }
        }
        viewModel.IsIncomeSessionTEModeActive.observe(this) { isActive ->
            if(isActive){
                binding.btnBarcode.setImageResource(R.drawable.barcode_selected)
            }else{
                binding.btnBarcode.setImageResource(R.drawable.barcode)
                if(viewModel.IsWorkableTE.value == true){
                    var dialog = android.app.AlertDialog.Builder(this@MainActivity)
                        .create()
                    var text = EditText(this)
                    text.width = 100
                    text.setPadding(30)
                    text.isSingleLine = true
                    text.requestFocus()
                    dialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, "Сохранить") { _, _ ->
                        viewModel.unworkTe()
                        viewModel.setTE(text.text.toString())
                    }
                    dialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, "Отмена") { dialogInterface, _ ->
                        viewModel.turnOnTeMode()
                        viewModel.workTe()
                        dialogInterface.dismiss()
                    }
                    dialog.setOnCancelListener {
                        viewModel.turnOnTeMode()
                        viewModel.workTe()
                    }

                    dialog.setView(text)
                    dialog.show()
                   /* dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)// after that two line work keyboard when click update line
                    dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)*/
                }
            }
        }
        viewModel.IsScanningActive.observe(this){ isActive ->
            if(isActive == true){
                binding.etIncomeBarcode.visibility = View.VISIBLE
                var widthOfScanning = getWidth(binding)
                viewModel.setWidthScanningField(widthOfScanning)
                binding.etIncomeBarcode.requestFocus()
            }else{
                binding.etIncomeBarcode.visibility = View.GONE
                binding.etIncomeBarcodeScan.requestFocus()
            }
        }
        viewModel.WidthScanningField.observe(this){ value ->
            binding.etIncomeBarcode.width = value
        }
        viewModel.Barcode.observe(this){
            binding.etIncomeBarcodeScan.requestFocus()
        }
        if(savedInstanceState == null){
            supportFragmentManager.commit {
                setCustomAnimations(
                    R.anim.slide_in_right, // enter
                    R.anim.slide_out_left,  // exit
                    R.anim.slide_in_left,   // popEnter
                    R.anim.slide_out_right
                )
                setReorderingAllowed(true)
                replace(R.id.fragmentContainer, MainFragment())
                addToBackStack(null)


            }
        }
        with(binding){
            btnBack.setOnClickListener{
                val count = supportFragmentManager.backStackEntryCount


                /*if (count == 2) {
                    viewModel.showMenu()
                    super.onBackPressed()
                }*/
                if (viewModel.IsIncomeSessionActive.value == true) {
                    // Если есть фрагменты в стеке
                    val dialog = AlertDialog.Builder(this@MainActivity)
                        .setTitle("Выход")
                        .setMessage("Точно закрыть текущий экран?")
                        .setPositiveButton("Да") { _, _ ->
                            viewModel.finishIncomeSession()
                            viewModel.turnOffTeMode()
                            super.onBackPressed()
                        }
                        .setNegativeButton("Нет", null)
                        .create()
                    dialog.show()
                }
                else if (viewModel.IsIncomeMenuActive.value == true) {
                    super.onBackPressed()
                    viewModel.finishIncomeMenu()
                    viewModel.showMenu()
                }
            }
            btnBarcode.setOnClickListener {
                if(viewModel.IsIncomeSessionTEModeActive.value == true){
                    viewModel.turnOffTeMode()
                }else{
                    viewModel.turnOnTeMode()
                    viewModel.workTe()
                }
            }
            etIncomeBarcode.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    val text = etIncomeBarcode.text.toString()
                    if(text != "") {
                        viewModel.setBarcode(text)
                        viewModel.updateBarcode()
                        etIncomeBarcode.setText("")
                    }
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false

            }
            btnThreeDots.setOnClickListener { view ->
            if (viewModel.IsIncomeSessionActive.value == true) {
                    val inflater = layoutInflater
                    val popupView = inflater.inflate(R.layout.pop_up_income_menu, null)
                    var scanBtn = popupView.findViewById<Button>(R.id.btnScanningMode)

                    val popupWindow = PopupWindow(
                        popupView,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        true
                    )
                    scanBtn.setOnClickListener { view ->
                        viewModel.switchScanMode()
                        popupWindow.dismiss()
                    }
                    val location = IntArray(2)
                    btnThreeDots.getLocationOnScreen(location)

// Show popup to the left of the button
                    popupWindow.showAtLocation(
                        btnThreeDots,
                        Gravity.NO_GRAVITY,
                        location[0] - popupWindow.width,  // x coordinate - to the left
                        location[1] + btnThreeDots.height // y coordinate
                    )
                }
            }

        }
        binding.etIncomeBarcodeScan.setOnKeyListener {v, keyCode, event ->
            val ch = event.unicodeChar.toChar()
            if (ch == '\u0000' || event.action == KeyEvent.ACTION_UP){
               return@setOnKeyListener false
            }
            if (keyCode == KeyEvent.KEYCODE_ENTER || ch == '\n' || ch == '\r') {
                val scannedCode = barcodeBuffer.toString()
                barcodeBuffer.clear()
                viewModel.setBarcode(scannedCode)
                return@setOnKeyListener true
            } else {
                barcodeBuffer.append(ch)
                return@setOnKeyListener true
            }
        }
        binding.etIncomeBarcodeScan.requestFocus()
    }
    override fun onResume() {
        super.onResume()
        binding.etIncomeBarcodeScan.requestFocus()
        binding.etIncomeBarcodeScan.post {
            binding.etIncomeBarcodeScan.requestFocus()
            }
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
private fun getWidth(binding: ActivityMainBinding) : Int {
    return binding.main.width - (binding.btnBack.width + binding.btnBarcode.width + binding.btnThreeDots.width + binding.btnSearch.width)
}
fun readTextFileFromInternalStorage(context: Context, fileName: String): String {
    val file = File(context.filesDir, fileName)
    val reader = BufferedReader(InputStreamReader(file.inputStream(), Charset.forName("Windows-1251")))
    return reader.readText()

}
/*fun writeDataToFile(fileName: String, context: Context, db: MainDB){
    var data = db.getDao().getAllItems()
    var str = ""
    for (element in data){
        str += "${element.id} ${element.te} ${element.barcode} ${element.time} ${element.cell}\n"
    }
    try {
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use { outputStream ->
            outputStream.write(str.toByteArray())
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}*/
/* fun readBorkCatalog(db: MainDB){
     lifecycleScope.launch {
         withContext(Dispatchers.IO) {
             val fileContent = readTextFileFromInternalStoragee(this@MainActivity, "Bork.txt")
             val splittedData = fileContent.split("\r\n").dropLast(1)
             try {
                 for (line in splittedData) {
                     val (id, name, barcode) = line.split(",")
                         .map { element -> element.trim() }
                     val item = CatalogBork(
                         id.toInt(),
                         name = name,
                     )
                     var catalogId: Long? = null
                     var catalog = db.getDao().getCatalogBorkByName(name)
                     if(catalog == null){
                         catalogId = db.getDao().insertCatalogBork(item)
                     }else{
                         catalogId = catalog.id!!.toLong()
                     }
                     val barcodeBork = BarcodeBork(
                         id = null,
                         name = barcode,
                         type = "master",
                         catalogId = catalogId!!.toInt()
                     )
                     db.getDao().insertBorkBarcode(barcodeBork)

                 }
             } catch (e: IOException) {
                 println("${e.message}")
             }


         }
     }
 }
 @RequiresApi(Build.VERSION_CODES.O)
 fun readAtomyCatalogAndGoodsFromTxt(db: MainDB, mainActivity: MainActivity){
     lifecycleScope.launch {
         withContext(Dispatchers.IO) {
             val fileContent = readTextFileFromInternalStorage(this@MainActivity, "Atomy.txt")
             val splittedData = fileContent.split("\r\n").dropLast(1)
             try {
                 for (line in splittedData) {
                     val (TrE, barcode, date, cell) = line.split(";")
                         .map { element -> element.trim() }
                     var newCell :Cell? = getCell(cell, db)
                     var catalogId = getCatalogAtomyId(db, newCell, barcode)
                     val item = GoodsAtomy(
                         null,
                         catalogId = catalogId,
                         cellId = newCell!!.id ?: 0,
                         amount = 1,
                         TE = TrE.split(" ")[1],
                         date = date,
                         createdAt = LocalDateTime.now().toString()
                     )
                     db.getDao().insertGoodsAtomy(item)

                 }
             } catch (e: IOException) {
                 println("${e.message}")
             }


         }
     }
 }
 fun readTextFileFromInternalStoragee(context: Context, fileName: String, charset: Charset = Charsets.UTF_8): String {
     context.openFileInput(fileName).use { inputStream ->
         return InputStreamReader(inputStream, charset).readText()
     }
 }
 fun readBorkCatalogAndGoodsFromTxt(db: MainDB, mainActivity: MainActivity){
     lifecycleScope.launch {
         withContext(Dispatchers.IO) {
             val fileContent = readTextFileFromInternalStoragee(this@MainActivity, "Bork.txt")
             val splittedData = fileContent.split("\r\n").dropLast(1)
             try {
                 for (line in splittedData) {
                     val (name, barcode, cell, count) = line.split(";")
                         .map { element -> element.trim() }
                     var newCell :Cell? = getCell(cell, db)
                     var catalogId = getCatalogBorkId(db, newCell, barcode, name)
                     val item = GoodsBork(
                         null,
                         catalogId = catalogId,
                         cellId = newCell!!.id ?: 0,
                         amount = count.toInt(),
                         createdAt = LocalDateTime.now().toString()
                     )
                     db.getDao().insertGoodsBork(item)

                 }
             } catch (e: IOException) {
                 println("${e.message}")
             }


         }
     }
 }
 suspend fun getCatalogAtomyId(db: MainDB, newCell: Cell?, barcode: String): Int {
     var dao = db.getDao()
     var catalog = dao.getAllCatalogsAtomy().firstOrNull{ catalogItem ->
         catalogItem.firstBarcode == barcode
     }
     if(catalog == null){
         var id = dao.insertCatalogAtomy(CatalogAtomy(null, "name", barcode, barcode))
         return id.toInt()
     }else{
         return catalog.id!!.toInt()
     }

 }
 suspend fun getCatalogBorkId(db: MainDB, newCell: Cell?, barcode: String, name: String): Int {
     var dao = db.getDao()
     var catalog = dao.getAllBorkBarcode().firstOrNull{ catalogItem ->
         catalogItem.name == barcode
     }
     if(catalog == null){
         var id = dao.insertCatalogBork(CatalogBork(null, name))
         var barcodeNew = BarcodeBork(null, barcode,"master", id.toInt())
         dao.insertBorkBarcode(barcodeNew)
         return id.toInt()
     }else{
         return catalog.id!!.toInt()
     }

 }
 private fun getCell(cell: String, db: MainDB): Cell? {
     var newCell = db.getDao().getAllCells().firstOrNull{ innerCell->
         innerCell.name == cell
     }
     if(newCell == null){
         var id = db.getDao().insertCell(Cell(null, cell))
         newCell = Cell(id.toInt(), cell)
     }
     return newCell
 }
 fun readAtomyCatalog(db: MainDB, mainActivity: MainActivity){
     lifecycleScope.launch {
         withContext(Dispatchers.IO){
         var items = getAtomyFromCsv("/testWms_db-atomyItems.csv", mainActivity)
         items.forEach { item ->
             var catalog = CatalogAtomy(null, "noName ${item.te}", item.barcode, item.expiration)
             var result = db.getDao().insertCatalogAtomy(catalog)
             if(isCell(item.cell)) {
                 var cell = db.getDao().getAllCells().firstOrNull{cell-> cell.name == item.cell}
                 if (cell == null) {
                     db.getDao().insertCell(Cell(null, item.cell))
                     cell = db.getDao().getAllCells().firstOrNull { cell -> cell.name == item.cell }
                 }

                 val goods: GoodsAtomy = GoodsAtomy(null, result.toInt(), cell!!.id!!, 1, "324", "2341", LocalDateTime.now().toString())
                 db.getDao().insertGoodsAtomy(goods)
             }
         }
         }
     }
 }
 fun getAtomyFromCsv(path: String, mainActivity: MainActivity): MutableList<AtomyItem> {
     var items = mutableListOf<AtomyItem>()
     var reader = CSVReader(FileReader(mainActivity.filesDir.absolutePath + path))

     reader.forEach{ line->
         val splittedItem = line[0].toString().split(";")
         val id = splittedItem[0].toIntOrNull()
         if( id != null) {
             val te = splittedItem[1]
             val barcode = splittedItem[2]
             val expiration = splittedItem[3]
             val cell = splittedItem[4]
             items.add(AtomyItem(id, te, barcode, expiration, cell))
         }
     }

     reader.close()
     return items
 }
 */

private fun isCell(cell: String): Boolean {
    if (cell.length == 4 && cell[0] in 'A' .. 'Z' && cell[1].isDigit() && cell[2].isDigit() && cell[3].isDigit()){
        return true
    }
    return false
}


