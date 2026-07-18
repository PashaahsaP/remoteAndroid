package com.example.wmswherther.data.db.Repositories

import android.util.Log
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
import com.example.wmswherther.data.db.PullItem
import com.example.wmswherther.data.db.Request
import com.example.wmswherther.data.enums.Entities
import com.google.gson.Gson
import kotlinx.coroutines.awaitAll
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

                request.sendBarcode(ip, barcode, client)
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

                //update change
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
        operation: List< PullItem>
    ) : List<Pair<Entities,JSONArray>>{
        var result   = mutableListOf<Pair<Entities,JSONArray>>()
       for (dataType in operation){
           when(dataType.entity){
               Entities.Catalog -> result += Pair(Entities.Catalog, request.getCatalogs(ip, client, dataType.time))
               Entities.Goods -> result += Pair(Entities.Goods, request.getGoods(ip, client, dataType.time))
               Entities.Barcode -> result += Pair(Entities.Barcode,  request.getBarcodes(ip, client, dataType.time))
               Entities.Cell -> result += Pair(Entities.Cell, request.getCells(ip, client, dataType.time))
               Entities.TypeCell -> result += Pair(Entities.TypeCell, request.getCellTypes(ip, client, dataType.time))
               Entities.Credential -> result += Pair(Entities.Credential, request.getCredentials(ip, client, dataType.time))
               Entities.IncomeItem -> result += Pair(Entities.IncomeItem,  request.getIncomeItem(ip, client, dataType.time))
               Entities.InventoryDiffItem -> result += Pair(Entities.InventoryDiffItem, request.getInventoryDiffItem(ip, client, dataType.time))
               Entities.Movement -> result += Pair(Entities.Movement, request.getMovement(ip, client, dataType.time))
               Entities.OutcomeItem -> result += Pair(Entities.OutcomeItem, request.getOutcomeItem(ip, client, dataType.time))
               Entities.Package -> result += Pair(Entities.Package, request.getPackageEntity(ip, client, dataType.time))
               Entities.PickerItem -> result += Pair(Entities.PickerItem, request.getPickerItems(ip, client, dataType.time))
               Entities.Service -> result += Pair(Entities.Service, request.getService(ip, client, dataType.time))
               Entities.SessionIncome -> result += Pair(Entities.SessionIncome, request.getSessionIncome(ip, client, dataType.time))
               Entities.SessionInventory -> result += Pair( Entities.SessionInventory, request.getSessionInventory(ip, client, dataType.time))
               Entities.SessionOutcome -> result += Pair(Entities.SessionOutcome, request.getSessionOutcome(ip, client, dataType.time))
               Entities.SessionPicker -> result += Pair(Entities.SessionPicker, request.getSessionPicker(ip, client, dataType.time))
               Entities.Supplier -> result += Pair(Entities.Supplier, request.getSuppliers(ip, client, dataType.time))
               Entities.TrueSign -> result += Pair(Entities.TrueSign, request.getTrueSign(ip, client, dataType.time))
               Entities.User -> result += Pair(Entities.User, request.getUsers(ip, client, dataType.time))


           }
       }
        Log.d("#####", Gson().toJson(result))
        return  result
    }
}