package com.example.wmswherther.data.db.Repositories

import com.example.wmsRemote.data.enums.OperationType
import com.example.wmswherther.data.db.Entityes.Barcode
import com.example.wmswherther.data.db.Entityes.Catalog
import com.example.wmswherther.data.db.Entityes.Cell
import com.example.wmswherther.data.db.Entityes.Change
import com.example.wmswherther.data.db.Entityes.Goods
import com.example.wmswherther.data.db.Entityes.IncomeItem
import com.example.wmswherther.data.db.Entityes.InventoryDiffItem
import com.example.wmswherther.data.db.Entityes.Movement
import com.example.wmswherther.data.db.Entityes.SessionIncome
import com.example.wmswherther.data.db.Entityes.SessionInventory
import com.example.wmswherther.data.db.Entityes.SessionPicker
import com.example.wmswherther.data.db.Request
import com.google.gson.Gson
import okhttp3.OkHttpClient
import org.json.JSONArray

class SyncRepository (
    private val request: Request,
    private val client: OkHttpClient
) {

    suspend fun syncPush(
        ip: String,
        operation: Change
    ) {

        when(operation.operationType) {

            OperationType.InsertBarcode.ordinal -> {

                val barcode = Gson().fromJson(
                    operation.payload,
                    Barcode::class.java
                )

                request.sendBorkBarcode(ip, barcode, client)
            }
            OperationType.InsertMovement.ordinal -> {

                val movement = Gson().fromJson(
                    operation.payload,
                    Movement::class.java
                )

                request.sendMovement(ip, movement, client)
            }
            OperationType.InsertCatalog.ordinal -> {

                val catalog = Gson().fromJson(
                    operation.payload,
                    Catalog::class.java
                )

                request.sendCatalog(ip, catalog, client)
            }
            OperationType.InsertCell.ordinal -> {

                val cell = Gson().fromJson(
                    operation.payload,
                    Cell::class.java
                )

                request.sendCell(ip, cell, client)
            }
            OperationType.InsertGoods.ordinal -> {

                val goods = Gson().fromJson(
                    operation.payload,
                    Goods::class.java
                )

                request.sendGoods(ip, goods, client)
            }
            OperationType.InsertInventoryDiff.ordinal -> {

                val inventoryDiff = Gson().fromJson(
                    operation.payload,
                    InventoryDiffItem::class.java
                )

                request.sendInventoryDiffItem(ip, inventoryDiff, client)
            }
            OperationType.InsertInventorySession.ordinal -> {

                val inventorySession = Gson().fromJson(
                    operation.payload,
                    SessionInventory::class.java
                )

                request.sendInventorySession(ip, inventorySession, client)
            }
            OperationType.UpdateCell.ordinal -> {

                val cell = Gson().fromJson(
                    operation.payload,
                    Cell::class.java
                )

                request.updateCell(ip, cell, client)
            }
            OperationType.UpdateGoods.ordinal ->{
                val goods = Gson().fromJson(
                    operation.payload,
                    Goods::class.java
                )

                request.updateGoods(ip, goods, client)
            }
            OperationType.UpdateIncomeSession.ordinal ->{
                val sessionIncome = Gson().fromJson(
                    operation.payload,
                    SessionIncome::class.java
                )

                request.updateIncomeSession(ip, sessionIncome, client)
            }
            OperationType.UpdateInventorySession.ordinal ->{
                val sessionInventory = Gson().fromJson(
                    operation.payload,
                    SessionInventory::class.java
                )

                request.updateInventorySession(ip, sessionInventory, client)
            }
            OperationType.UpdatePickerSession.ordinal ->{
                val pickerSession = Gson().fromJson(
                    operation.payload,
                    SessionPicker::class.java
                )

                request.updateAssemblySession(ip, pickerSession, client)
            }
        }
    }
    suspend fun syncPull(
        ip: String,
        operation: List< Pair<String, Long>>
    ) : List<JSONArray>{
        var result : MutableList<JSONArray> = mutableListOf<JSONArray>()
       for (dataType in operation){
           when(dataType.first){
               "Catalog" -> result += request.getCatalogs(ip, client, dataType.second)
               "Goods" -> result += request.getGoods(ip, client, dataType.second)
               "Barcode" -> result += request.getBarcodes(ip, client, dataType.second)
               "Cell" -> result += request.getCells(ip, client, dataType.second)
               "CellType" -> result += request.getCellTypes(ip, client, dataType.second)
               "Credential" -> result += request.getCredentials(ip, client, dataType.second)
               "IncomeItem" -> result += request.getIncomeItem(ip, client, dataType.second)
               "InventoryDiffItem" -> result += request.getInventoryDiffItem(ip, client, dataType.second)
               "Movement" -> result += request.getMovement(ip, client, dataType.second)
               "OutcomeItem" -> result += request.getOutcomeItem(ip, client, dataType.second)
               "PackageEntity" -> result += request.getPackageEntity(ip, client, dataType.second)
               "PickerItem" -> result += request.getPickerItems(ip, client, dataType.second)
               "Service" -> result += request.getService(ip, client, dataType.second)
               "SessionIncome" -> result += request.getSessionIncome(ip, client, dataType.second)
               "SessionInventory" -> result += request.getSessionInventory(ip, client, dataType.second)
               "SessionOutcome" -> result += request.getSessionOutcome(ip, client, dataType.second)
               "SessionPicker" -> result += request.getSessionPicker(ip, client, dataType.second)
               "Supplier" -> result += request.getSuppliers(ip, client, dataType.second)
               "TrueSign" -> result += request.getTrueSign(ip, client, dataType.second)
               "User" -> result += request.getUsers(ip, client, dataType.second)


           }
       }
        return  result
    }
}