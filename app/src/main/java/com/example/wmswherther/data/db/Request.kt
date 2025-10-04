package com.example.wmswherther.data.db

import com.example.wmsRemote.Classes.AssemblyItem
import com.example.wmsRemote.models.client
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

  /*  suspend fun updateAtomyGoods(ip:String, goodsAtomy: GoodsAtomy) : String {
        val json = JSONObject()
            .put("id", goodsAtomy.Id)
            .put("cellId", goodsAtomy.cellId)
            .put("catalogId", goodsAtomy.catalogId)
            .put("amount", goodsAtomy.amount)
            .put("createdAt", goodsAtomy.createdAt)
            .put("TE", goodsAtomy.TE)
            .put("date", goodsAtomy.date)
            .toString()
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://$ip:3000/goodsAtomy/update")
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
    suspend fun getAllAssemblySession(ip:String) : List<JSONObject> {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://$ip:3000/assemblySessions")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                val arr = JSONArray(body)
                (0 until arr.length()).map { arr.getJSONObject(it) }
            }
        }
    }
    suspend fun getAllAssemblyBorkItemBySessionId(ip:String, sessionId: Int) : List<JSONObject> {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://$ip:3000/assemblyBorkItems/sessionId/${sessionId}")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                val arr = JSONArray(body)
                (0 until arr.length()).map { arr.getJSONObject(it) }
            }
        }
    }
    suspend fun getAssemblySessionById(ip:String, id: String) : JSONObject {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://$ip:3000/assembly_sessions/id/${id}")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                val obj = JSONObject(body)
                obj
            }
        }
    }
    suspend fun getAssemblyBorkItemById(ip:String, id: Int) : JSONObject {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://$ip:3000/assemblyBorkItems/${id}")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                val obj = JSONObject(body)
                obj
            }
        }
    }
    suspend fun updateAssemblyBorkItem(ip:String, borkItem: AssemblyItem) : String {
        var cellid = client.getCellByName(ip, borkItem.cell)
        val json = JSONObject()
            .put("id", borkItem.assemblyItemId)
            .put("assemblyId", borkItem.sessionId)
            .put("cell", cellid["id"].toString())
            .put("startedAt", LocalDateTime.now().toString())
            .put("finishedAt", LocalDateTime.now().toString())
            .put("status", "finished")
            .toString()
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://$ip:3000/assemblyBorkItems/update")
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
    suspend fun updateAssemblySession(ip:String, assSession: AssemblySession) : String {
        val json = JSONObject()
            .put("id", assSession.id)
            .put("supplierId", 1)
            .put("outCell", 1.toString())
            .put("status", "finished")
            .put("date", LocalDateTime.now().toString())
            .put("createdAt", LocalDateTime.now().toString())
            .put("finishedAt", LocalDateTime.now().toString())
            .put("amount", assSession.amount)
            .put("lines", assSession.lines)
            .toString()
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val client = OkHttpClient()
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
    suspend fun getAllAtomyGoods(ip:String) : List<String> {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://$ip:3000/allAtomyGoods")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                val arr = JSONArray(body)
                (0 until arr.length()).map { arr.getString(it) }
            }
        }
    }
    suspend fun getAllAtomyGoodsByCellId(ip:String, cellId: String) : JSONArray {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://$ip:3000/allAtomyGoods/cellId/$cellId")
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
    suspend fun getAllBorkGoodsByCellId(ip:String, cellId: String) : JSONArray {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://$ip:3000/allBorkGoods/cellId/$cellId")
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
    suspend fun getAtomyCatalogById(ip:String, id: String) : JSONObject {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://$ip:3000/catalogAtomy/id/$id")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                JSONObject(body)
            }
        }
    }
    suspend fun getBorkCatalogById(ip:String, id: String) : JSONObject {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://$ip:3000/catalogBork/id/$id")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                JSONObject(body)
            }
        }
    }
    suspend fun getBorkBarcodeByName(ip:String, name: String) : JSONObject {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://$ip:3000/barcodeBork/name/$name")
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
    }
    suspend fun getBorkCatalogByName(ip:String, name: String) : JSONObject {
        val client = OkHttpClient()
        var encoded = URLEncoder.encode(name, "UTF-8")
        val request = Request.Builder()
            .url("http://$ip:3000/catalogsBorks/name/$encoded")
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
    }
    suspend fun getBorkBarcodeByCatalogId(ip:String, catalogId: String) : JSONArray {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://$ip:3000/barcodeBork/catalogId/$catalogId")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                JSONArray(body)
            }
        }
    }
    suspend fun getAtomyCatalogByBarcode(ip:String, barcode: String) : JSONObject {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://$ip:3000/catalogAtomy/barcode/$barcode")
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
    }
    suspend fun sendAtomyCatalog(ip:String, atomyItem: CatalogAtomy) : String {
        val client = OkHttpClient()
        val json = JSONObject()
            .put("id", atomyItem.id)
            .put("name", atomyItem.name)
            .put("firstBarcode", atomyItem.firstBarcode)
            .put("secondBarcode", atomyItem.secondBarcode)
            .toString()
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("http://$ip:3000/catalogAtomy/")
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
    suspend fun sendCell(ip:String, cellName: String) : JSONObject {
        val client = OkHttpClient()
        val json = """{"name":"$cellName"}"""
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
    suspend fun sendGoodsAtomy(ip:String, goods: GoodsAtomy) : JSONObject {
        val client = OkHttpClient()
        val json = JSONObject()
            .put("id", goods.Id)
            .put("cellId", goods.cellId)
            .put("catalogId", goods.catalogId)
            .put("amount", goods.amount)
            .put("createdAt", goods.createdAt)
            .put("date", goods.date)
            .put("TE", goods.TE)
            .toString()
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("http://$ip:3000/goodsAtomy/")
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
    suspend fun sendGoodsBork(ip:String, borkGoods: GoodsBork) : JSONObject {
        val json = JSONObject()
            .put("id", borkGoods.Id)
            .put("cellId", borkGoods.cellId)
            .put("catalogId", borkGoods.catalogId)
            .put("amount", borkGoods.amount)
            .put("createdAt", borkGoods.createdAt)
            .toString()
        val client = OkHttpClient()
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("http://$ip:3000/goodsBork/")
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
    suspend fun getCellById(ip:String, id: String) : JSONObject {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://$ip:3000/cell/id/$id")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                if(body == ""){
                    JSONObject()
                }else{
                    JSONObject(body)
                }
            }
        }
    }
    suspend fun getBorkGoodsById(ip:String, id: String) : JSONObject {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://$ip:3000/goods_bork/id/$id")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                if(body == ""){
                    JSONObject()
                }else{
                    JSONObject(body)
                }
            }
        }
    }
    suspend fun getAtomyGoodsById(ip:String, id: String) : JSONObject {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://$ip:3000/goods_atomy/id/$id")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                if(body == ""){
                    JSONObject()
                }else{
                    JSONObject(body)
                }
            }
        }
    }
    suspend fun getCellByName(ip:String, name: String) : JSONObject {
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
    }
    suspend fun updateBorkGoods(ip: String, suppliementGoods: GoodsBork) {
        val json = JSONObject()
            .put("id", suppliementGoods.Id)
            .put("cellId", suppliementGoods.cellId)
            .put("catalogId", suppliementGoods.catalogId)
            .put("amount", suppliementGoods.amount)
            .put("createdAt", suppliementGoods.createdAt)
            .toString()
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://$ip:3000/goodsBork/update")
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
    suspend fun getAllBorkCatalog(ip:String) : JSONArray {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://$ip:3000/allCatalogBork/")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ошибка ${response.code}")
                val body = response.body?.string().orEmpty()
                JSONArray(body)
            }
        }
    }
    suspend fun sendBorkBarcode(ip: String, barcode: BarcodeBork): JSONObject {
        val client = OkHttpClient()
        val json = JSONObject()
            .put("catalogId", barcode.catalogId)
            .put("name", barcode.name)
            .put("type", barcode.type)
            .toString()
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("http://$ip:3000/barcodeBork/")
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
*/

}