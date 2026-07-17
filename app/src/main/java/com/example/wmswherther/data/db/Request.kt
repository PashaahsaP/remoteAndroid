package com.example.wmswherther.data.db

import android.content.Context
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.wmsRemote.models.client
import com.example.wmswherther.data.db.Entityes.Barcode
import com.example.wmswherther.data.db.Entityes.Catalog
import com.example.wmswherther.data.db.Entityes.Cell
import com.example.wmswherther.data.db.Entityes.Goods
import com.example.wmswherther.data.db.Entityes.InventoryDiffItem
import com.example.wmswherther.data.db.Entityes.Movement
import com.example.wmswherther.data.db.Entityes.SessionIncome
import com.example.wmswherther.data.db.Entityes.SessionInventory
import com.example.wmswherther.data.db.Entityes.SessionPicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.time.LocalDateTime



class Request {

    suspend fun updateIncomeSession(ip:String, sessionId: String, status: Int) : String {
        val client = OkHttpClient()
        val json = JSONObject()
            .put("id", sessionId)
            .put("status", status)
            .toString()
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("http://$ip:3000/incomeSession/updateStatus")
            .put(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                body
            }
        }
    }
    suspend fun updateInventorySession(ip:String, sessionId: String, status: Int) : String {
        val client = OkHttpClient()
        val json = JSONObject()
            .put("id", sessionId)
            .put("status", status)
            .toString()
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("http://$ip:3000/inventorySession/updateStatus")
            .put(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                body
            }
        }
    }
    suspend fun updateAssemblySession(ip:String, sessionId: String, status: Int) : String {
        val client = OkHttpClient()
        val json = JSONObject()
            .put("id", sessionId)
            .put("status", status)
            .toString()
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("http://$ip:3000/assemblySession/updateStatus")
            .put(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                body
            }
        }
    }
    suspend fun updateGoods(ip:String, goods: Goods, client: OkHttpClient) : String {
        val json = JSONObject()
            .put("id", goods.id)
            .put("amount", goods.amount)
            .put("cellId", goods.cellId)
            .put("catalogId", goods.catalogId)
            .put("createdAt", goods.createdAt)
            .put("updatedAt", goods.updatedAt)
            .put("deletedAt", goods.deletedAt)
            .put("isDeleted", goods.isDeleted)
            .put("other", goods.other)
            .toString()
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("http://$ip:3000/goods/update")
            .put(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                body
            }
        }
    }
    suspend fun updateIncomeSession(ip:String, incomeSession: SessionIncome, client: OkHttpClient) : String {
        val json = JSONObject()
            .put("id", incomeSession.id)
            .put("supplierId", incomeSession.supplierId)
            .put("incomeCellId", incomeSession.incomeCellId)
            .put("toCellId", incomeSession.toCellId)
            .put("status", incomeSession.status)
            .put("createdAt", incomeSession.createdAt)
            .put("startedAt", incomeSession.startedAt)
            .put("finishedAt", incomeSession.finishedAt)
            .put("updatedAt", incomeSession.updatedAt)
            .put("deletedAt", incomeSession.deletedAt)
            .put("isDeleted", incomeSession.isDeleted)
            .put("other", incomeSession.other)
            .toString()
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("http://$ip:3000/incomeSession/update")
            .put(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                body
            }
        }
    }
    suspend fun updateInventorySession(ip:String, inventorySession: SessionInventory, client: OkHttpClient) : String {

        val json = JSONObject()
            .put("id", inventorySession.id)
            .put("supplierId", inventorySession.supplierId)
            .put("cellId", inventorySession.cellId)
            .put("prevSessionId", inventorySession.prevSessionId)
            .put("status", inventorySession.status)
            .put("createdAt", inventorySession.createdAt)
            .put("startedAt", inventorySession.startedAt)
            .put("finishedAt", inventorySession.finishedAt)
            .put("updatedAt", inventorySession.updatedAt)
            .put("deletedAt", inventorySession.deletedAt)
            .put("isDeleted", inventorySession.isDeleted)
            .put("other", inventorySession.other)
            .toString()
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("http://$ip:3000/inventorySession/update")
            .put(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                body
            }
        }
    }
    suspend fun updateAssemblySession(ip:String, pickerSession: SessionPicker, client: OkHttpClient) : String {
        val json = JSONObject()
            .put("id", pickerSession.id)
            .put("supplierId", pickerSession.supplierId)
            .put("outCellId", pickerSession.outCellId)
            .put("status", pickerSession.status)
            .put("createdAt", pickerSession.createdAt)
            .put("finishedAt", pickerSession.finishedAt)
            .put("startedAt", pickerSession.startedAt)
            .put("updatedAt", pickerSession.startedAt)
            .put("deletedAt", pickerSession.startedAt)
            .put("isDeleted", pickerSession.startedAt)
            .put("other", pickerSession.startedAt)
            .toString()
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("http://$ip:3000/assembly_session/update")
            .put(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                body
            }
        }
    }
    suspend fun updateCell(ip: String, cell: Cell, client: OkHttpClient) {
        val json = JSONObject()
            .put("id", cell.id)
            .put("typeCellId", cell.typeCellId)
            .put("parentCellId", cell.parentCellId)
            .put("name", cell.name)
            .put("createdAt", cell.createdAt)
            .put("updatedAt", cell.updatedAt)
            .put("deletedAt", cell.deletedAt)
            .put("isDeleted", cell.isDeleted)
            .put("other", cell.other)
            .toString()
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("http://$ip:3000/cell/update")
            .put(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                body
            }
        }
    }

    suspend fun getCatalogs(ip:String, client: OkHttpClient, time: Long)  : JSONArray {

        val request = Request.Builder()
            .url("http://$ip:3000/catalogs/$time")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()

                val arr = JSONArray(body)
                arr
            }
        }
    }
    suspend fun getBarcodes(ip:String, client: OkHttpClient, time: Long) : JSONArray {
        val request = Request.Builder()
            .url("http://$ip:3000/barcodes/$time")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                val arr = JSONArray(body)
                arr
            }
        }
    }
    suspend fun getCells(ip:String, client: OkHttpClient, time: Long)  : JSONArray {
        val request = Request.Builder()
            .url("http://$ip:3000/cells/$time")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                val arr = JSONArray(body)
                arr
            }
        }
    }
    suspend fun getCredentials(ip:String, client: OkHttpClient, time: Long)  : JSONArray {
        val request = Request.Builder()
            .url("http://$ip:3000/credentials/$time")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                val arr = JSONArray(body)
                arr
            }
        }
    }
    suspend fun getIncomeItem(ip:String, client: OkHttpClient, time: Long)  : JSONArray {
        val request = Request.Builder()
            .url("http://$ip:3000/incomeItem/$time")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                val arr = JSONArray(body)
                arr
            }
        }
    }
    suspend fun getInventoryDiffItem(ip:String, client: OkHttpClient, time: Long)  : JSONArray {
        val request = Request.Builder()
            .url("http://$ip:3000/inventoryDiffItem/$time")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                val arr = JSONArray(body)
                arr
            }
        }
    }
    suspend fun getMovement(ip:String, client: OkHttpClient, time: Long)  : JSONArray {
        val request = Request.Builder()
            .url("http://$ip:3000/movement/$time")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                val arr = JSONArray(body)
                arr
            }
        }
    }
    suspend fun getCellTypes(ip:String, client: OkHttpClient, time: Long)  : JSONArray {
        val request = Request.Builder()
            .url("http://$ip:3000/cellTypes/$time")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                val arr = JSONArray(body)
                arr
            }
        }
    }
    suspend fun getOutcomeItem(ip:String, client: OkHttpClient, time: Long)  : JSONArray {
        val request = Request.Builder()
            .url("http://$ip:3000/outcomeItem/$time")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                val arr = JSONArray(body)
                arr
            }
        }
    }
    suspend fun getPackageEntity(ip:String, client: OkHttpClient, time: Long)  : JSONArray {
        val request = Request.Builder()
            .url("http://$ip:3000/packageEntity/$time")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                val arr = JSONArray(body)
                arr
            }
        }
    }
    suspend fun getPickerItems(ip:String, client: OkHttpClient, time: Long)  : JSONArray {
        val request = Request.Builder()
            .url("http://$ip:3000/pickerItems/$time")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                val arr = JSONArray(body)
                arr
            }
        }
    }
    suspend fun getService(ip:String, client: OkHttpClient, time: Long)  : JSONArray {
        val request = Request.Builder()
            .url("http://$ip:3000/service/$time")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                val arr = JSONArray(body)
                arr
            }
        }
    }
    suspend fun getSessionIncome(ip:String, client: OkHttpClient, time: Long)  : JSONArray {
        val request = Request.Builder()
            .url("http://$ip:3000/sessionsIncome/$time")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                val arr = JSONArray(body)
                arr
            }
        }
    }
    suspend fun getSessionInventory(ip:String, client: OkHttpClient, time: Long)  : JSONArray {
        val request = Request.Builder()
            .url("http://$ip:3000/sessionsInventory/$time")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                val arr = JSONArray(body)
                arr
            }
        }
    }
    suspend fun getSessionOutcome(ip:String, client: OkHttpClient, time: Long)  : JSONArray {
        val request = Request.Builder()
            .url("http://$ip:3000/sessionsOutcome/$time")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                val arr = JSONArray(body)
                arr
            }
        }
    }
    suspend fun getSessionPicker(ip:String, client: OkHttpClient, time: Long)  : JSONArray {
        val request = Request.Builder()
            .url("http://$ip:3000/sessionsPickers/$time")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                val arr = JSONArray(body)
                arr
            }
        }
    }
    suspend fun getSuppliers(ip:String, client: OkHttpClient, time: Long)  : JSONArray {
        val request = Request.Builder()
            .url("http://$ip:3000/suppliers/$time")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                val arr = JSONArray(body)
                arr
            }
        }
    }
    suspend fun getTrueSign(ip:String, client: OkHttpClient, time: Long)  : JSONArray {
        val request = Request.Builder()
            .url("http://$ip:3000/trueSign/$time")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                val arr = JSONArray(body)
                arr
            }
        }
    }
    suspend fun getUsers(ip:String, client: OkHttpClient, time: Long)  : JSONArray {
        val request = Request.Builder()
            .url("http://$ip:3000/users/$time")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                val arr = JSONArray(body)
                arr
            }
        }
    }
    suspend fun getGoods(ip:String, client: OkHttpClient, time: Long)  : JSONArray {
        val request = Request.Builder()
            .url("http://$ip:3000/goods/$time")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                val arr = JSONArray(body)
                arr
            }
        }
    }
   /* suspend fun getCellByName(ip:String, name: String) : JSONObject {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://$ip:3000/cell/name/$name")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                if(body == ""){
                    JSONObject()
                }else {
                    JSONObject(body)
                }
            }
        }
    }*/

    suspend fun sendBarcode(ip: String, barcode: Barcode, client: OkHttpClient): JSONObject {
        val json = JSONObject()
            .put("id", barcode.id)
            .put("name", barcode.name)
            .put("catalogId", barcode.catalogId)
            .put("supplierId", barcode.supplierId)
            .put("createdAt", barcode.createdAt)
            .put("updatedAt", barcode.updatedAt)
            .put("deletedAt", barcode.deletedAt)
            .put("isDeleted", barcode.isDeleted)
            .put("other", barcode.other)
            .toString()
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("http://$ip:3000/barcode/")
            .post(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                var jsonObj = JSONObject(body)
                jsonObj
            }
        }
    }
    suspend fun sendMovement(ip: String, movement: Movement, client: OkHttpClient): JSONObject {

        val json = JSONObject()
            .put("id", movement.id)
            .put("cellFromId", movement.cellFromId)
            .put("cellToId", movement.cellToId)
            .put("catalogId", movement.catalogId)
            .put("goodsId", movement.goodsId)
            .put("qty", movement.qty)
            .put("userId", movement.userId)
            .put("executedAt", movement.executedAt)
            .put("operationType", movement.operationType)
            .put("entityId", movement.entityId)
            .put("createdAt", movement.createdAt)
            .put("updatedAt", movement.updatedAt)
            .put("deletedAt", movement.deletedAt)
            .put("isDeleted", movement.isDeleted)
            .toString()
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("http://$ip:3000/movement/")
            .post(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                var jsonObj = JSONObject(body)
                jsonObj
            }
        }
    }
    suspend fun sendCatalog(ip:String, catalog: Catalog, client: OkHttpClient) : String {
        val json = JSONObject()
            .put("id", catalog.id)
            .put("name", catalog.name)
            .put("sku", catalog.sku)
            .put("supplierId", catalog.supplierId)
            .put("createdAt", catalog.createdAt)
            .put("updatedAt", catalog.updatedAt)
            .put("deletedAt", catalog.deletedAt)
            .put("isDeleted", catalog.isDeleted)
            .put("other", catalog.other)
            .toString()
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("http://$ip:3000/catalog/")
            .post(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                body
            }
        }
    }
    suspend fun sendCell(ip:String, cell: Cell, client: OkHttpClient) : JSONObject {
        val json = JSONObject()
            .put("id", cell.id)
            .put("typeCellId", cell.typeCellId)
            .put("parentCellId", cell.parentCellId)
            .put("name", cell.name)
            .put("createdAt", cell.createdAt)
            .put("updatedAt", cell.updatedAt)
            .put("deletedAt", cell.deletedAt)
            .put("isDeleted", cell.isDeleted)
            .put("other", cell.other)
            .toString()
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("http://$ip:3000/cell/")
            .post(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                var jsonObj = JSONObject(body)
                jsonObj
            }
        }
    }
    suspend fun sendGoods(ip:String, goods: Goods, client: OkHttpClient) : JSONObject {
        val json = JSONObject()
            .put("id", goods.id)
            .put("amount", goods.amount)
            .put("cellId", goods.cellId)
            .put("catalogId", goods.catalogId)
            .put("createdAt", goods.createdAt)
            .put("isAvailable", goods.isAvailable)
            .put("updatedAt", goods.updatedAt)
            .put("deletedAt", goods.deletedAt)
            .put("isDeleted", goods.isDeleted)
            .put("other", goods.other)
            .toString()
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("http://$ip:3000/goods/")
            .post(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                var jsonObj = JSONObject(body)
                jsonObj
            }
        }
    }
    suspend fun sendInventoryDiffItem(ip:String, inventoryDiffItem: InventoryDiffItem, client: OkHttpClient) : JSONObject {

        val json = JSONObject()
            .put("id", inventoryDiffItem.id)
            .put("inventorySessionId", inventoryDiffItem.inventorySessionId)
            .put("catalogId", inventoryDiffItem.catalogId)
            .put("barcode", inventoryDiffItem.barcode)
            .put("isTE", inventoryDiffItem.isTE)
            .put("parentCellId", inventoryDiffItem.parentCellId)
            .put("diffCount", inventoryDiffItem.diffCount)
            .put("status", inventoryDiffItem.status)
            .put("createdAt", inventoryDiffItem.createdAt)
            .put("updatedAt", inventoryDiffItem.updatedAt)
            .put("deletedAt", inventoryDiffItem.deletedAt)
            .put("isDeleted", inventoryDiffItem.isDeleted)
            .put("other", inventoryDiffItem.other)
            .toString()
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("http://$ip:3000/inventoryDiff/")
            .post(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                var jsonObj = JSONObject(body)
                jsonObj
            }
        }
    }
    suspend fun sendInventorySession(ip:String, inventorySession: SessionInventory, client: OkHttpClient) : JSONObject {
        val json = JSONObject()
            .put("id", inventorySession.id)
            .put("supplierId", inventorySession.supplierId)
            .put("cellId", inventorySession.cellId)
            .put("prevSessionId", inventorySession.prevSessionId)
            .put("status", inventorySession.status)
            .put("createdAt", inventorySession.createdAt)
            .put("startedAt", inventorySession.startedAt)
            .put("finishedAt", inventorySession.finishedAt)
            .put("updatedAt", inventorySession.updatedAt)
            .put("deletedAt", inventorySession.deletedAt)
            .put("isDeleted", inventorySession.isDeleted)
            .put("other", inventorySession.other)
            .toString()
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("http://$ip:3000/inventorySession/")
            .post(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                var jsonObj = JSONObject(body)
                jsonObj
            }
        }
    }

}