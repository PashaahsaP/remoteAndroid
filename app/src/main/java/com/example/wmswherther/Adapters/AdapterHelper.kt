package com.example.wmsRemote.Adapters

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.wmsRemote.R
import com.example.wmsRemote.Classes.AtomyInventoryItem
import com.example.wmsRemote.Classes.BorkInventoryItem
import com.example.wmsRemote.Classes.IInventoryItem
import com.example.wmsRemote.data.db.MainDB

import com.example.wmsRemote.viewModel.InventoryViewModel
import com.example.wmswherther.data.db.Request
import org.json.JSONObject

object AdapterHelper {
/*    var client = Request()
    var ip = "192.168.6.208"
    // <editor-fold desc="assembly code">
    val getAssemblyItems: Map<Int, suspend (sessionId: Int, db: MainDB) -> List<AssemblyItem>> = mapOf(*/
        /*1 to { sessionId, db ->
            var data = client.getAllAssemblyBorkItemBySessionId(ip, sessionId)
                .filter { item -> item["status"] == "created" }
                .map { newItem->
                    var statusVal = when(newItem["status"].toString()){
                        "created" -> StatusType.Created.ordinal
                        "work" -> StatusType.Work.ordinal
                        "enterCell"-> StatusType.EnterCell.ordinal
                        "enterBarcode"->StatusType.EnterBarcode.ordinal
                        "enterCount"->StatusType.EnterCount.ordinal
                        "finished"->StatusType.Finished.ordinal
                        "canceled"->StatusType.Canceled.ordinal
                        else -> 999
                    }
                    var goods = client.getBorkGoodsById(ip, newItem["goodsId"].toString())
                    var catalog = client.getBorkCatalogById(ip, goods["catalogId"].toString())
                    var cell = client.getCellById(ip, newItem["cellId"].toString())
                    var barcodes = client.getBorkBarcodeByCatalogId(ip, catalog["id"].toString())
                    AssemblyItem(
                        sessionId = sessionId,
                        assemblyItemId = newItem["id"].toString().toInt(),
                        catalogId = goods["catalogId"].toString().toInt(),
                        supplierId = 1,
                        amount = goods["amount"].toString().toInt(),
                        cell = cell["name"].toString(),
                        name = catalog["name"].toString(),
                        status = statusVal,
                        barcodes = (0 until barcodes.length()).map { item -> barcodes.getJSONObject(item)["name"].toString() }.toList()
                    )

                }
            data.filter { item-> item.status == 0 }.sortedBy { item -> item.cell }
        }*/
   /*)*/
// </editor-fold>
    // <editor-fold desc="inventory code">
   /* val prepareNoneTypeInventoryItem: Map<Int, suspend (inventoryItem: IInventoryItem, db: MainDB, cellId: Int?, context:Context ) -> IInventoryItem?> = mapOf(
        0 to { inventoryItem, db, cellId, context ->
            var result : AtomyInventoryItem? = null
            var newCatalog : CatalogAtomy = CatalogAtomy(null, "", "", null)
            if(inventoryItem.barcode == ""){
                result = null
            }else {
                newCatalog = CatalogAtomy(
                    null,
                    name = inventoryItem.barcode,
                    firstBarcode = inventoryItem.barcode,
                    secondBarcode = inventoryItem.barcode,
                )

                var catalogIdJson = HelperFunction.retryRequest(context) { client.sendAtomyCatalog(ip, newCatalog)}
                val catalogID = catalogIdJson.toInt()
                if (inventoryItem is AtomyInventoryItem) {
                    var newGoods = GoodsAtomy(
                        null,
                        catalogID.toInt(),
                        cellId = convertToInt(cellId),
                        inventoryItem.amount.first,
                        inventoryItem.TE,
                        inventoryItem.date,
                        LocalDateTime.now().toString()
                    )
                    var goodsId = HelperFunction.retryRequest(context) { client.sendGoodsAtomy(ip, newGoods)}
                    result = AtomyInventoryItem(
                        catalogId = catalogID.toInt(),
                        goodsId = goodsId["id"].toString().toInt(),
                        cellId = cellId!!.toInt(),
                        barcode = inventoryItem.barcode,
                        amount = Pair(0, inventoryItem.amount.first),
                        type = inventoryItem.type,
                        supplierId = inventoryItem.supplierId,
                        TE = inventoryItem.TE,
                        date = inventoryItem.date
                    )
                }
            }
            result
        },
        1 to { inventoryItem, db, cellId, context ->
            var result : BorkInventoryItem? = null
            if(inventoryItem.amount.first != 0) {
                if (inventoryItem is BorkInventoryItem) {
                    var catalogInJson = HelperFunction.retryRequest(context) { client.getBorkCatalogByName(ip, inventoryItem.name)}
                    if (catalogInJson.length() == 0) {
                        result = null
                    } else {
                        var catalogId: Int = catalogInJson["id"].toString().toInt()
                        var barcode: BarcodeBork =
                            BarcodeBork(null, inventoryItem.barcode, "master", catalogId)

                        var newGoods = GoodsBork(
                            null,
                            catalogId = catalogId!!.toInt(),
                            cellId = convertToInt(cellId),
                            inventoryItem.amount.first,
                            LocalDateTime.now().toString()
                        )
                        var barcodeJson = HelperFunction.retryRequest(context) { client.sendBorkBarcode(ip, barcode)}
                        var goodsInJson = HelperFunction.retryRequest(context) { client.sendGoodsBork(ip, newGoods)}
                        result = BorkInventoryItem(
                            catalogId = catalogId.toInt(),
                            goodsId = goodsInJson["id"].toString().toInt(),
                            cellId = cellId!!.toInt(),
                            barcode = inventoryItem.barcode,
                            amount = Pair(0, inventoryItem.amount.first),
                            type = "default",
                            supplierId = inventoryItem.supplierId,
                            name = inventoryItem.name,
                        )
                    }
                }
            }
            result
        }
    )
    val removeInventoryItem: Map<Int, suspend (inventoryItem: IInventoryItem, db: MainDB, context:Context)-> IInventoryItem?> = mapOf(
        0 to { inventoryItem, db, context ->
            if(inventoryItem.goodsId !=0 && inventoryItem.catalogId !=0) {
                var cellJson = HelperFunction.retryRequest(context) { client.getCellByName(ip, "Z999")}
                var goodsJson = HelperFunction.retryRequest(context){ client.getAtomyGoodsById(ip, inventoryItem.goodsId.toString())}
                var goods = getGoodsAtomyFromJsonObject(goodsJson)
                if (cellJson.length() == 0) {
                    val cellId = HelperFunction.retryRequest(context){ client.sendCell(ip, "Z999")["id"].toString().toInt()}
                    goods.cellId = convertToInt(cellId)
                    HelperFunction.retryRequest(context) { client.updateAtomyGoods(ip, goods)}
                } else {
                    goods.cellId = convertToInt(cellJson["id"].toString().toInt())
                    HelperFunction.retryRequest (context){ client.updateAtomyGoods(ip, goods)}
                }
                null
            }
            null
        },
        1 to { inventoryItem, db, context  ->
            var cellJson = HelperFunction.retryRequest(context) { client.getCellByName(ip, "Z999")}
            var goodsJson = HelperFunction.retryRequest(context) { client.getBorkGoodsById(ip, inventoryItem.goodsId.toString())}
            var goods = getGoodsBorkFromJsonObject(goodsJson)
            if (cellJson.length() == 0) {
                val cellId = HelperFunction.retryRequest(context) { client.sendCell(ip, "Z999")["id"].toString().toInt()}
                goods.cellId = convertToInt(cellId)
                HelperFunction.retryRequest(context) { client.updateBorkGoods(ip, goods)}
            } else {
                goods.cellId = convertToInt(cellJson["id"].toString().toInt())
                HelperFunction.retryRequest(context) { client.updateBorkGoods(ip, goods)}
            }
            null
        }
    )
    val changeInventoryItem: Map<Int, suspend (inventoryItem: IInventoryItem, db: MainDB, cellId: String, context:Context)-> IInventoryItem?> = mapOf(
        0 to { inventoryItem, db, cellId, context ->
            var result : AtomyInventoryItem? = null
            if(inventoryItem is AtomyInventoryItem) {
                if (inventoryItem.type == "default") {
                    var goodsJson = HelperFunction.retryRequest(context) { client.getAtomyGoodsById(ip, inventoryItem.goodsId.toString())}
                    var goods = getGoodsAtomyFromJsonObject(goodsJson)
                    goods.amount = inventoryItem.amount.first

                    HelperFunction.retryRequest(context){ client.updateAtomyGoods(ip, goods)}

                        result = AtomyInventoryItem(
                            supplierId = inventoryItem.supplierId,
                            catalogId = inventoryItem.catalogId,
                            goodsId = inventoryItem.goodsId,
                            cellId = inventoryItem.cellId,
                            barcode = inventoryItem.barcode,
                            amount = Pair(0, inventoryItem.amount.first),
                            type = inventoryItem.type,
                            TE = inventoryItem.TE,
                            date = inventoryItem.date
                        )
                }else{
                    var catalogId : Int = -1
                    var catalogInJson = HelperFunction.retryRequest(context)  { client.getAtomyCatalogByBarcode(ip, inventoryItem.barcode)}
                    if(catalogInJson.length() == 0){
                        var newCatalog = HelperFunction.retryRequest(context)  { client.sendAtomyCatalog(ip, CatalogAtomy(
                            null,
                            "name",
                            inventoryItem.barcode,
                            inventoryItem.barcode
                        )
                        )}
                        catalogId = newCatalog.toInt()
                    }else{
                        catalogId = catalogInJson["id"].toString().toInt()
                    }
                    var goodsInJson = HelperFunction.retryRequest(context) { client.sendGoodsAtomy(ip, GoodsAtomy(
                            Id = null,
                            catalogId = catalogId,
                            cellId = cellId.toInt(),
                            amount = 1,
                            TE = inventoryItem.TE,
                            date = inventoryItem.date,
                            createdAt = LocalDateTime.now().toString()
                        ))}
                        result = AtomyInventoryItem(
                            supplierId = inventoryItem.supplierId,
                            catalogId = catalogId,
                            goodsId = goodsInJson["id"].toString().toInt(),
                            cellId = inventoryItem.cellId,
                            barcode = inventoryItem.barcode,
                            amount = Pair(0, inventoryItem.amount.first),
                            type = inventoryItem.type,
                            TE = inventoryItem.TE,
                            date = inventoryItem.date
                        )
                }
            }
            result
        },
        1 to { inventoryItem, db, cellId, context ->
            var result: BorkInventoryItem? = null
            if(inventoryItem is BorkInventoryItem) {
                if (inventoryItem.type == "default") {
                    var goodsJson = HelperFunction.retryRequest(context){ client.getBorkGoodsById(ip, inventoryItem.goodsId.toString())}
                    var goods = getGoodsBorkFromJsonObject(goodsJson)
                    goods.amount = inventoryItem.amount.first
                    HelperFunction.retryRequest(context) { client.updateBorkGoods(ip, goods)}

                    if (inventoryItem is BorkInventoryItem) {
                        result = BorkInventoryItem(
                            supplierId = inventoryItem.supplierId,
                            catalogId = inventoryItem.catalogId,
                            goodsId = inventoryItem.goodsId,
                            cellId = inventoryItem.cellId,
                            barcode = inventoryItem.barcode,
                            amount = Pair(0, inventoryItem.amount.first),
                            type = inventoryItem.type,
                            name = inventoryItem.name,
                        )
                    }
                } else {
                    var newGoods = GoodsBork(
                        null,
                        inventoryItem.catalogId,
                        cellId = convertToInt(inventoryItem.cellId),
                        inventoryItem.amount.first,
                        LocalDateTime.now().toString()
                    )
                    var goodsIdJson = HelperFunction.retryRequest(context) { client.sendGoodsBork(ip, newGoods)}
                    var goodsId = goodsIdJson["id"].toString().toInt()
                    result = BorkInventoryItem(
                        catalogId = inventoryItem.catalogId,
                        goodsId = goodsId.toInt(),
                        cellId = inventoryItem.cellId,
                        barcode = inventoryItem.barcode,
                        amount = Pair(0, inventoryItem.amount.first),
                        type = "default",
                        supplierId = inventoryItem.supplierId,
                        name = inventoryItem.name,
                    )
                }
            }
            result
        }
    )*/
    /*val getProcessedInventoryItem: Map<Int,suspend (values:List<IInventoryItem>, input: String, db: MainDB, cell: String, context:Context)-> List<IInventoryItem>> = mapOf(
        0 to {values, input, db, cell, context->
            var cell = HelperFunction.retryRequest(context) { client.getCellByName(ip, cell)}
            var cellId = cell["id"].toString().toInt()
            var result :List<IInventoryItem> = listOf()
            var isNone = true//если не менятся то это новый элемент в коллекции, иначе нужно изменить количество
            values.forEach{item ->
                if(item is AtomyInventoryItem &&  item.TE == input){
                    item.amount = Pair(item.amount.first + 1, item.amount.second)
                    isNone = false
                }
                result += item
            }
            if(isNone){
                result += AtomyInventoryItem(
                    cellId = cellId!!,
                    goodsId = 0,
                    catalogId = 0,
                    supplierId = 0,
                    TE = input,
                    barcode = "",
                    date = "",
                    type = "none",
                    amount = Pair(1,0)

                )

            }
            result
        },
        1 to{ values, input, db, cell, context->
            var cell = HelperFunction.retryRequest(context){client.getCellByName(ip, cell)}
            var cellId = cell["id"].toString().toInt()
            var result :List<IInventoryItem> = listOf()
            var catalog: CatalogBork? = null
            var barcodeJson = HelperFunction.retryRequest(context){client.getBorkBarcodeByName(ip, input)}
            if(barcodeJson.length() != 0) {
                var jsonCatalog = HelperFunction.retryRequest(context){client.getBorkCatalogById(ip, barcodeJson["catalogId"].toString())}
                catalog = getCatalogBorkFromJsonObject(jsonCatalog)
            }
            var isNone = true
            if(catalog != null) {
                values.forEach { item ->
                    if (item is BorkInventoryItem && item.catalogId == catalog.id ) {
                        item.amount = Pair(item.amount.first + 1, item.amount.second)
                        isNone = false
                    }
                    result += item
                }
                if(isNone){
                    result += BorkInventoryItem(
                        cellId = cellId!!.toInt(),
                        goodsId = -1,
                        catalogId = catalog.id!!.toInt(),
                        supplierId = 1,
                        barcode = input,
                        name = catalog.name,
                        type = "new",
                        amount = Pair(1, 0)
                    )
                }
            }else{
                values.forEach { item ->
                    result += item
                }
                result += BorkInventoryItem(
                    cellId = cellId!!.toInt(),
                    goodsId = -1,
                    catalogId = -1,
                    supplierId = 1,
                    barcode = input,
                    name = input,
                    type = "none",
                    amount = Pair(1, 0)
                )
            }
            result
        }

    )*/
    val getInventoryDialog: Map<Int, (dynamicContainer: LinearLayout, dialog: AlertDialog, position: Int, item: IInventoryItem, context: Context, viewModel: InventoryViewModel, catalogs: MutableList<CatalogItem>)-> Unit> = mapOf(
        0 to { dynamicContainer, dialog, position, item, context, viewModel, catalogs ->
            // <editor-fold desc="init dialog">
            dynamicContainer.orientation = LinearLayout.VERTICAL
            val etName = EditText(context)
            val tvName = TextView(context)
            val etТЕ = EditText(context)
            val tvТЕ = TextView(context)
            val etBarcode = EditText(context)
            val tvBarcode = TextView(context)
            val etExpiration = EditText(context)
            val tvExpiration = TextView(context)
            val etAmount = EditText(context)
            val tvAmount = TextView(context)
            if(item is AtomyInventoryItem) {
                tvName.setText("Название товара")
                etName.hint = "Введите название товара"
                tvТЕ.setText("ТЕ паллета")
                etТЕ.setText(item.TE)
                etТЕ.hint = "Введите ТЕ паллета"
                tvBarcode.setText("Шк товара")
                etBarcode.hint = "Введите шк товара"
                tvExpiration.setText("Срок годности")
                etExpiration.hint = "Введите окончание срока годности"
                tvAmount.setText("Количество")
                etAmount.hint = "Введите количество товара"
            }
            dynamicContainer.addView(tvName)
            dynamicContainer.addView(etName)
            dynamicContainer.addView(tvТЕ)
            dynamicContainer.addView(etТЕ)
            dynamicContainer.addView(tvBarcode)
            dynamicContainer.addView(etBarcode)
            dynamicContainer.addView(tvExpiration)
            dynamicContainer.addView(etExpiration)
            dynamicContainer.addView(tvAmount)
            dynamicContainer.addView(etAmount)
            // </editor-fold>

            //TODO make validation that field isn't empty
            val saveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveBtn.setOnClickListener {
                if (etТЕ.text.isBlank() || etAmount.text.isBlank()  || etBarcode.text.isBlank() || etExpiration.text.isBlank()) {
                    Toast.makeText(context, "Заполните все поля!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val updatedItem = AtomyInventoryItem(
                    TE = etТЕ.text.toString(),
                    barcode = etBarcode.text.toString(),
                    date =  etExpiration.text.toString(),
                    type = "refac",
                    amount = Pair(etAmount.text.toString().toIntOrNull() ?: 0, 0),
                    supplierId = 0,
                    goodsId = 0,
                    cellId = 0,
                    catalogId = 0
                )
                viewModel.updateItemsAt(updatedItem, position) // Обновление элемента адаптера

                Toast.makeText(context, "Изменения сохранены!", Toast.LENGTH_SHORT).show()
                dialog.dismiss() // Закрытие диалога после успешного сохранения

            }
        },
        1 to { dynamicContainer, dialog, position, item, context, viewModel, catalogs ->
            dynamicContainer.orientation = LinearLayout.VERTICAL
            var isNew = false
            val etName = EditText(context)
            etName.requestFocus()
            val tvName = TextView(context)
            val etFirstBarcode = EditText(context)
            val tvFirstBarcode = TextView(context)
            val etSecondBarcode = EditText(context)
            val tvSecondBarcode = TextView(context)
            val etAmount = EditText(context)
            val tvAmount = TextView(context)
            if(item is BorkInventoryItem) {
                tvName.setText("Название товара")
                etName.hint = "Введите название товара"
                tvFirstBarcode.setText("Шк товара")
                etFirstBarcode.setText(item.barcode)
                etFirstBarcode.hint = "Введите шк товара"
                tvAmount.setText("Количество")
                etAmount.hint = "Введите количество товара"
                etAmount.setText("${item.amount.first}")
            }
            // <editor-fold desc="autoCompleteTV set">
            var autoCompleteTV = AutoCompleteTextView(context)
            var adapter = CatalogAdapter(
                context,
                catalogs

            )
            var selectedItem : CatalogItem = CatalogItem(0, "dummy data")
            autoCompleteTV.setOnItemClickListener{ parent, view, position, id ->
                selectedItem = adapter.getItem(position)
                etName.setText(selectedItem.name)
                etName.isEnabled = false
                isNew = false
                etFirstBarcode.setText(item.barcode)
                if(item is BorkInventoryItem){
                    etSecondBarcode.setText(item.barcode)
                }else{
                    etSecondBarcode.setText(item.barcode)
                }
            }
            autoCompleteTV.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // Вызывается перед изменением текста
                }
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    etName.isEnabled = true
                    isNew = true
                }
                override fun afterTextChanged(s: Editable?) {
                    // Вызывается после того, как текст был изменён
                    // Здесь можно проверять итоговое значение текста
                }
            })
            autoCompleteTV.setAdapter(adapter)
            autoCompleteTV.threshold = 1
            var clearIcon = ContextCompat.getDrawable(context, R.drawable.ic_clear)
            autoCompleteTV.setCompoundDrawablesWithIntrinsicBounds(null,null, clearIcon, null)
            autoCompleteTV.setOnTouchListener { v, event ->
                val DRAWABLE_END = 2  // Index of drawableEnd
                if (event.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >= (autoCompleteTV.right - autoCompleteTV.compoundDrawables[DRAWABLE_END].bounds.width() + 100)) {
                        autoCompleteTV.text.clear()  // Clear text
                        etName.isEnabled = true
                        etName.setText("")
                        etFirstBarcode.setText("")
                        etSecondBarcode.setText("")
                        etAmount.setText("")
                        isNew = true
                        return@setOnTouchListener true
                    }
                }
                false
            }
            // </editor-fold>
            dynamicContainer.addView(autoCompleteTV)
            dynamicContainer.addView(tvName)
            dynamicContainer.addView(etName)
            dynamicContainer.addView(tvFirstBarcode)
            dynamicContainer.addView(etFirstBarcode)
            dynamicContainer.addView(tvSecondBarcode)
            dynamicContainer.addView(etSecondBarcode)
            dynamicContainer.addView(tvAmount)
            dynamicContainer.addView(etAmount)

            //TODO make validation that field isn't empty
            val saveBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveBtn.setOnClickListener {
                if (etName.text.isBlank() || etFirstBarcode.text.isBlank() || etSecondBarcode.text.isBlank() || etAmount.text.isBlank()) {
                    Toast.makeText(context, "Заполните все поля!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val updatedItem = BorkInventoryItem(
                    catalogId = selectedItem.id,
                    cellId = item.cellId,
                    supplierId = 1,
                    goodsId = 0,
                    barcode = etFirstBarcode.text.toString(),
                    name = if (selectedItem.id == 0) etName.text.toString() else selectedItem.name,
                    amount = Pair(etAmount.text.toString().toInt(), 0),
                    type = "none"
                )
                viewModel.updateItemsAt(updatedItem, position) // Обновление элемента адаптера

                Toast.makeText(context, "Изменения сохранены!", Toast.LENGTH_SHORT).show()
                dialog.dismiss() // Закрытие диалога после успешного сохранения
            }
        }
    )
/*    val getListInventoryItem: Map<Int,suspend (db: MainDB, cell: Cell, context:Context)-> List<IInventoryItem>> = mapOf(
        0 to{ db, cell, context ->
            var listJson = HelperFunction.retryRequest(context){client.getAllAtomyGoodsByCellId(ip, cell.id.toString())}
            var list  = getGoodsAtomyFromJsonArray(listJson)


            var newList = list.map { item ->
                val catalogJson = HelperFunction.retryRequest(context){client.getAtomyCatalogById(ip, item.catalogId.toString())}
                var catalog = getCatalogAtomyFromJsonObject(catalogJson)

                AtomyInventoryItem(
                    catalogId = convertToInt(catalog.id),
                    goodsId = convertToInt(item.Id),
                    cellId = item.cellId,
                    TE = item.TE,
                    barcode = catalog.firstBarcode,
                    date =  item.date,
                    type = "default",
                    supplierId = 0,
                    amount = Pair(0,item.amount)
                )
            }
            newList
        },
        1 to{ db, cell, context  ->
            var listJson = HelperFunction.retryRequest(context){ client.getAllBorkGoodsByCellId(ip, cell.id.toString())}
            var list  = getGoodsBorkFromJsonArray(listJson)
            var newList =list.map { item ->
                val catalogJson = HelperFunction.retryRequest(context){ client.getBorkCatalogById(ip, item.catalogId.toString())}
                var catalog = getCatalogBorkFromJsonObject(catalogJson)
                var barcodesJson = HelperFunction.retryRequest(context){ client.getBorkBarcodeByCatalogId(ip, catalog.id.toString())}
                var barcodes = getBarcodeBorkFromJsonArr(barcodesJson)
                var barcode = barcodes.firstOrNull{ barcode ->
                    barcode.catalogId == catalog.id
                }
                BorkInventoryItem(
                    catalogId = convertToInt(catalog.id),
                    goodsId = convertToInt(item.Id),
                    cellId = item.cellId,
                    name = catalog.name.toString(),
                    barcode = barcode!!.name,
                    type = "default",
                    supplierId = 1,
                    amount = Pair(0,item.amount)
                )
            }
            newList
        }
    )*/
    // </editor-fold>
    // <editor-fold desc="ui code">
    val getDialogForDefaultGoods: Map<Int, (context: Context, inventoryItem: IInventoryItem,  position: Int, viewModel :InventoryViewModel)-> AlertDialog.Builder> = mapOf(
        0 to { context, item,  position,viewModel ->
            // <editor-fold desc="initDialog">
            val container = LinearLayout(context)
            container.orientation = LinearLayout.VERTICAL

            val etName = EditText(context)
            val tvName = TextView(context)
            val etTe = EditText(context)
            val tvTe = TextView(context)
            val etFirstBarcode = EditText(context)
            val tvFirstBarcode = TextView(context)
            val etExpiration = EditText(context)
            val tvExpiration = TextView(context)
            val etAmount = EditText(context)
            val tvAmount = TextView(context)
            if(item is AtomyInventoryItem) {
                tvName.setText("Название товара")
                etName.setText("")
                tvTe.setText("TE товара")
                etTe.setText(item.TE)
                tvFirstBarcode.setText("Шк упаковки")
                etFirstBarcode.setText(item.barcode)
                tvExpiration.setText("Окончание срока годности")
                etExpiration.setText(item.date)
                etAmount.setText("${item.amount.first}")
                tvAmount.setText("Количество")
                container.addView(tvName)
                container.addView(etName)
                container.addView(tvTe)
                container.addView(etTe)
                container.addView(tvFirstBarcode)
                container.addView(etFirstBarcode)
                container.addView(tvExpiration)
                container.addView(etExpiration)
                container.addView(tvAmount)
                container.addView(etAmount)
            }
            // </editor-fold>
            AlertDialog.Builder(context)
                .setView(container)
                .setPositiveButton("Save") { dialog: DialogInterface, _: Int ->
                    // Save the new text

                    var inventoryItem = AtomyInventoryItem(
                        goodsId = item.goodsId,
                        cellId = item.cellId,
                        catalogId = item.catalogId,
                        supplierId = item.supplierId,
                        TE = etTe.text.toString(),
                        barcode = etFirstBarcode.text.toString(),
                        date = etExpiration.text.toString(),
                        type = item.type,
                        amount = Pair(etAmount.text.toString().toInt(), item.amount.second)
                    )

                    viewModel.updateItemsAt(inventoryItem, position)
                    Toast.makeText(context, "Изменения сохраненны!", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel") { dialog: DialogInterface, _: Int ->
                    dialog.cancel()
                }

        },
        1 to { context, item, position,viewModel ->
            // <editor-fold desc="initDialog">
            val container = LinearLayout(context)
            container.orientation = LinearLayout.VERTICAL

            val etName = EditText(context)
            val tvName = TextView(context)
            val etFirstBarcode = EditText(context)
            val tvFirstBarcode = TextView(context)
            val etSecondBarcode = EditText(context)
            val tvSecondBarcode = TextView(context)
            val etAmount = EditText(context)
            val tvAmount = TextView(context)
            if(item is BorkInventoryItem) {
                etName.setText("${item.name}")
                tvName.setText("Название товара")
                tvFirstBarcode.setText("Шк товара")
                etFirstBarcode.setText("${item.barcode}")
                tvSecondBarcode.setText("Шк упаковки")
                etSecondBarcode.setText("${item.barcode}")
                etAmount.setText("${item.amount.second}")
                tvAmount.setText("Количество")
                container.addView(tvName)
                container.addView(etName)
                container.addView(tvFirstBarcode)
                container.addView(etFirstBarcode)
                container.addView(tvSecondBarcode)
                container.addView(etSecondBarcode)
                container.addView(tvAmount)
                container.addView(etAmount)
            }
            // </editor-fold>
            AlertDialog.Builder(context)
                .setView(container)
                .setPositiveButton("Save") { dialog: DialogInterface, _: Int ->
                    // Save the new text

                    var inventoryItem = BorkInventoryItem(
                        goodsId = item.goodsId,
                        cellId = item.cellId,
                        catalogId = item.catalogId,
                        supplierId = item.supplierId,
                        barcode = etFirstBarcode.text.toString(),
                        name= etName.text.toString(),
                        type = item.type,
                        amount = Pair(etAmount.text.toString().toInt(), item.amount.second)
                    )

                    viewModel.updateItemsAt(inventoryItem, position)
                    Toast.makeText(context, "Изменения сохраненны!", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel") { dialog: DialogInterface, _: Int ->
                    dialog.cancel()
                }
        },
    )
    val getRedUiForGoods: Map<Int, (holder: InventoryViewHolder, context : Context, item: IInventoryItem)-> InventoryViewHolder> = mapOf(
        0 to { holder, context, item ->
            if (item is AtomyInventoryItem){
            holder.tvLeft.text = item.TE
            holder.tvLeft.setTextColor(ContextCompat.getColor(context, R.color.regularRed))
            holder.tvRight.text = "${item.amount.first}/${item.amount.second}"
            holder.tvRight.setTextColor(ContextCompat.getColor(context, R.color.regularRed))
                }
            holder

        },
        1 to { holder, context, item ->
            if (item is BorkInventoryItem){
                holder.tvLeft.text = item.name
                holder.tvLeft.setTextColor(ContextCompat.getColor(context, R.color.regularRed))
                holder.tvRight.text = "${item.amount.first}/${item.amount.second}"
                holder.tvRight.setTextColor(ContextCompat.getColor(context, R.color.regularRed))
            }
            holder
        }

    )
    val getBlackUiForGoods: Map<Int, (holder: InventoryViewHolder, context : Context, item: IInventoryItem)-> InventoryViewHolder> = mapOf(
        0 to { holder, context, item ->
            if(item is AtomyInventoryItem) {
                holder.tvLeft.text = item.TE
                holder.tvLeft.setTextColor(ContextCompat.getColor(context, R.color.black))
                holder.tvRight.text = "${item.amount.first}/${item.amount.second}"
                holder.tvRight.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
            holder
        },
        1 to { holder, context, item ->
            if(item is BorkInventoryItem) {
            holder.tvLeft.text = item.name
            holder.tvLeft.setTextColor(ContextCompat.getColor(context, R.color.black))
            holder.tvRight.text = "${item.amount.first}/${item.amount.second}"
            holder.tvRight.setTextColor(ContextCompat.getColor(context, R.color.black))
        }
            holder
        }
    )
    // </editor-fold>
    // <editor-fold desc="move code">
   /* val getMoveSessionItem: Map<Int,(id: Int, name: String, count: Pair<Int, Int>)->MoveSessionItem> = mapOf(
        0 to { id, name, count ->
            MoveSessionItem(Triple(id ?: 0,name , count), false)
        },
       1 to { id, name, count ->
           MoveSessionItem(Triple(id ?: 0,name , count), false)
       }
    )*/
   /* val getMoveSessionItems: Map<Int,suspend (db: MainDB, supplier: Int, cell:JSONObject, context:Context) -> List<MoveSessionItem>> = mapOf(
       *//* 0 to { db, supplier, cell, context ->
            var cellId = cell["id"].toString()
            var listOfItems :MutableList<MoveItem> = mutableListOf()
            var result =HelperFunction.retryRequest(context){client.getAllAtomyGoodsByCellId(ip, cellId)}
            for(i in 0 until result.length()){
                val id = result.getJSONObject(i)["id"].toString().toInt()
                val TE = result.getJSONObject(i)["TE"].toString()
                val amount = result.getJSONObject(i)["amount"].toString().toInt()
                var func = getMoveItem[supplier]
                var moveItem = func!!.invoke( id, TE, Pair(0, amount))
                listOfItems.add(moveItem)
            }
                //TODO make after creating session and moveElement
            listOfItems.toList()
        },*//*
      *//*  1 to { db, supplier, cell, context ->
            var cellId = cell["id"].toString()
            var listOfItems :MutableList<MoveItem> = mutableListOf()
            var result = HelperFunction.retryRequest(context){client.getAllBorkGoodsByCellId(ip, cellId)}
            for(i in 0 until result.length()){
                val id = result.getJSONObject(i)["id"].toString().toInt()
                val catalogId = result.getJSONObject(i)["catalogId"].toString()
                val amount = result.getJSONObject(i)["amount"].toString().toInt()
                var catalog = HelperFunction.retryRequest(context){client.getBorkCatalogById(ip, catalogId)}
                var func = getMoveItem[supplier]
                var moveItem = func!!.invoke( id, catalog["name"].toString(), Pair(0, amount))
                listOfItems.add(moveItem)
            }
            //TODO make after creating session and moveElement
            listOfItems.toList()
        }*//*
    )*/
    /*val getUpdatedMoveSessionItems: Map<Int, suspend (db: MainDB, supplier: Int, list: MutableList<MoveSessionItem>?, text: String, context:Context) -> List<MoveSessionItem>> = mapOf(
       *//* 0 to {db, supplier, list, text, context ->
            val listNew = list!!.map { item ->
                //var goods = db.getDao().getGoodsAtomy(item.item.first)
                var updatedMoveItem = getUpdatedAtomyMoveItem(item.item.first, text,item.item.second, item)
                updatedMoveItem
            }
            listNew
        },
        1 to {db, supplier, list, text, context ->
            val list = list!!.map { item ->
                var barcodeObj = HelperFunction.retryRequest(context){client.getBorkBarcodeByName(ip, text)}
                var catalog = HelperFunction.retryRequest(context){client.getBorkCatalogById(ip, barcodeObj["catalogId"].toString())}
                var updatedMoveItem = getUpdatedBorkMoveItem(item.item.first, item.item.second,catalog["name"].toString(), item)
                updatedMoveItem
            }
            list
        }*//*
    )*/
/*    val MoveItems: Map<Int, suspend (movingItem: List<MoveItem>, db: MainDB, text: String, context: Context) -> List<MoveItem>> = mapOf(
        0 to { moveItem, db, text, context ->
            val result: MutableList<MoveItem> = mutableListOf()
            var cell = HelperFunction.retryRequest (context){ client.getCellByName(ip, text)}
            var cellId = -1
            if (cell.length() == 0) {
                val json = HelperFunction.retryRequest(context){client.sendCell(ip, text)}
                cellId = json["id"].toString().toInt()
            }else{
                cellId = cell["id"].toString().toInt()
            }

            moveItem.forEach { movingItem ->
                var goodsJson = HelperFunction.retryRequest(context){client.getAtomyGoodsById(ip, movingItem.item.first.toString())}
                var goods = getGoodsAtomyFromJsonObject(goodsJson)
                HelperFunction.retryRequest(context){client.updateAtomyGoods(ip, goods.copy(cellId=cellId))}
            }
             result
        },
        1 to { moveItem, db, text, context ->
            val result: MutableList<MoveItem> = mutableListOf()
            var cell = HelperFunction.retryRequest(context){client.getCellByName(ip, text)}
            var cellId = -1
            if (cell.length() == 0) {
                val json = HelperFunction.retryRequest(context){client.sendCell(ip, text)}
                cellId = json["id"].toString().toInt()
            }else{
                cellId = cell["id"].toString().toInt()
            }

            var goodsInMoveJSON = HelperFunction.retryRequest(context){client.getAllBorkGoodsByCellId(ip, cellId.toString())}
            var goodsInMove= getGoodsBorkFromJsonArray(goodsInMoveJSON)

            moveItem.forEach { movingItem ->
                val changingItemJson = HelperFunction.retryRequest(context){client.getBorkGoodsById(ip, movingItem.item.first.toString())}
                var changingItem = getGoodsBorkFromJsonObject(changingItemJson)

                var suppliementGoods = goodsInMove.firstOrNull{goods -> goods.catalogId == changingItem.catalogId} // найти товары, которые соответсвуют перемещаемым чтобы сплюсовать количество
                if (movingItem.item.third.first == movingItem.item.third.second) { // если равны то не нужно создавать новый товар
                    if(goodsInMove.size != 0 &&  suppliementGoods!= null){
                        suppliementGoods.amount = suppliementGoods.amount + changingItem.amount
                        HelperFunction.retryRequest(context){client.updateBorkGoods(ip, suppliementGoods)}
                        var stockCellId = client.getCellByName(ip, "Z999")["id"].toString()
                        HelperFunction.retryRequest(context){client.updateBorkGoods(ip, changingItem.copy(cellId = stockCellId.toInt(), amount = 0))}
                    }else{
                        HelperFunction.retryRequest(context){client.updateBorkGoods(ip, changingItem.copy(cellId = cellId))}
                    }

                } else {// иначе создается новый товар
                    val moveGoods = GoodsBork(
                        null,
                        changingItem.catalogId,
                        cellId!!,
                        movingItem.item.third.first,
                        LocalDateTime.now().toString()
                    )
                    val stayGoods = GoodsBork(
                        changingItem.Id,
                        changingItem.catalogId,
                        changingItem.cellId,
                        movingItem.item.third.second - movingItem.item.third.first,
                        changingItem.createdAt
                    )
                    if(goodsInMove.size != 0 &&  suppliementGoods!= null){
                        suppliementGoods.amount = suppliementGoods.amount + movingItem.item.third.first
                        HelperFunction.retryRequest(context){client.updateBorkGoods(ip, suppliementGoods)}
                        HelperFunction.retryRequest(context){client.updateBorkGoods(ip, stayGoods)}
                    }else{
                        HelperFunction.retryRequest(context){client.sendGoodsBork(ip, moveGoods)}
                        HelperFunction.retryRequest(context){client.updateBorkGoods(ip,changingItem.copy(amount = movingItem.item.third.second - movingItem.item.third.first))}
                    }

                    result += MoveItem(Triple(stayGoods.Id!!, movingItem.item.second, Pair(0, movingItem.item.third.second - movingItem.item.third.first)),false)
                }
            }
             result
        }
    )*/
    // </editor-fold>
    // <editor-fold desc="commented code">
    /*val getDisplayedGoods: Map <Int, (Catalog, Goods)-> Triple<String, String, Pair<Int, Int>>> = mapOf(
        1 to { item1, item2 -> Triple(
        item1.name,
        "${item1.firstBarcode} ${item1.secondBarcode} ${item2.Id} false false ${item1.id}",
        Pair(0, item2.amount)
        )},
        2 to { item1, item2 -> Triple(
            item1.name,
            "${item1.firstBarcode} ${item1.secondBarcode} ${item2.Id} false false ${item1.id}",
            Pair(0, item2.amount)
        )}
    )*/
   /* val updateDB: Map<Int, (inventoryItem: InventoryItem, db: MainDB, typeOp: String, cell: String, viewModelScope: CoroutineScope) -> Unit> = mapOf(
        1 to { inventoryItem, db, typeOp, cell, coroutine ->
            coroutine.launch {
                withContext(Dispatchers.IO)
                {
                    var catalogItem: Catalog = Catalog(null, "", "", "", 0)
                    var splittedSecond = inventoryItem.item.second.split(" ")
                    if (splittedSecond.size == 5) {// catalog item need create
                        catalogItem = Catalog(
                            null,
                            inventoryItem.item.first,
                            inventoryItem.item.second.split(" ")[0],
                            inventoryItem.item.second.split(" ")[1],
                            1
                        )
                        var catalogId = db.getDao().insertCatalog(catalogItem)
                        catalogItem.id = catalogId.toInt()
                    } else {//it's mean that item selected from catalog
                        catalogItem = db.getDao().getCatalog(splittedSecond[5].toInt())
                        catalogItem.firstBarcode = inventoryItem.item.second.split(" ")[0]
                        catalogItem.secondBarcode = inventoryItem.item.second.split(" ")[1]
                    }
                    var cellItem: Cell? = null
                    cellItem = db.getDao().getAllCells()
                        .firstOrNull { innerCell -> innerCell.name == cell }
                    if (cellItem == null) {
                        db.getDao().insertCell(Cell(null, cell))
                        cellItem = db.getDao().getAllCells()
                            .firstOrNull { innerCell -> innerCell.name == cell }
                    }
                    var newGoods = Goods(
                        null,
                        convertToInt(catalogItem?.id),
                        convertToInt(cellItem?.id),
                        inventoryItem.item.third.first
                    )
                    db.getDao().insertGoods(newGoods)
                    db.getDao().updateCatalog(catalogItem)
                    if (inventoryItem.item.second.split(" ")[4].toBoolean()) {
                        db.getDao().updateCatalog(
                            catalogItem.copy(
                                name = inventoryItem.item.first,
                                firstBarcode = inventoryItem.item.second.split(" ")[0],
                                secondBarcode = inventoryItem.item.second.split(" ")[1]
                            )
                        )
                    }

                }
            }
        },
        2 to { inventoryItem, db, typeOp, cell, coroutine ->
            coroutine.launch {
                withContext(Dispatchers.IO)
                {
                    var catalogItem: Catalog = Catalog(null, "", "", "", 0)
                    var splittedSecond = inventoryItem.item.second.split(" ")
                    if (splittedSecond.size == 5) {// catalog item need create
                        catalogItem = Catalog(
                            null,
                            inventoryItem.item.first,
                            inventoryItem.item.second.split(" ")[0],
                            inventoryItem.item.second.split(" ")[1],
                            2
                        )
                        var catalogId = db.getDao().insertCatalog(catalogItem)
                        catalogItem.id = catalogId.toInt()
                    } else {//it's mean that item selected from catalog
                        catalogItem = db.getDao().getCatalog(splittedSecond[5].toInt())
                    }
                    var cellItem: Cell? = null
                    cellItem = db.getDao().getAllCells()
                        .firstOrNull { innerCell -> innerCell.name == cell }
                    if (cellItem == null) {
                        db.getDao().insertCell(Cell(null, cell))
                        cellItem = db.getDao().getAllCells()
                            .firstOrNull { innerCell -> innerCell.name == cell }
                    }
                    var newGoods = Goods(
                        null,
                        convertToInt(catalogItem?.id),
                        convertToInt(cellItem?.id),
                        inventoryItem.item.third.first
                    )
                    db.getDao().insertGoods(newGoods)
                    if (inventoryItem.item.second.split(" ")[4].toBoolean()) {
                        db.getDao().updateCatalog(
                            catalogItem.copy(
                                name = inventoryItem.item.first,
                                firstBarcode = inventoryItem.item.second.split(" ")[0],
                                secondBarcode = inventoryItem.item.second.split(" ")[1]
                            )
                        )
                    }

                }
            }
        }
    )*/
    /* val updatedCollection: Map<Int, (inventoryItem: InventoryItem, barcode: String)-> Pair<Triple<String,String, Pair<Int,Int>>, Boolean>> = mapOf(
     1 to { inventoryItem, barcode ->
         var result =  Pair(Triple(inventoryItem.item.first,
             inventoryItem.item.second,
             Pair(
                 inventoryItem.item.third.first,
                 inventoryItem.item.third.second)), true)
         if (inventoryItem.item.second.split(" ")[0] == barcode || inventoryItem.item.second.split(" ")[1] == barcode) {
             result =  result.copy(first = Triple(inventoryItem.item.first,
                 inventoryItem.item.second,
                 Pair(
                     inventoryItem.item.third.first + 1,
                     inventoryItem.item.third.second)), second = false)
         }
         result
     },
     2 to { inventoryItem, barcode ->
         var result =  Pair(Triple(inventoryItem.item.first,
             inventoryItem.item.second,
             Pair(
                 inventoryItem.item.third.first,
                 inventoryItem.item.third.second)), true)
         if (inventoryItem.item.first.split(" ")[1] == barcode) {
             result = result.copy(first = Triple(inventoryItem.item.first,
                 inventoryItem.item.second,
                 Pair(
                     inventoryItem.item.third.first + 1,
                     inventoryItem.item.third.second)), second = false)
         }
         result
     }

 )*/
    /* val getNewGoods: Map<Int, (barcode: String, catalog: Catalog) -> Pair<InventoryItem, Boolean>> = mapOf(
         1 to { barcode, catalog ->
             var result = Pair(InventoryItem(Triple(
                 catalog.name,
                 "${catalog.firstBarcode} ${catalog.secondBarcode} new false false ${catalog.id}",
                 Pair(1, 0)
             )), false)
             if (catalog.firstBarcode == barcode || catalog.secondBarcode == barcode) {
                 result = result.copy(second = true)
             }
             result
         },
         2 to { barcode, catalog ->
             var result = Pair(InventoryItem(Triple(
                 catalog.name,
                 "${catalog.firstBarcode} ${catalog.secondBarcode} new false false ${catalog.id}",
                 Pair(1, 0)
             )), false)
             if (catalog.name.split(" ")[1] == barcode) {
                 result = result.copy(second = true)
             }
             result
         }
     )*/
    // </editor-fold>
}

// <editor-fold desc="helper method">
fun convertToInt(nullableInt: Int?): Int {
    return nullableInt ?: 0  // If nullableInt is null, use 0 as default
}
/*fun getUpdatedAtomyMoveItem(id: Int?, left: String, catalogName: String, moveSessionItem: MoveSessionItem) : MoveSessionItem {
    if(catalogName.contains(left) && moveSessionItem.item.third.first < moveSessionItem.item.third.second){
        return MoveSessionItem(Triple(id ?: 0, catalogName , Pair(moveSessionItem.item.third.first + 1,moveSessionItem.item.third.second)), true)
    }else{
      return MoveSessionItem(Triple(id ?: 0, catalogName, Pair(moveSessionItem.item.third.first, moveSessionItem.item.third.second)), false)
    }
}
fun getUpdatedBorkMoveItem(id: Int?, left: String, catalogName: String, moveSessionItem: MoveSessionItem) : MoveSessionItem {
    if(catalogName.contains(left) && moveSessionItem.item.third.first < moveSessionItem.item.third.second){
        return MoveSessionItem(Triple(id ?: 0, left , Pair(moveSessionItem.item.third.first + 1,moveSessionItem.item.third.second)), true)
    }else{
        return MoveSessionItem(Triple(id ?: 0, left, Pair(moveSessionItem.item.third.first, moveSessionItem.item.third.second)), false)
    }
}*/
/*fun getGoodsBorkFromJsonArray(list: JSONArray) : List<GoodsBork>{
    var result: MutableList<GoodsBork> = mutableListOf()
    for(i in 0 until list.length()){
        var obj = list.getJSONObject(i)
        result.add(getGoodsBorkFromJsonObject(obj))
    }
    return result
}
fun getGoodsBorkFromJsonObject(obj: JSONObject) : GoodsBork{
    return GoodsBork(
        Id = obj["id"].toString().toInt(),
        cellId = obj["cellId"].toString().toInt(),
        catalogId = obj["catalogId"].toString().toInt(),
        amount =  obj["amount"].toString().toInt(),
        createdAt =  obj["createdAt"].toString()
    )
}
fun getGoodsAtomyFromJsonArray(list: JSONArray) : List<GoodsAtomy>{
    var result: MutableList<GoodsAtomy> = mutableListOf()
    for(i in 0 until list.length()){
        var obj = list.getJSONObject(i)
        result.add(getGoodsAtomyFromJsonObject(obj))
    }
    return result
}
fun getGoodsAtomyFromJsonObject(obj: JSONObject) : GoodsAtomy{
    return GoodsAtomy(
        Id = obj["id"].toString().toInt(),
        cellId = obj["cellId"].toString().toInt(),
        catalogId = obj["catalogId"].toString().toInt(),
        amount =  obj["amount"].toString().toInt(),
        createdAt =  obj["createdAt"].toString(),
        TE = obj["TE"].toString(),
        date = obj["date"].toString()
    )
}
fun getCatalogAtomyFromJsonObject(obj: JSONObject) : CatalogAtomy{
    return CatalogAtomy(
        id = obj["id"].toString().toInt(),
        name = obj["name"].toString(),
        firstBarcode = obj["firstBarcode"].toString(),
        secondBarcode =  obj["secondBarcode"].toString(),
    )
}
fun getCatalogBorkFromJsonObject(obj: JSONObject) : CatalogBork{
    return CatalogBork(
        id = obj["id"].toString().toInt(),
        name = obj["name"].toString()
    )
}
fun getBarcodeBorkFromJsonArr(list: JSONArray) : List<BarcodeBork>{
    var result: MutableList<BarcodeBork> = mutableListOf()
    for(i in 0 until list.length()){
        var obj = list.getJSONObject(i)
        result.add(getBarcodeBorkFromJsonObj(obj))
    }
    return result
}
fun getBarcodeBorkFromJsonObj(obj: JSONObject) : BarcodeBork{
    return BarcodeBork(
        id = obj["id"].toString().toInt(),
        name = obj["name"].toString(),
        type = "master",
        catalogId = obj["catalogId"].toString().toInt()
    )
}*/
// </editor-fold>