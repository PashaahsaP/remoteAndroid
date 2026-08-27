package com.example.wmswherther.data.db

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.wmsRemote.data.db.Dao
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.data.enums.StatusType
import com.example.wmswherther.data.db.Entityes.Barcode
import com.example.wmswherther.data.db.Entityes.Batches
import com.example.wmswherther.data.db.Entityes.Catalog
import com.example.wmswherther.data.db.Entityes.Cell
import com.example.wmswherther.data.db.Entityes.CellType
import com.example.wmswherther.data.db.Entityes.Credential
import com.example.wmswherther.data.db.Entityes.Goods
import com.example.wmswherther.data.db.Entityes.IncomeItem
import com.example.wmswherther.data.db.Entityes.InventoryDiffItem
import com.example.wmswherther.data.db.Entityes.Movement
import com.example.wmswherther.data.db.Entityes.OutcomeItem
import com.example.wmswherther.data.db.Entityes.PackageEntity
import com.example.wmswherther.data.db.Entityes.PickerItem
import com.example.wmswherther.data.db.Entityes.Service
import com.example.wmswherther.data.db.Entityes.SessionIncome
import com.example.wmswherther.data.db.Entityes.SessionInventory
import com.example.wmswherther.data.db.Entityes.SessionOutcome
import com.example.wmswherther.data.db.Entityes.SessionPicker
import com.example.wmswherther.data.db.Entityes.Supplier
import com.example.wmswherther.data.db.Entityes.TrueSign
import com.example.wmswherther.data.db.Entityes.User
import com.example.wmswherther.data.db.Repositories.SyncRepository
import com.example.wmswherther.data.enums.Entities
import com.example.wmswherther.data.enums.SyncType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

data class PullItem(
    val entity: Entities,
    var time: Long
)
class SyncWorker(
    val context: Context,
    val params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val syncType = inputData.getString("sync_type")
        var data = listOf<PullItem>()
        val type = object : TypeToken<List<PullItem>>() {}.type
        if (syncType == SyncType.PULL.name) {
            data = Gson().fromJson(
                inputData.getString("pullData"),
                type
            )
        }
        val db = MainDB.getDB(context = context)
        val dao = db.getDao()
        val request = Request()
        val client = OkHttpClient()
        val repository = SyncRepository(request, client)

        return try {
            when (syncType) {
                SyncType.PUSH.name -> {
                    pushing(dao, repository)
                }

                SyncType.PULL.name -> {
                    pulling(dao, repository, data)
                }

                SyncType.FULL.name -> {
                    pushing(dao, repository)
                    totalPulling(dao, repository)
                }
            }


            Result.success()

        } catch (e: IOException) {

            Result.retry()

        } catch (e: Exception) {


            Result.failure()
        }
    }

    suspend fun pushing(dao: Dao, repository: SyncRepository) {
        val operations = dao.getAllChanges().filter { inner -> inner.status != StatusType.Finished.ordinal }

        for (operation in operations) {

            repository.syncPush(
                ip = "172.25.188.9",
                operation = operation
            )

            dao.updateChange(operation.copy(status = StatusType.Finished.ordinal))
        }
    }

    suspend fun pulling(dao: Dao, repository: SyncRepository, data: List<PullItem>) {
        var result = repository.syncPull("172.25.188.9", data)
        prepareAndAppendData(dao, result)
    }

    suspend fun totalPulling(dao: Dao, repository: SyncRepository) {
        var data = generateDataForRequest(dao)
        var result = repository.syncPull("172.25.188.9", data)

        prepareAndAppendData(dao, result)

    }

    suspend fun prepareAndAppendData(
        dao: Dao,
        pairs: List<Pair<Entities, JSONArray>>
    ) {
        val scope = CoroutineScope(Dispatchers.IO)
        pairs.forEach {
            if (it.first.ordinal == Entities.Supplier.ordinal && it.second.length() != 0) {
                Log.d("Size", Gson().toJson(it.second))
                it.second.toEntity { obj ->
                    var sup = Supplier(
                        id = obj.getInt("id"),
                        name = obj.getString("name"),
                        createdAt = obj.getLong("createdAt"),
                        updatedAt = obj.getLong("updatedAt"),
                        deletedAt = obj.optLong("deletedAt"),
                        isDeleted = obj.getBoolean("isDeleted"),
                        other = obj.optString("other")
                    )
                    scope.launch {
                        var item = dao.getSupplierById(sup.id)
                        if(item != null)
                            dao.updateSupplier(sup)
                        else
                            dao.insertSupplier(sup)
                    }
                }
            }

        }
        delay(150)
        pairs.forEach {

            if(it.first.ordinal ==  Entities.Catalog.ordinal && it.second.length() != 0) {
                it.second.toEntity { obj ->
                    var catalog = Catalog(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        sku = obj.optString("sku"),
                        supplierId = obj.getInt("supplierId"),
                        createdAt = obj.getLong("createdAt"),
                        updatedAt = obj.getLong("updatedAt"),
                        deletedAt = obj.optLong("deletedAt"),
                        isDeleted = obj.getBoolean("isDeleted"),
                        other = obj.optString("other")
                    )
                    scope.launch {
                        var item = dao.getCatalogById(catalog.id)
                        if(item != null)
                            dao.updateCatalog(catalog)
                        else
                            dao.insertCatalog(catalog)
                    }
                }
            }
        }
        delay(150)
        pairs.forEach {
            if(it.first.ordinal ==  Entities.TypeCell.ordinal&& it.second.length() != 0) {
                it.second.toEntity { obj ->
                    var type = CellType(
                        id = obj.getString("id"),
                        type = obj.getString("type"),
                        mask = obj.optString("mask"),
                        createdAt = obj.getLong("createdAt"),
                        updatedAt = obj.getLong("updatedAt"),
                        deletedAt = obj.optLong("deletedAt"),
                        isDeleted = obj.getBoolean("isDeleted"),
                        other = obj.optString("other")
                    )
                    scope.launch {
                        var item = dao.getCellTypeById(type.id)
                        if(item != null)
                            dao.updateCellType(type)
                        else
                            dao.insertCellType(type)
                    }
                }
            }
        }
        delay(150)
        pairs.forEach {
            if(it.first.ordinal ==  Entities.Cell.ordinal&& it.second.length() != 0)
            {it.second.toEntity { obj ->
                var cell = Cell(
                    id = obj.getString("id"),
                    typeCellId = obj.getString("typeCellId"),
                    parentCellId = obj.optString("parentCellId"),
                    name = obj.getString("name"),
                    createdAt = obj.getLong("createdAt"),
                    updatedAt = obj.getLong("updatedAt"),
                    deletedAt = obj.optLong("deletedAt"),
                    isDeleted = obj.getBoolean("isDeleted"),
                    other = obj.optString("other")
                )
                scope.launch {
                    var item = dao.getCellById(cell.id)
                    if(item != null)
                        dao.updateCell(cell)
                    else
                        dao.insertCell(cell)
                }
            }}
        }
        delay(150)
        pairs.forEach {
            if(it.first.ordinal ==  Entities.Credential.ordinal&& it.second.length() != 0)
            {it.second.toEntity { obj ->
                var crd = Credential(
                    id = obj.getLong("id"),
                    type = obj.getString("type"),
                    createdAt = obj.getLong("createdAt"),
                    updatedAt = obj.getLong("updatedAt"),
                    deletedAt = obj.optLong("deletedAt"),
                    isDeleted = obj.getBoolean("isDeleted"),
                    other = obj.optString("other")
                )
                scope.launch {
                    var item = dao.getCredentialById(crd.id)
                    if(item != null)
                        dao.updateCredential(crd)
                    else
                        dao.insertCredential(crd)
                }
            }}
        }
        delay(150)
        pairs.forEach {
            if(it.first.ordinal ==  Entities.User.ordinal&& it.second.length() != 0)
            {it.second.toEntity { obj ->
                var user = User(
                    id = obj.getLong("id"),
                    firstName = obj.getString("firstName"),
                    lastName = obj.getString("firstName"),
                    credentialId = obj.getLong("credentialId"),
                    createdAt = obj.getLong("createdAt"),
                    updatedAt = obj.getLong("updatedAt"),
                    deletedAt = obj.optLong("deletedAt"),
                    isDeleted = obj.getBoolean("isDeleted"),
                    other = obj.optString("other")
                )
                scope.launch {
                    var item = dao.getUserById(user.id)
                    if(item != null)
                        dao.updateUser(user)
                    else
                        dao.insertUser(user)
                }
            }}
        }
        delay(150)
        pairs.forEach {
            if(it.first.ordinal ==  Entities.Goods.ordinal&& it.second.length() != 0)
            {it.second.toEntity { obj ->
                var goods = Goods(
                    id = obj.getString("id"),
                    amount = obj.getInt("amount"),
                    cellId = obj.getString("cellId"),
                    catalogId = obj.getString("catalogId"),
                    isAvailable = obj.getBoolean("isAvailable"),
                    createdAt = obj.getLong("createdAt"),
                    updatedAt = obj.getLong("updatedAt"),
                    deletedAt = obj.optLong("deletedAt"),
                    isDeleted = obj.getBoolean("isDeleted"),
                    other = obj.optString("other")
                )

                scope.launch {
                    var item = dao.getGoodsById(goods.id)
                    if(item != null)
                        dao.updateGoods(goods)
                    else
                        dao.insertGoods(goods)
                }
            }}
        }
        delay(150)
        pairs.forEach {
            if(it.first.ordinal ==  Entities.Barcode.ordinal && it.second.length() != 0)
            {it.second.toEntity { obj ->
                var bar = Barcode(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    catalogId = obj.getString("catalogId"),
                    supplierId = obj.optInt("supplierId"),
                    createdAt = obj.getLong("createdAt"),
                    updatedAt = obj.getLong("updatedAt"),
                    deletedAt = obj.optLong("deletedAt"),
                    isDeleted = obj.getBoolean("isDeleted"),
                    other = obj.optString("other")
                )

                scope.launch {
                    var item = dao.getBarcodeById(bar.id)
                    if(item != null)
                        dao.updateBarcode(bar)
                    else
                        dao.insertBarcode(bar)
                }
            }}
        }
        delay(150)
        pairs.forEach {
            if(it.first.ordinal ==  Entities.Batches.ordinal && it.second.length() != 0)
            {it.second.toEntity { obj ->
                var batch = Batches(
                    id = obj.getInt("id"),
                    name = obj.getString("name"),
                    catalogId = obj.getString("catalogId"),
                    createdAt = obj.getLong("createdAt"),
                    updatedAt = obj.getLong("updatedAt"),
                    deletedAt = obj.optLong("deletedAt"),
                    isDeleted = obj.getBoolean("isDeleted"),
                    other = obj.optString("other")
                )

                scope.launch {
                    var item = dao.getBatcheById(batch.id)
                    if(item != null)
                        dao.updateBatch(batch)
                    else
                        dao.insertBatch(batch)
                }
            }}
        }
        delay(150)
        pairs.forEach {
            if(it.first.ordinal ==  Entities.TrueSign.ordinal && it.second.length() != 0)
            {it.second.toEntity { obj ->
                var trueSing = TrueSign(
                    id = obj.getString("id"),
                    createdAt = obj.getLong("createdAt"),
                    updatedAt = obj.getLong("updatedAt"),
                    deletedAt = obj.optLong("deletedAt"),
                    isDeleted = obj.getBoolean("isDeleted"),
                    catalogId = obj.getString("catalogId"),
                    goodsId = obj.getString("goodsId"),
                    name = obj.optString("name"),
                    other = obj.optString("other"),
                )
                scope.launch {
                    var item = dao.getTrueSingById(trueSing.id)
                    if(item != null)
                        dao.updateTrueSing(trueSing)
                    else
                        dao.insertTrueSign(trueSing)
                }
            }}
        }
        delay(150)
        pairs.forEach {
            if(it.first.ordinal ==  Entities.SessionIncome.ordinal && it.second.length() != 0)
            {it.second.toEntity { obj ->
                var session = SessionIncome(
                    id = obj.getString("id"),
                    supplierId = obj.optInt("supplierId"),
                    incomeCellId = obj.optString("incomeCellId"),
                    //toCellId = obj.optString("toCellId"),
                    toCellId = null,
                    status = obj.getInt("status"),
                    createdAt = obj.getLong("createdAt"),
                    startedAt = obj.optLong("startedAt"),
                    finishedAt = obj.optLong("finishedAt"),
                    updatedAt = obj.getLong("updatedAt"),
                    deletedAt = obj.optLong("deletedAt"),
                    isDeleted = obj.getBoolean("isDeleted"),
                    other = obj.optString("other")
                )
                scope.launch {
                    var item = dao.getIncomeSessionById(session.id)
                    if(item != null)
                        dao.updateIncomeSession(session)
                    else
                        dao.insertIncomeSession(session)
                }
            }}
        }
        delay(150)
        pairs.forEach {
            if(it.first.ordinal ==  Entities.IncomeItem.ordinal && it.second.length() != 0)
            {it.second.toEntity { obj ->
                var incomeItem = IncomeItem(
                    id = obj.getString("id"),
                    status = obj.getInt("status"),
                    createdAt = obj.getLong("createdAt"),
                    updatedAt = obj.getLong("updatedAt"),
                    deletedAt = obj.optLong("deletedAt"),
                    isDeleted = obj.getBoolean("isDeleted"),
                    other = obj.optString("other"),
                    sessionId = obj.getString("sessionId"),
                    goodsId = obj.getString("goodsId"),
                )
                scope.launch {
                    var item = dao.getIncomeItemById(incomeItem.id)
                    if(item != null)
                        dao.updateIncomeItem(incomeItem)
                    else
                        dao.insertIncomeItem(item)
                }
            }}
        }
        delay(150)
        pairs.forEach {
            if(it.first.ordinal ==  Entities.Service.ordinal && it.second.length() != 0)
            {it.second.toEntity { obj ->
                var service = Service(
                    id = obj.getString("id"),
                    createdAt = obj.getLong("createdAt"),
                    updatedAt = obj.getLong("updatedAt"),
                    deletedAt = obj.optLong("deletedAt"),
                    isDeleted = obj.getBoolean("isDeleted"),
                    other = obj.optString("other"),
                    supplierId = obj.optInt("supplierId"),
                    name = obj.getString("name"),
                )
                scope.launch {
                    var item = dao.getServiceById(service.id)
                    if(item != null)
                        dao.updateService(service)
                    else
                        dao.insertService(service)
                }
            }}
        }
        delay(150)
        pairs.forEach {
            if(it.first.ordinal ==  Entities.Package.ordinal && it.second.length() != 0)
            {it.second.toEntity { obj ->
                var packageEntity = PackageEntity(
                    id = obj.getString("id"),
                    createdAt = obj.getLong("createdAt"),
                    updatedAt = obj.getLong("updatedAt"),
                    deletedAt = obj.optLong("deletedAt"),
                    isDeleted = obj.getBoolean("isDeleted"),
                    other = obj.optString("other"),
                    supplierId = obj.optInt("supplierId"),
                    name = obj.optString("name"),
                    baseAmount = obj.optInt("baseAmount"),
                    //catalogId = obj.optString("catalogId"),
                    weight = obj.optDouble("weight"),
                    height = obj.optDouble("height"),
                    width = obj.optDouble("width"),
                    volume = obj.optDouble("volume"),
                )
                scope.launch {
                    var item = dao.getPackageEntityById(packageEntity.id)
                    if(item != null)
                        dao.updatePackageEntity(packageEntity)
                    else
                        dao.insertPackageItem(packageEntity)
                }
            }}
        }
        delay(150)
        pairs.forEach {
            if(it.first.ordinal ==  Entities.Movement.ordinal && it.second.length() != 0)
            {it.second.toEntity { obj ->
                var movement = Movement(
                    id = obj.getString("id"),
                    createdAt = obj.getLong("createdAt"),
                    updatedAt = obj.getLong("updatedAt"),
                    deletedAt = obj.optLong("deletedAt"),
                    isDeleted = obj.getBoolean("isDeleted"),
                    catalogId = obj.getString("catalogId"),
                    cellFromId = obj.getString("cellFromId"),
                    cellToId = obj.getString("cellToId"),
                    goodsId = obj.getString("goodsId"),
                    qty = obj.getString("qty"),
                    userId = obj.getLong("userId"),
                    executedAt = obj.getLong("executedAt"),
                    operationType = obj.optInt("operationType"),
                    entityId = obj.optString("entityId"),
                )
                scope.launch {
                    var item = dao.getMovementById(movement.id)
                    if(item != null)
                        dao.updateMovement(movement)
                    else
                        dao.insertMovement(movement)
                }
            }}
        }
        delay(150)
        pairs.forEach {
            if(it.first.ordinal ==  Entities.SessionInventory.ordinal && it.second.length() != 0)
            {it.second.toEntity { obj ->
                var session = SessionInventory(
                    id = obj.getString("id"),
                    supplierId = obj.optInt("supplierId"),
                    cellId = obj.optString("cellId"),
                    prevSessionId = obj.optString("prevSessionId"),
                    status = obj.getInt("status"),
                    createdAt = obj.getLong("createdAt"),
                    startedAt = obj.optLong("startedAt"),
                    finishedAt = obj.optLong("finishedAt"),
                    updatedAt = obj.getLong("updatedAt"),
                    deletedAt = obj.optLong("deletedAt"),
                    isDeleted = obj.getBoolean("isDeleted"),
                    other = obj.optString("other")
                )
                scope.launch {
                    var item = dao.getInventorySessionById(session.id)
                    if(item != null)
                        dao.updateInventorySession(session)
                    else
                        dao.insertInventorySession(session)
                }
            }}
        }
        delay(150)
        pairs.forEach {
            if(it.first.ordinal ==  Entities.InventoryDiffItem.ordinal && it.second.length() != 0)
            {it.second.toEntity { obj ->
                var diff = InventoryDiffItem(
                    id = obj.getString("id"),
                    status = obj.getInt("status"),
                    createdAt = obj.getLong("createdAt"),
                    updatedAt = obj.getLong("updatedAt"),
                    deletedAt = obj.optLong("deletedAt"),
                    isDeleted = obj.getBoolean("isDeleted"),
                    other = obj.optString("other"),
                    inventorySessionId = obj.getString("inventorySessionId"),
                    catalogId = obj.getString("catalogId"),
                    barcode = obj.getString("barcode"),
                    isTE = obj.getBoolean("isTE"),
                    parentCellId = obj.getString("parentCellId"),
                    diffCount = obj.getInt("diffCount")
                )
                scope.launch {
                    var item = dao.getInventoryDiffItemById(diff.id)
                    if(item != null)
                        dao.updateInventoryDiffItem(diff)
                    else
                        dao.insertInventoryDiffItem(item)
                }
            }}
        }
        delay(150)
        pairs.forEach {
            if(it.first.ordinal ==  Entities.SessionPicker.ordinal && it.second.length() != 0)
            {it.second.toEntity { obj ->
                var session = SessionPicker(
                    id = obj.getString("id"),
                    supplierId = obj.optInt("supplierId"),
                    outCellId = obj.optString("outCellId"),
                    status = obj.getInt("status"),
                    createdAt = obj.getLong("createdAt"),
                    startedAt = obj.optLong("startedAt"),
                    finishedAt = obj.optLong("finishedAt"),
                    updatedAt = obj.getLong("updatedAt"),
                    deletedAt = obj.optLong("deletedAt"),
                    isDeleted = obj.getBoolean("isDeleted"),
                    other = obj.optString("other"),
                )
                scope.launch {
                    var item = dao.getPickerSessionById(session.id)
                    if(item != null)
                        dao.updatePickerSession(session)
                    else
                        dao.insertPickerSession(session)
                }
            }}
        }
        delay(150)
        pairs.forEach {
            if(it.first.ordinal ==  Entities.PickerItem.ordinal && it.second.length() != 0)
            {it.second.toEntity { obj ->
                var pickerItem = PickerItem(
                    id = obj.getString("id"),
                    status = obj.getInt("status"),
                    createdAt = obj.getLong("createdAt"),
                    updatedAt = obj.getLong("updatedAt"),
                    deletedAt = obj.optLong("deletedAt"),
                    isDeleted = obj.getBoolean("isDeleted"),
                    other = obj.optString("other"),
                    sessionId = obj.getString("sessionId"),
                    goodsId = obj.getString("goodsId"),
                    cellId = obj.optString("cellId"),
                    startedAt = obj.optLong("startedAt"),
                    finishedAt = obj.optLong("finishedAt"),
                )
                scope.launch {
                    var item = dao.getPickerItemById(pickerItem.id)
                    if (item != null)
                        dao.updatePickerItem(pickerItem)
                    else
                        dao.insertPickerItem(pickerItem)
                }
            }}
        }
        delay(150)
        pairs.forEach {
            if(it.first.ordinal ==  Entities.SessionOutcome.ordinal && it.second.length() != 0)
            {it.second.toEntity { obj ->
                var session = SessionOutcome(
                    id = obj.getString("id"),
                    supplierId = obj.optInt("supplierId"),
                    toCellId = obj.optString("toCellId"),
                    outCellId = obj.optString("outCellId"),
                    status = obj.getInt("status"),
                    createdAt = obj.getLong("createdAt"),
                    startedAt = obj.optLong("startedAt"),
                    finishedAt = obj.optLong("finishedAt"),
                    updatedAt = obj.getLong("updatedAt"),
                    deletedAt = obj.optLong("deletedAt"),
                    isDeleted = obj.getBoolean("isDeleted"),
                    other = obj.optString("other"),
                    pickerSessionId = obj.optString("pickerSessionId")
                )
                scope.launch {
                    var item = dao.getOutcomeSessionById(session.id)
                    if(item != null)
                        dao.updateOutcomeSession(session)
                    else
                        dao.insertOutcomeSession(session)
                }
            }}
        }
        delay(150)
        pairs.forEach {
            if(it.first.ordinal ==  Entities.OutcomeItem.ordinal && it.second.length() != 0)
            {it.second.toEntity { obj ->
                var outcomeItem = OutcomeItem(
                    id = obj.getString("id"),
                    status = obj.getInt("status"),
                    createdAt = obj.getLong("createdAt"),
                    updatedAt = obj.getLong("updatedAt"),
                    deletedAt = obj.optLong("deletedAt"),
                    isDeleted = obj.getBoolean("isDeleted"),
                    other = obj.optString("other"),
                    sessionId = obj.getString("sessionId"),
                    goodsId = obj.getString("goodsId"),
                    cellId = obj.optString("cellId"),
                )
                scope.launch {
                    var item = dao.getOutcomeItemById(outcomeItem.id)
                    if(item != null)
                        dao.updateOutcomeItem(outcomeItem)
                    else
                        dao.insertOutcomeItem(outcomeItem)
                }
            }}
        }
        delay(150)

    }

    suspend fun generateDataForRequest(dao: Dao): MutableList<PullItem> {
        var data = mutableListOf<PullItem>()
        data += PullItem(Entities.Catalog, dao.getCatalogs().maxOfOrNull { it.updatedAt } ?: 0)
        data += PullItem(Entities.Goods, dao.getGoods().maxOfOrNull { it.updatedAt }?: 0)
        data += PullItem(Entities.Barcode, dao.getBarcodes().maxOfOrNull { it.updatedAt }?: 0)
        data += PullItem(Entities.Batches, dao.getBatches().maxOfOrNull { it.updatedAt }?: 0)
        data += PullItem(Entities.Cell, dao.getAllCells().maxOfOrNull { it.updatedAt }?: 0)
        data += PullItem(Entities.TypeCell, dao.getCellTypes().maxOfOrNull { it.updatedAt }?: 0)
        data += PullItem(Entities.Credential, dao.getAllCredential().maxOfOrNull { it.updatedAt }?: 0)
        data += PullItem(Entities.IncomeItem, dao.getAllIncomeItem().maxOfOrNull { it.updatedAt }?: 0)
        data += PullItem(Entities.InventoryDiffItem, dao.getInventoryDiffItems().maxOfOrNull { it.updatedAt }?: 0)
        data += PullItem(Entities.Movement, dao.getAllMovement().maxOfOrNull { it.updatedAt }?: 0)
        data += PullItem(Entities.OutcomeItem, dao.getAllOutcomeItems().maxOfOrNull { it.updatedAt }?: 0)
        data += PullItem(Entities.Package, dao.getAllPackageItems().maxOfOrNull { it.updatedAt }?: 0)
        data += PullItem(Entities.PickerItem, dao.getPickerItems().maxOfOrNull { it.updatedAt }?: 0)
        data += PullItem(Entities.Service, dao.getAllService().maxOfOrNull { it.updatedAt }?: 0)
        data += PullItem(Entities.SessionIncome, dao.getAllIncomeSession().maxOfOrNull { it.updatedAt }?: 0)
        data += PullItem(Entities.SessionInventory, dao.getInventorySessions().maxOfOrNull { it.updatedAt }?: 0)
        data += PullItem(Entities.SessionOutcome, dao.getAllOutcomeSession().maxOfOrNull { it.updatedAt }?: 0)
        data += PullItem(Entities.SessionPicker, dao.getPickerSessions().maxOfOrNull { it.updatedAt }?: 0)
        data += PullItem(Entities.Supplier, dao.getAllSuppliers().maxOfOrNull { it.updatedAt }?: 0)
        data += PullItem(Entities.TrueSign, dao.getAllTrueSign().maxOfOrNull { it.updatedAt }?: 0)
        data += PullItem(Entities.User, dao.getAllUser().maxOfOrNull { it.updatedAt }?: 0)

        data.forEach { inner ->
            if(inner.time > 0){
                 inner.time += 1L
            }
        }
        return data
    }
}
fun <T> JSONArray.toEntity(mapper: (JSONObject) -> T): List<T> {
    return (0 until length()).map { index ->
        mapper(getJSONObject(index))
    }
}