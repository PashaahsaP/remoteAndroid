package com.example.wmswherther

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.wmsRemote.data.db.Dao
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.data.enums.OperationType
import com.example.wmsRemote.data.enums.StatusType
import com.example.wmswherther.data.db.Entityes.Barcode
import com.example.wmswherther.data.db.Entityes.Catalog
import com.example.wmswherther.data.db.Entityes.Cell
import com.example.wmswherther.data.db.Entityes.CellType
import com.example.wmswherther.data.db.Entityes.Change
import com.example.wmswherther.data.db.Entityes.Goods
import com.example.wmswherther.data.db.Entityes.IncomeItem
import com.example.wmswherther.data.db.Entityes.SessionIncome
import com.example.wmswherther.data.db.Entityes.Supplier
import com.example.wmswherther.data.db.Repositories.IncomeRepository
import com.example.wmswherther.data.factory.BarcodeFactory
import com.example.wmswherther.data.factory.CatalogFactory
import com.example.wmswherther.data.factory.CellFactory
import com.example.wmswherther.data.factory.CellTypeFactory
import com.example.wmswherther.data.factory.ChangeFactory
import com.example.wmswherther.data.factory.GoodsFactory
import com.example.wmswherther.data.factory.IncomeItemFactory
import com.example.wmswherther.data.factory.SessionIncomeFactory
import com.example.wmswherther.data.factory.SupplierFactory
import com.example.wmswherther.viewModel.IncomeSessionViewModel
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class IncomeSessionDbTest {
    private lateinit var db: MainDB
    private lateinit var repo: IncomeRepository
    private lateinit var sessionId : String
    private lateinit var supplierId : String
    private lateinit var vm : IncomeSessionViewModel

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = MainDB.getDB(context)
        repo = IncomeRepository(db.getDao())
        sessionId = "12312312"
        supplierId = "12312312"
        db = Room.inMemoryDatabaseBuilder(
            context,
            MainDB::class.java
        ).allowMainThreadQueries().build()
        runBlocking {
            appendDummyData(db.getDao(), sessionId, supplierId)
        }
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun loadData() = runBlocking {
        var items = db.getDao().getAllIncomeItemBySessionId(sessionId)
            .map { item -> repo.getGoodsById(item.goodsId) }
        assertEquals(23, items.size)
    }
    suspend fun appendDummyData(dao: Dao, sessionId: String, supplierId: String){
        val borkSupplier = Supplier(
            id = supplierId,
            name = "Bork",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            deletedAt = null,
            isDeleted = false,
            other = null
        )
        SupplierFactory.create("Bork")
        val atomySupplier = SupplierFactory.create("Atomy")
        dao.insertSupplier(borkSupplier)
        dao.insertSupplier(atomySupplier)

        val incomeType = CellTypeFactory.create("Income", "IN##")
        dao.insertCellType(incomeType)
        val teType = CellTypeFactory.create("BoxTE", "N########")
        dao.insertCellType(teType)

        var IN01 = CellFactory.create(
            typeCellId = incomeType.id,
            parentCellId = null,
            name = "IN-01"
        )
        var cellChange = ChangeFactory.create(
            payload = Gson().toJson(IN01) ,
            entityId = IN01.id,
            supplierId = null,
            operationType = OperationType.InsertCell
        )
        dao.insertCellSync(IN01, cellChange)

        var N00000001 = CellFactory.create(
            typeCellId = teType.id,
            parentCellId = IN01.id,
            name = "N00000001"
        )
        var teChange = ChangeFactory.create(
            payload = Gson().toJson(N00000001),
            entityId = N00000001.id,
            supplierId = null,
            operationType = OperationType.InsertCell
        )
        dao.insertCellSync(N00000001, teChange)

        val pickerType = CellTypeFactory.create(
            type = "Picker",
            mask = "*###"
        )
        dao.insertCellType(pickerType)

        var session = SessionIncome(
            id = sessionId,
            supplierId = supplierId,
            incomeCellId = null,
            toCellId = IN01.id,
            status = StatusType.Created.ordinal,
            createdAt = System.currentTimeMillis(),
            startedAt = System.currentTimeMillis(),
            finishedAt = null,
            updatedAt = System.currentTimeMillis(),
            deletedAt = null,
            isDeleted = false,
            other = null
        )
        SessionIncomeFactory.create(
            supplierId = borkSupplier.id,
            incomeCellId = null,
            toCellId = IN01.id
        )

        var sessionChange = ChangeFactory.create(
            payload = Gson().toJson(session),
            entityId = session.id,
            supplierId = borkSupplier.id,
            operationType = OperationType.InsertIncomeSession
        )
        dao.insertIncomeSessionAsync(session, sessionChange)

        for (enum in 10 .. 29){
            var A111 = CellFactory.create(
                typeCellId = pickerType.id,
                parentCellId = null,
                name = "A1${enum}"
            )
            var cellChangeSecond = ChangeFactory.create(
                payload = Gson().toJson(A111),
                entityId = A111.id,
                supplierId = null,
                operationType = OperationType.InsertCell
            )
            dao.insertCellSync(A111, cellChangeSecond)
            var catalog = CatalogFactory.create(
                name = "Kettle k5${enum}",
                sku = "3241223",
                supplierId = borkSupplier.id
            )
            var catalogChange = ChangeFactory.create(
                payload = Gson().toJson(catalog),
                entityId = catalog.id,
                supplierId = borkSupplier.id,
                operationType = OperationType.InsertCatalog
            )

            var barcode = BarcodeFactory.create(
                name = "46654537764${enum}",
                catalogId = catalog.id,
                supplierId = borkSupplier.id
            )
            var barcodeChanges = ChangeFactory.create(
                payload = Gson().toJson(barcode),
                entityId = barcode.id,
                supplierId = borkSupplier.id,
                operationType = OperationType.InsertBarcode
            )
            dao.insertCatalogSync(catalog, catalogChange)
            dao.insertBarcodeAsync(barcode, barcodeChanges)


            var goods = GoodsFactory.create(
                amount = 3 + enum,
                cellId = A111.id,
                catalogId = catalog.id,
                isAvailable = false
            )
            var goodsChange = ChangeFactory.create(
                payload = Gson().toJson(goods),
                entityId = goods.id,
                supplierId = borkSupplier.id,
                operationType = OperationType.InsertGoods
            )
            dao.insertGoodsAsync(goods, goodsChange)

            var incomeItem = IncomeItemFactory.create(
                sessionId = session.id,
                goodsId = goods.id
            )
            var incomeItemChange = ChangeFactory.create(
                payload = Gson().toJson(incomeItem),
                entityId = incomeItem.id,
                supplierId = borkSupplier.id,
                operationType = OperationType.InsertIncomeItem
            )
            dao.insertIncomeItemSync(incomeItem, incomeItemChange)

        }

        for(enum in 50..52){
            var A111 =  CellFactory.create(
                typeCellId = pickerType.id,
                parentCellId = null,
                name = "A1${enum}"
            )
            var cellChangeSecond = ChangeFactory.create(
                payload = Gson().toJson(A111),
                entityId = A111.id,
                supplierId = null,
                operationType = OperationType.InsertCell
            )
            dao.insertCellSync(A111, cellChangeSecond)
            var catalog = CatalogFactory.create(
                name = "Kettle k5${enum}",
                sku = "3241223",
                supplierId = borkSupplier.id
            )
            var catalogChange = ChangeFactory.create(
                payload = Gson().toJson(catalog),
                entityId = catalog.id,
                supplierId = borkSupplier.id,
                operationType = OperationType.InsertCatalog
            )

            var barcode = BarcodeFactory.create(
                name = "46654537764${enum}",
                catalogId = catalog.id,
                supplierId = borkSupplier.id
            )

            var barcodeChanges = ChangeFactory.create(
                payload = Gson().toJson(barcode),
                entityId = barcode.id,
                supplierId = borkSupplier.id,
                operationType = OperationType.InsertBarcode
            )
            dao.insertCatalogSync(catalog, catalogChange)
            dao.insertBarcodeAsync(barcode, barcodeChanges)


            var goods = GoodsFactory.create(
                amount = 3 + enum,
                cellId = N00000001.id,
                catalogId = catalog.id,
                isAvailable = false
            )
            var goodsChange = ChangeFactory.create(
                payload = Gson().toJson(goods),
                entityId = goods.id,
                supplierId = borkSupplier.id,
                operationType = OperationType.InsertGoods
            )
            dao.insertGoodsAsync(goods, goodsChange)

            var incomeItem = IncomeItemFactory.create(
                sessionId = session.id,
                goodsId = goods.id
            )
            var incomeItemChange = ChangeFactory.create(
                payload = Gson().toJson(incomeItem),
                entityId = incomeItem.id,
                supplierId = borkSupplier.id,
                operationType = OperationType.InsertIncomeItem
            )
            dao.insertIncomeItemSync(incomeItem, incomeItemChange)
        }


    }

}
