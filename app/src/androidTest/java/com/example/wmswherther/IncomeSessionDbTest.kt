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
import com.example.wmswherther.viewModel.IncomeSessionViewModel
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
    private lateinit var vm : IncomeSessionViewModel

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = MainDB.getDB(context)
        repo = IncomeRepository(db.getDao())
        sessionId = "12312312"
        db = Room.inMemoryDatabaseBuilder(
            context,
            MainDB::class.java
        ).allowMainThreadQueries().build()
        runBlocking {
            appendDummyData(db.getDao(), sessionId)
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
    suspend fun appendDummyData(daoTransp: Dao, sessionId: String){
        val dao = daoTransp
        val borkSupplier = Supplier(
            UUID.randomUUID().toString(),
            "Bork",
            null
        )
        val atomySupplier = Supplier(
            UUID.randomUUID().toString(),
            "Atomy",
            null
        )
        dao.insertSupplier(borkSupplier)
        dao.insertSupplier(atomySupplier)

        val incomeType = CellType(
            UUID.randomUUID().toString(),
            "Income",
            "IN##",
            null
        )
        dao.insertCellType(incomeType)

        val teType = CellType(
            UUID.randomUUID().toString(),
            "BoxTE",
            "N########",
            null
        )
        dao.insertCellType(teType)

        var IN01 = Cell(
            UUID.randomUUID().toString(),
            incomeType.id,
            null,
            "IN-01"
        )
        var cellChange = Change(
            UUID.randomUUID().toString(),
            IN01.id,
            OperationType.InsertCell.ordinal,
            StatusType.Created.ordinal,
            null,
            null
        )
        dao.insertCellSync(IN01, cellChange)
        var N00000001 = Cell(
            UUID.randomUUID().toString(),
            teType.id,
            IN01.id,
            "N00000001"
        )
        var teChange = Change(
            UUID.randomUUID().toString(),
            N00000001.id,
            OperationType.InsertCell.ordinal,
            StatusType.Created.ordinal,
            null,
            null
        )
        dao.insertCellSync(N00000001, teChange)

        val pickerType = CellType(
            UUID.randomUUID().toString(),
            "Picker",
            "*###",
            null
        )
        dao.insertCellType(pickerType)

        var session = SessionIncome(
            sessionId,
            borkSupplier.id,
            null,
            IN01.id,
            StatusType.Created.ordinal,
            System.currentTimeMillis(),
            null,
            null,
            null
        )
        var sessionChange = Change(
            UUID.randomUUID().toString(),
            session.id,
            OperationType.InsertIncomeSession.ordinal,
            status = StatusType.Created.ordinal,
            borkSupplier.id,
            null
        )
        dao.insertIncomeSessionAsync(session, sessionChange)

        for (enum in 10 .. 29){
            var A111 = Cell(
                UUID.randomUUID().toString(),
                pickerType.id,
                null,
                "A1${enum}"
            )
            var cellChangeSecond = Change(
                UUID.randomUUID().toString(),
                A111.id,
                OperationType.InsertCell.ordinal,
                StatusType.Created.ordinal,
                null,
                null
            )
            dao.insertCellSync(A111, cellChangeSecond)
            var catalog = Catalog(
                UUID.randomUUID().toString(),
                "Kettle k5${enum}",
                "3241223",
                borkSupplier.id,
                null
            )
            var catalogChange = Change(
                UUID.randomUUID().toString(),
                catalog.id,
                OperationType.InsertCatalog.ordinal,
                StatusType.Created.ordinal,
                borkSupplier.id,
                null
            )

            var barcode = Barcode(
                UUID.randomUUID().toString(),
                "46654537764${enum}",
                catalog.id,
                borkSupplier.id,
                null
            )
            var barcodeChanges = Change(
                UUID.randomUUID().toString(),
                barcode.id,
                OperationType.InsertBarcode.ordinal,
                StatusType.Created.ordinal,
                borkSupplier.id,
                null
            )
            dao.insertCatalogSync(catalog, catalogChange)
            dao.insertBarcodeAsync(barcode, barcodeChanges)


            var goods = Goods(
                id = UUID.randomUUID().toString(),
                amount = 3 + enum,
                cellId = A111.id,
                catalogId = catalog.id,
                createdAt = System.currentTimeMillis(),
                isAvailable = false,
                other = null
            )
            var goodsChange = Change(
                UUID.randomUUID().toString(),
                goods.id,
                OperationType.InsertGoods.ordinal,
                status = StatusType.Created.ordinal,
                borkSupplier.id,
                null
            )
            dao.insertGoodsAsync(goods, goodsChange)

            var incomeItem = IncomeItem(
                UUID.randomUUID().toString(),
                session.id,
                goods.id,
                StatusType.Created.ordinal,
                null
            )
            var incomeItemChange = Change(
                UUID.randomUUID().toString(),
                incomeItem.id,
                OperationType.InsertIncomeItem.ordinal,
                StatusType.Created.ordinal,
                borkSupplier.id,
                null
            )
            dao.insertIncomeItemSync(incomeItem, incomeItemChange)

        }

        for(enum in 50..52){
            var A111 = Cell(
                UUID.randomUUID().toString(),
                pickerType.id,
                null,
                "A1${enum}"
            )
            var cellChangeSecond = Change(
                UUID.randomUUID().toString(),
                A111.id,
                OperationType.InsertCell.ordinal,
                StatusType.Created.ordinal,
                null,
                null
            )
            dao.insertCellSync(A111, cellChangeSecond)
            var catalog = Catalog(
                UUID.randomUUID().toString(),
                "Kettle k5${enum}",
                "3241223",
                borkSupplier.id,
                null
            )
            var catalogChange = Change(
                UUID.randomUUID().toString(),
                catalog.id,
                OperationType.InsertCatalog.ordinal,
                StatusType.Created.ordinal,
                borkSupplier.id,
                null
            )

            var barcode = Barcode(
                UUID.randomUUID().toString(),
                "46654537764${enum}",
                catalog.id,
                borkSupplier.id,
                null
            )
            var barcodeChanges = Change(
                UUID.randomUUID().toString(),
                barcode.id,
                OperationType.InsertBarcode.ordinal,
                StatusType.Created.ordinal,
                borkSupplier.id,
                null
            )
            dao.insertCatalogSync(catalog, catalogChange)
            dao.insertBarcodeAsync(barcode, barcodeChanges)


            var goods = Goods(
                id = UUID.randomUUID().toString(),
                amount = 3 + enum,
                cellId = N00000001.id,
                catalogId = catalog.id,
                createdAt = System.currentTimeMillis(),
                isAvailable = false,
                other = null
            )
            var goodsChange = Change(
                UUID.randomUUID().toString(),
                goods.id,
                OperationType.InsertGoods.ordinal,
                status = StatusType.Created.ordinal,
                borkSupplier.id,
                null
            )
            dao.insertGoodsAsync(goods, goodsChange)

            var incomeItem = IncomeItem(
                UUID.randomUUID().toString(),
                session.id,
                goods.id,
                StatusType.Created.ordinal,
                null
            )
            var incomeItemChange = Change(
                UUID.randomUUID().toString(),
                incomeItem.id,
                OperationType.InsertIncomeItem.ordinal,
                StatusType.Created.ordinal,
                borkSupplier.id,
                null
            )
            dao.insertIncomeItemSync(incomeItem, incomeItemChange)
        }


    }
}
