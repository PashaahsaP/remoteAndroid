package com.example.wmswherther.data.db

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.wmsRemote.data.db.Dao
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.data.enums.StatusType
import com.example.wmswherther.data.db.Repositories.SyncRepository
import com.example.wmswherther.data.enums.SyncType
import okhttp3.OkHttpClient
import java.io.IOException

class SyncWorker(
    val context: Context,
    val params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val syncType = inputData.getString("sync_type")
        val db = MainDB.getDB(context = context)
        val dao = db.getDao()
        val request = Request()
        val client = OkHttpClient()
        val repository = SyncRepository(request, client)

        return try {
            when(syncType){
                SyncType.PUSH.name ->{
                    pushing(dao, repository)
                }
                SyncType.PULL.name ->{
                    //pulling(dao, repository)
                }
                SyncType.FULL.name ->{
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
    suspend fun pushing(dao: Dao, repository: SyncRepository){
        val operations = dao.getAllChanges()

        for(operation in operations) {

            repository.syncPush(
                ip = "192.168.0.10",
                operation = operation
            )

            dao.updateChange(operation.copy(status = StatusType.Finished.ordinal))
        }
    }
    suspend fun pulling(dao: Dao, repository: SyncRepository,targetData:  Pair<String, Long>){


    }
    suspend fun totalPulling(dao: Dao, repository: SyncRepository){
        var catalog = Pair("Catalog", dao.getCatalogs().maxOf { it.updatedAt })
        var goods = Pair("Goods", dao.getGoods().maxOf { it.updatedAt })
        var barcode = Pair("Barcode", dao.getBarcodes().maxOf { it.updatedAt })
        var cells = Pair("Cell", dao.getAllCells().maxOf { it.updatedAt })
        var cellType = Pair("CellType", dao.getCellTypes().maxOf { it.updatedAt })
        var credential = Pair("Credential", dao.getAllCredential().maxOf { it.updatedAt })
        var incomeItem = Pair("IncomeItem", dao.getAllIncomeItem().maxOf { it.updatedAt })
        var inventoryDiffItem = Pair("InventoryDiffItem", dao.getInventoryDiffItems().maxOf { it.updatedAt })
        var movement = Pair("Movement", dao.getAllMovement().maxOf { it.updatedAt })
        var outcomeItem = Pair("OutcomeItem", dao.getAllOutcomeItems().maxOf { it.updatedAt })
        var packageEntities = Pair("PackageEntity", dao.getAllPackageItems().maxOf { it.updatedAt })
        var pickerItem = Pair("PickerItem", dao.getPickerItems().maxOf { it.updatedAt })
        var service = Pair("Service", dao.getAllService().maxOf { it.updatedAt })
        var sessionIncome = Pair("SessionIncome", dao.getAllIncomeSession().maxOf { it.updatedAt })
        var sessionInventory = Pair("SessionInventory", dao.getInventorySessions().maxOf { it.updatedAt })
        var sessionOutcome = Pair("SessionOutcome", dao.getAllOutcomeSession().maxOf { it.updatedAt })
        var sessionPicker = Pair("SessionPicker", dao.getPickerSessions().maxOf { it.updatedAt })
        var supplier = Pair("Supplier", dao.getAllSuppliers().maxOf { it.updatedAt })
        var trueSign = Pair("TrueSign", dao.getAllTrueSign().maxOf { it.updatedAt })
        var user = Pair("User", dao.getAllUser().maxOf { it.updatedAt })

    }
}