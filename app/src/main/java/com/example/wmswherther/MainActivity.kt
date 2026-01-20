package com.example.wmsRemote
import android.app.AlertDialog.*
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
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.wmsRemote.databinding.ActivityMainBinding
import com.example.wmsRemote.data.db.Cell
import com.example.wmsRemote.data.db.Dao
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.viewModel.AssemblyViewModel
import com.example.wmswherther.Classes.UiState
import com.example.wmswherther.Classes.UiState.*
import com.example.wmswherther.Fragments.IncomeFragment
import com.example.wmswherther.Fragments.MainFragment
import com.example.wmswherther.Fragments.SearchFragment
import com.example.wmswherther.LogActivity
import com.example.wmswherther.data.db.Goods
import com.example.wmswherther.viewModel.IncomeSessionViewModel
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

data class SupplierItem(val id: String, val text: String)

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding for ActivityMain")
    private lateinit var getContent: ActivityResultLauncher<String>
    val viewModel: MainViewModel by viewModels()
    private val localViewModel: IncomeSessionViewModel by viewModels()

    override fun onBackPressed() {
        //val count = supportFragmentManager.backStackEntryCount

        when(val state = viewModel.uiState.value){
            is IncomeMenu -> {
                viewModel.setActiveUi(state.prevState!!)
                super.onBackPressed()
            }
            is IncomeSessionMenu -> {
                val dialog = AlertDialog.Builder(this@MainActivity)
                    .setTitle("Выход")
                    .setMessage("Точно закрыть текущий экран?")
                    .setPositiveButton("Да") { _, _ ->
                        viewModel.setActiveUi(state.prevState!!)
                        super.onBackPressed()
                    }
                    .setNegativeButton("Нет", null)
                    .create()
                dialog.show()
            }
            is MainMenu -> {}
            is MoveMenu -> {
                viewModel.setActiveUi(state.prevState!!)
                super.onBackPressed()
            }
            MoveSessionMenu -> {}
            is SearchMenu -> {
                viewModel.setActiveUi(state.prevState!!)
                super.onBackPressed()
            }
            null -> {}
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = MainDB.getDB(this)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        viewModel.setMainBinding(binding)
        enableEdgeToEdge()
        setContentView(binding.root)
        setNavigationBar()
       // var searchData : MutableList<Pair<String, List<String>>> = mutableListOf()
        var barcodeBuffer: StringBuilder = StringBuilder()

        viewModel.uiState.observe(this){ State->
            when(State){
                is SearchMenu ->{
                    binding.btnBack.visibility = if (State.isBackBtnActive) View.VISIBLE else View.GONE
                    binding.btnSearch.visibility = if (State.isSearchLoopActive) View.VISIBLE else View.GONE
                    binding.btnBarcode.visibility = if (State.isTEModeActive) View.VISIBLE else View.GONE
                    binding.etIncomeBarcode.visibility = if (State.isBarcodeFieldActive) View.VISIBLE else View.GONE
                    var widthOfScanning = getWidth(binding)
                    viewModel.setWidthScanningField(widthOfScanning)
                    binding.etIncomeBarcode.requestFocus()
                }
                is IncomeMenu -> {
                    binding.btnBack.visibility = if (State.isBackBtnActive) View.VISIBLE else View.GONE
                    binding.btnSearch.visibility = if (State.isSearchLoopActive) View.VISIBLE else View.GONE
                    binding.btnBarcode.visibility = if (State.isTEModeActive) View.VISIBLE else View.GONE
                    binding.etIncomeBarcode.visibility = if (State.isBarcodeFieldActive) View.VISIBLE else View.GONE
                }
                is IncomeSessionMenu -> {

                    binding.btnBack.visibility = if (State.isBackBtnActive) View.VISIBLE else View.GONE
                    binding.btnSearch.visibility = if (State.isSearchLoopActive) View.VISIBLE else View.GONE
                    binding.btnBarcode.visibility = if (State.isTEBtnActive) View.VISIBLE else View.GONE
                    binding.etIncomeBarcode.visibility = if (State.isBarcodeFieldActive) View.VISIBLE else View.GONE
                    binding.etIncomeBarcodeScan.visibility = if (State.isBarcodeScanActive) View.VISIBLE else View.GONE
                    var widthOfScanning = getWidth(binding)
                    viewModel.setWidthScanningField(widthOfScanning)
                    if (State.isBarcodeFieldActive)
                        binding.etIncomeBarcode.requestFocus()
                    else binding.etIncomeBarcodeScan.requestFocus()
                    if(State.isTEModeActive){
                        binding.btnBarcode.setImageResource(R.drawable.barcode_selected)
                    }else{
                        binding.btnBarcode.setImageResource(R.drawable.barcode)
                    }

                }
                is MainMenu -> {
                    binding.btnBack.visibility = if (State.isBackBtnActive) View.VISIBLE else View.GONE
                    binding.btnSearch.visibility = if (State.isSearchLoopActive) View.VISIBLE else View.GONE
                    binding.btnBarcode.visibility = if (State.isTEModeActive) View.VISIBLE else View.GONE
                    binding.etIncomeBarcode.visibility = if (State.isBarcodeFieldActive) View.VISIBLE else View.GONE
                }
                is MoveMenu -> {
                    binding.btnBack.visibility = if (State.isBackBtnActive) View.VISIBLE else View.GONE
                    binding.btnSearch.visibility = if (State.isSearchLoopActive) View.VISIBLE else View.GONE
                    binding.btnBarcode.visibility = if (State.isTEModeActive) View.VISIBLE else View.GONE
                    binding.etIncomeBarcode.visibility = if (State.isBarcodeFieldActive) View.VISIBLE else View.GONE
                }
                MoveSessionMenu -> {}
                is SearchMenu -> {}
            }
        }
        viewModel.WidthScanningField.observe(this){ value ->
            binding.etIncomeBarcode.width = value
        }
        viewModel.Barcode.observe(this){ searchData ->
            binding.etIncomeBarcodeScan.requestFocus()
            when(val state = viewModel.uiState.value){
                is SearchMenu ->{
                    if(state.searchPattern == "Barcode"){
                        // Если шк то сначала найти каталог такой, а дальше все goods и уже ячейки к ним
                        lifecycleScope.launch {
                            var totalResult: String = ""
                            withContext(Dispatchers.IO) {
                                var dao = db.getDao()
                                var bar = dao.getBarcodeByName(searchData)
                                if(bar != null) {
                                    var catalog = dao.getCatalogById(bar.catalogId)
                                    var goodsList = dao.getGoodsByCatalogId(catalog.id)
                                    var result = goodsList.map { item ->
                                        var cell = dao.getCellById(item.cellId)
                                        "${catalog.name}   ${item.amount}   ${cell.name}"
                                    }
                                    totalResult = result.joinToString("\n")
                                    println()
                                }
                            }
                            //TODO удалить result в view когда забиваешь к примеру буквы вместо шк
                            withContext(Dispatchers.Main) {
                                viewModel.setSearchData(totalResult)
                            }

                        }
                    }else if (state.searchPattern == "Name"){
                        // Если наименование, то надо найти все каталоги которые имею данное вхождение
                        // Дальше найти все goods с данным каталогом
                        // Сформировать результат
                        lifecycleScope.launch {
                            var totalResult: String = ""
                            withContext(Dispatchers.IO) {
                                var dao = db.getDao()
                                var catalogs = dao.getCatalogs().filter { item ->
                                    item.name.contains(searchData)
                                }
                                var goodsList : MutableList<Pair<Goods, String>> = mutableListOf()
                                catalogs.forEach { item->
                                    var goods = dao.getGoodsByCatalogId(item.id)
                                    goodsList.addAll(goods.map { innerItem -> Pair(innerItem, item.name ) })
                                }
                                var result = goodsList.map { item ->
                                    var cell = dao.getCellById(item.first.cellId)
                                    "${item.second}   ${item.first.amount}   ${cell.name}" //item.second is name of catalog
                                }
                                totalResult = result.joinToString("\n")
                            }
                            withContext(Dispatchers.Main) {
                                viewModel.setSearchData(totalResult)
                            }

                        }
                    }else if (state.searchPattern == "Cells"){
                        //Надо отобразить все что привязано к ячейки в том числе и те
                        //Сначало надо показать весь товар на ячейке, а потом сами ячейки которые привязаны к данной ячейке
                        lifecycleScope.launch {
                            var totalResult: String = ""
                            withContext(Dispatchers.IO) {
                                var dao = db.getDao()
                                var cell = dao.getCellByName(searchData)
                                if(cell != null){
                                    var goods = dao.getGoodsByCellId(cell.id)
                                    var result = goods.map { item ->
                                        var catalog = dao.getCatalogById(item.catalogId)
                                        "${catalog.name}   ${item.amount}   ${cell.name}"
                                    }
                                    var cells = dao.getChildrenCells(cell.id)
                                    if(cells != null){
                                        result += cells.map { item ->
                                            "${item.name}   ${1}   ${cell.name}"
                                        }
                                    }

                                    totalResult = result.joinToString("\n")
                                }
                            }
                            withContext(Dispatchers.Main) {
                                viewModel.setSearchData(totalResult)
                            }

                        }
                    }
                }
                is IncomeMenu -> {}
                is IncomeSessionMenu -> {}
                is MainMenu -> {}
                is MoveMenu -> {}
                MoveSessionMenu -> {}
                null -> {}
            }
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
        with(binding) {
            btnBack.setOnClickListener {
                when(val state = viewModel.uiState.value){
                    is IncomeMenu -> {
                        viewModel.setActiveUi(state.prevState!!)
                        super.onBackPressed()
                    }
                    is IncomeSessionMenu -> {
                        val dialog = AlertDialog.Builder(this@MainActivity)
                            .setTitle("Выход")
                            .setMessage("Точно закрыть текущий экран?")
                            .setPositiveButton("Да") { _, _ ->
                                viewModel.setActiveUi(state.prevState!!)
                                super.onBackPressed()
                            }
                            .setNegativeButton("Нет", null)
                            .create()
                        dialog.show()
                    }
                    is MainMenu -> {}
                    is MoveMenu -> {
                        viewModel.setActiveUi(state.prevState!!)
                        super.onBackPressed()
                    }
                    MoveSessionMenu -> {}
                    is SearchMenu -> {
                        viewModel.setActiveUi(state.prevState!!)
                        super.onBackPressed()
                    }
                    null -> {}
                }
            }
            btnBarcode.setOnClickListener {
                if ((viewModel.uiState.value as UiState.IncomeSessionMenu).isTEModeActive) {
                    var dialog = Builder(this@MainActivity)
                        .create()
                    var text = EditText(this@MainActivity)
                    text.width = 100
                    text.setPadding(30)
                    text.isSingleLine = true
                    text.requestFocus()
                    dialog.setButton(BUTTON_POSITIVE, "Сохранить") { _, _ ->
                        if(isBoxTE(text.text.toString())){
                            viewModel.setActiveUi((viewModel.uiState.value as IncomeSessionMenu).copy(isTEModeActive = false))
                            viewModel.setTE(text.text.toString())
                        }else{
                            Toast.makeText(this@MainActivity, "invalid te barcode", Toast.LENGTH_SHORT).show()
                        }
                    }
                    dialog.setButton(BUTTON_NEGATIVE, "Закрыть") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    dialog.setButton(BUTTON_NEUTRAL, "Отмена") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    /*dialog.setOnCancelListener {
                        viewModel.turnOnTeMode()
                        viewModel.workTe()
                    }*/

                    dialog.setView(text)
                    dialog.show()
                }
                else {
                    viewModel.setActiveUi((viewModel.uiState.value as UiState.IncomeSessionMenu).copy(isTEModeActive = true))
                }
            }
            etIncomeBarcode.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    val text = etIncomeBarcode.text.toString()
                    if (text != "") {
                        viewModel.setBarcode(text)
                        etIncomeBarcode.setText("")
                    }
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false

            }
            btnThreeDots.setOnClickListener { view ->
                when(val state = viewModel.uiState.value){
                    is SearchMenu -> {
                        val inflater = layoutInflater
                        val popupView = inflater.inflate(R.layout.pop_up_search_menu, null)
                        var btnSupplier = popupView.findViewById<Button>(R.id.btnSuppliersList)
                        var btnName = popupView.findViewById<Button>(R.id.btnName)
                        var btnBarcode = popupView.findViewById<Button>(R.id.btnBarcode)
                        var btnCells = popupView.findViewById<Button>(R.id.btnCells)

                        val popupWindow = PopupWindow(
                            popupView,
                            WindowManager.LayoutParams.WRAP_CONTENT,
                            WindowManager.LayoutParams.WRAP_CONTENT,
                            true
                        )
                        btnSupplier.setOnClickListener { view ->
                            var supplierList : List<SupplierItem> = listOf()
                            var counter = 0
                            var selected = 0
                            lifecycleScope.launch {
                                Dispatchers.IO{
                                    var dao = MainDB.getDB(this@MainActivity).getDao()

                                    supplierList = dao.getAllSuppliers().map { item ->
                                        if(viewModel.CurrentSupplierId.value == item.id){
                                            selected = counter
                                            counter = counter + 1
                                            SupplierItem(item.id, item.name)
                                        }else{
                                            counter = counter + 1
                                            SupplierItem(item.id, item.name)
                                        }

                                    }
                                }
                                Dispatchers.Main {
                                    var dialog = Builder(this@MainActivity)
                                        .create()
                                    var spinner = Spinner(this@MainActivity)
                                    spinner.setPadding(20, 40, 20, 0)
                                    val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, supplierList.map { it.text })
                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                                    spinner.adapter = adapter
                                    spinner.setSelection(selected)

                                    dialog.setButton(BUTTON_POSITIVE, "Выбрать") { _, _ ->
                                        val pos = spinner.selectedItemPosition
                                        val selected = supplierList[pos]
                                        viewModel.setCurrentSupplierId(selected.id)
                                    }
                                    dialog.setButton(BUTTON_NEGATIVE, "Отмена") { dialogInterface, _ ->
                                        dialogInterface.dismiss()
                                    }

                                    dialog.setView(spinner)
                                    dialog.show()
                                    popupWindow.dismiss()
                                }
                            }
                        }
                        btnName.setOnClickListener { view ->
                            viewModel.setActiveUi(state.copy(searchPattern = "Name"))
                            popupWindow.dismiss()
                        }
                        btnCells.setOnClickListener { view ->
                            viewModel.setActiveUi(state.copy(searchPattern = "Cells"))
                            popupWindow.dismiss()
                        }
                        btnBarcode.setOnClickListener { view ->
                            viewModel.setActiveUi(state.copy(searchPattern = "Barcode"))
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
                    is IncomeMenu -> {}
                    is IncomeSessionMenu -> {
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
                            viewModel.setActiveUi(state.copy(
                                isBarcodeFieldActive = !state.isBarcodeFieldActive,
                                isBarcodeScanActive = !state.isBarcodeScanActive
                                ))
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
                    is MainMenu -> {}
                    is MoveMenu -> {}
                    MoveSessionMenu -> {}
                    null -> {}
                }

            }
            etIncomeBarcodeScan.setOnKeyListener {v, keyCode, event ->
                val ch = event.unicodeChar.toChar()
                if (ch == '\u0000' || event.action == KeyEvent.ACTION_UP){
                    return@setOnKeyListener false
                }
                if (keyCode == KeyEvent.KEYCODE_ENTER || ch == '\n' || ch == '\r') {
                    val scannedCode = barcodeBuffer.toString()
                    barcodeBuffer.clear()
                    viewModel.setBarcode(scannedCode)
                    viewModel.setBarcode("")
                    return@setOnKeyListener true
                } else {
                    barcodeBuffer.append(ch)
                    return@setOnKeyListener true
                }
            }
            etIncomeBarcodeScan.requestFocus()//TODO попробовать использовать тсд и удалить, может и без этого норм работат, а то помню много раз пробовал переделать
            btnSearch.setOnClickListener {
            val currentFragment = supportFragmentManager
                .findFragmentById(R.id.fragmentContainer)
                if(currentFragment != null){

                    viewModel.setActiveUi(SearchMenu(prevState = viewModel.uiState.value))
                    currentFragment.parentFragmentManager.commit {
                        setCustomAnimations(
                            R.anim.slide_in_right, // enter
                            R.anim.slide_out_left,  // exit
                            R.anim.slide_in_left,   // popEnter
                            R.anim.slide_out_right  // popExit
                        )
                        hide(currentFragment)
                        //viewModel.CurrFragment.value.let { it -> hide(it!!) }
                        add<SearchFragment>(R.id.fragmentContainer)
                        addToBackStack(null)

                    }
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        binding.etIncomeBarcodeScan.requestFocus()
        binding.etIncomeBarcodeScan.post {//not work
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
    var width = 0
    if(binding.btnBack.isVisible){
        if(binding.btnBack.width != 0){
            width += binding.btnBack.width
        }else{
            width += 144
        }
    }
    if(binding.btnBarcode.isVisible){
        if(binding.btnBarcode.width != 0){
            width += binding.btnBarcode.width
        }else{
            width += 144
        }
    }
    if(binding.btnThreeDots.isVisible){
        if(binding.btnThreeDots.width != 0){
            width += binding.btnThreeDots.width
        }else{
            width += 144
        }
    }
    if(binding.btnSearch.isVisible){
        if(binding.btnSearch.width != 0){
            width += binding.btnSearch.width
        }else{
            width += 144
        }
    }

    return binding.main.width - width
}
fun readTextFileFromInternalStorage(context: Context, fileName: String): String {
    val file = File(context.filesDir, fileName)
    val reader = BufferedReader(InputStreamReader(file.inputStream(), Charset.forName("Windows-1251")))
    return reader.readText()

}
// <editor-fold desc=" smth">

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
// </editor-fold>
private fun isCell(cell: String): Boolean {
    return cell.length == 4 && cell[0] in 'A' .. 'Z' && cell[1].isDigit() && cell[2].isDigit() && cell[3].isDigit()
}
fun isBoxTE(str: String) : Boolean{
    return str.length == 9
}


