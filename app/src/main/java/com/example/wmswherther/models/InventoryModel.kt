package com.example.wmsRemote.models

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.wmsRemote.Adapters.AdapterHelper
import com.example.wmsRemote.Classes.IInventoryItem
import com.example.wmsRemote.InventoryActivity
import com.example.wmsRemote.data.db.Cell
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.viewModel.InventoryViewModel
import com.example.wmsRemote.viewModel.retryRequest
import com.example.wmswherther.HelperFunction
import com.example.wmswherther.data.db.Request
import kotlinx.coroutines.CoroutineScope

// <editor-fold desc="main methods">

var client = Request()
var ip = "192.168.6.208"
suspend fun processInputBarcode(
    input: String,
    db: MainDB,
    values: List<IInventoryItem>?,
    viewModelScope: CoroutineScope,
    supplier: Int,
    cell: String,
    context: Context
): List<IInventoryItem> {
    if (values != null) {
        var func = AdapterHelper.getProcessedInventoryItem[supplier]
        val result = func!!.invoke(values, input, db, cell, context)
        return result
    }
    return listOf()
}
suspend fun processInputCell(
    input: String,
    db: MainDB,
    viewModel: InventoryViewModel,
    context:Context
): List<IInventoryItem> {
    val inventoryList : List<IInventoryItem> = listOf()
    var cell = HelperFunction.retryRequest(context){client.getCellByName(ip, input)}

    if(cell.length() == 0) {
        if(isCell(input)){
            HelperFunction.retryRequest(context){client.sendCell(ip, input)}
        }
        return listOf()
    }
    else{
        val func =  AdapterHelper.getListInventoryItem[viewModel.supplier]
        var list = func!!.invoke(db,Cell(id = cell["id"].toString().toInt(), cell["name"].toString()), context)
        return list
    }
}
@RequiresApi(Build.VERSION_CODES.O)
suspend fun processSaveBtn(
    db: MainDB,
    values: List<IInventoryItem>?,
    cell: String?,
    inventoryActivity: InventoryActivity,
    context: Context) : List<IInventoryItem>? {
    var result : List<IInventoryItem> = listOf()
    val cellId = getCellId(db, cell, context)

    for (inventoryItem in values!!){
        if (inventoryItem.type == "default" || inventoryItem.type == "new" || inventoryItem.type == "refac") {
            var newItem = when {
                inventoryItem.amount.first == 0 -> removeInventoryItem(inventoryItem, db, context)
                inventoryItem.amount.first >= 1 -> changeInventoryItem(
                    inventoryItem,
                    db,
                    cellId.toString(),
                    context
                )

                else -> null
            }
            if (newItem != null) {
                result += newItem
            }
        } else {
            var func = AdapterHelper.prepareNoneTypeInventoryItem[inventoryItem.supplierId]
            var item = func!!.invoke(inventoryItem, db, cellId, context)
            if (item != null) {
                result += item
            } else {
                return null
            }
        }
    }

    return result
}
// </editor-fold>
// <editor-fold desc="helper Methods">
suspend fun getCellId(db: MainDB, cell: String?, context: Context): Int? {
    var cellId = -1
    var cell =HelperFunction.retryRequest(context){client.getCellByName(ip, cell.toString())}
    if(cell.length() == 0){
        var newCell = HelperFunction.retryRequest(context) { client.sendCell(ip, cell.toString())}
    }else{
        cellId = cell["id"].toString().toInt()
    }
    return  cellId
}
suspend fun changeInventoryItem(
    inventoryItem: IInventoryItem,
    db: MainDB,
    cell: String,
    context: Context
) : IInventoryItem? {
    var func = AdapterHelper.changeInventoryItem[inventoryItem.supplierId]
    var result = func!!.invoke(inventoryItem, db, cell, context)
    return result
}
suspend fun removeInventoryItem(
    inventoryItem: IInventoryItem,
    db: MainDB,
    context: Context
) : IInventoryItem? {
    var func = AdapterHelper.removeInventoryItem[inventoryItem.supplierId]
    func!!.invoke(inventoryItem, db, context)

    return null
}
private fun isCell(cell: String): Boolean {
    if (cell.length == 4 && cell[0] in 'A' .. 'Z' && cell[1].isDigit() && cell[2].isDigit() && cell[3].isDigit()){
        return true
    }
    return false
}
// </editor-fold>