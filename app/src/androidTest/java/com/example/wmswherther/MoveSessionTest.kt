package com.example.wmswherther

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.wmsRemote.MainActivity
import com.example.wmsRemote.R
import com.example.wmsRemote.data.db.Dao
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.data.enums.OperationType
import com.example.wmsRemote.data.enums.StatusType
import com.example.wmswherther.Classes.UiState
import com.example.wmswherther.Fragments.IncomeSessionFragment
import com.example.wmswherther.Fragments.MoveSessionFragment
import com.example.wmswherther.Fragments.ServiceLocator
import com.example.wmswherther.data.db.Entityes.Barcode
import com.example.wmswherther.data.db.Entityes.Catalog
import com.example.wmswherther.data.db.Entityes.Cell
import com.example.wmswherther.data.db.Entityes.CellType
import com.example.wmswherther.data.db.Entityes.Change
import com.example.wmswherther.data.db.Entityes.Credential
import com.example.wmswherther.data.db.Entityes.Goods
import com.example.wmswherther.data.db.Entityes.Movement
import com.example.wmswherther.data.db.Entityes.SessionIncome
import com.example.wmswherther.data.db.Entityes.SessionInventory
import com.example.wmswherther.data.db.Entityes.Supplier
import com.example.wmswherther.data.db.Entityes.User
import com.example.wmswherther.data.db.Repositories.MoveeRepository
import com.example.wmswherther.data.factory.BarcodeFactory
import com.example.wmswherther.data.factory.CatalogFactory
import com.example.wmswherther.data.factory.CellFactory
import com.example.wmswherther.data.factory.CellTypeFactory
import com.example.wmswherther.data.factory.ChangeFactory
import com.example.wmswherther.data.factory.CredentialFactory
import com.example.wmswherther.data.factory.GoodsFactory
import com.example.wmswherther.data.factory.UserFactory
import com.example.wmswherther.viewModel.MainViewModel
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import org.hamcrest.core.AllOf.allOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)

class MoveSessionTest {
    private lateinit var db: MainDB
    private lateinit var repo: MoveeRepository
    private var supplierId : Int = 123234
    lateinit var scenario: ActivityScenario<MainActivity>
    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MainDB::class.java
        ).allowMainThreadQueries().build()

        var dao = db.getDao()
        repo = MoveeRepository(dao)
        ServiceLocator.moveRepository = repo

        // 👉 подготовка данных
        runBlocking {
            appendMoveDummyData(dao = dao , supplierId)
            appendFunctionality(db)
            appendUser(db)
        }

        scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            val vm = ViewModelProvider(activity)[MainViewModel::class]
            vm.setActiveUi(UiState.MoveSessionMenu())

            var newFragment = MoveSessionFragment()

            vm.setActiveUi(UiState.MoveSessionMenu(prevState = vm.uiState.value, supplierId = supplierId))

            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, newFragment)
                .commitNow()


        }

    }

    @After
    fun teardown() {
        scenario.close()                      // UI
        db.close()                            // DB
        ServiceLocator.moveRepository = null // singleton
    }

    @Test
    fun checkBaseData() {
        Thread.sleep(500)
        onView(withId(R.id.btnThreeDots))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
            .perform(click())



        onView(withText("Scanning mode"))
            .inRoot(isPlatformPopup())
            .check(matches(isDisplayed()))
            .perform(click())

        onView(withId(R.id.etIncomeBarcode))
            .perform(clearText(), replaceText ("A111"))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
        onView(withId(R.id.etIncomeBarcode))
            .perform(pressImeActionButton())

        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).check(matches(withText("0/13")))


    }
    @Test
    fun LessMoving() {
        Thread.sleep(500)
        onView(withId(R.id.btnThreeDots))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
            .perform(click())



        onView(withText("Scanning mode"))
            .inRoot(isPlatformPopup())
            .check(matches(isDisplayed()))
            .perform(click())

        onView(withId(R.id.etIncomeBarcode))
            .perform(clearText(), replaceText ("A111"))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
        onView(withId(R.id.etIncomeBarcode))
            .perform(pressImeActionButton())

        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).check(matches(withText("0/13")))

        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).perform(click())

        onView(withId(R.id.etSelectedCount))
            .perform(clearText(), typeText("5"))
        onView(withId(R.id.etSelectedCount))
            .perform(pressImeActionButton())

        onView(withId(R.id.btnMove))
            .perform(click())

        onView(withId(R.id.etIncomeBarcode))
            .perform(clearText(), replaceText ("A112"))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
        onView(withId(R.id.etIncomeBarcode))
            .perform(pressImeActionButton())
        Thread.sleep(500)

       onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).check(matches(withText("0/8")))

        onView(withId(R.id.etIncomeBarcode))
            .perform(clearText(), replaceText ("A112"))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
        onView(withId(R.id.etIncomeBarcode))
            .perform(pressImeActionButton())

        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).check(matches(withText("0/5")))


    }
    @Test
    fun Moving() {
        Thread.sleep(500)
        onView(withId(R.id.btnThreeDots))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
            .perform(click())



        onView(withText("Scanning mode"))
            .inRoot(isPlatformPopup())
            .check(matches(isDisplayed()))
            .perform(click())

        onView(withId(R.id.etIncomeBarcode))
            .perform(clearText(), replaceText ("A111"))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
        onView(withId(R.id.etIncomeBarcode))
            .perform(pressImeActionButton())

        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).check(matches(withText("0/13")))

        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).perform(click())

        onView(withId(R.id.etSelectedCount))
            .perform(clearText(), typeText("13"))
        onView(withId(R.id.etSelectedCount))
            .perform(pressImeActionButton())

        onView(withId(R.id.btnMove))
            .perform(click())

        onView(withId(R.id.etIncomeBarcode))
            .perform(clearText(), replaceText ("A112"))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
        onView(withId(R.id.etIncomeBarcode))
            .perform(pressImeActionButton())
        Thread.sleep(500)

        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).check(doesNotExist())

        onView(withId(R.id.etIncomeBarcode))
            .perform(clearText(), replaceText ("A112"))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
        onView(withId(R.id.etIncomeBarcode))
            .perform(pressImeActionButton())

        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).check(matches(withText("0/13")))


    }
    @Test
    fun MoreMoving() {
        Thread.sleep(500)
        onView(withId(R.id.btnThreeDots))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
            .perform(click())



        onView(withText("Scanning mode"))
            .inRoot(isPlatformPopup())
            .check(matches(isDisplayed()))
            .perform(click())

        onView(withId(R.id.etIncomeBarcode))
            .perform(clearText(), replaceText ("A111"))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
        onView(withId(R.id.etIncomeBarcode))
            .perform(pressImeActionButton())

        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).check(matches(withText("0/13")))

        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).perform(click())

        onView(withId(R.id.etSelectedCount))
            .perform(clearText(), typeText("1333"))
        onView(withId(R.id.etSelectedCount))
            .perform(pressImeActionButton())

        onView(withId(R.id.btnMove))
            .perform(click())

        onView(withId(R.id.etIncomeBarcode))
            .perform(clearText(), replaceText ("A112"))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
        onView(withId(R.id.etIncomeBarcode))
            .perform(pressImeActionButton())
        Thread.sleep(500)

        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).check(doesNotExist())

        onView(withId(R.id.etIncomeBarcode))
            .perform(clearText(), replaceText ("A112"))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
        onView(withId(R.id.etIncomeBarcode))
            .perform(pressImeActionButton())

        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).check(matches(withText("0/13")))


    }
    @Test
    fun MovingByBarcode() {
        Thread.sleep(500)
        onView(withId(R.id.btnThreeDots))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
            .perform(click())



        onView(withText("Scanning mode"))
            .inRoot(isPlatformPopup())
            .check(matches(isDisplayed()))
            .perform(click())

        onView(withId(R.id.etIncomeBarcode))
            .perform(clearText(), replaceText ("A111"))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
        onView(withId(R.id.etIncomeBarcode))
            .perform(pressImeActionButton())

        onView(withId(R.id.etIncomeBarcode))
            .perform(clearText(), replaceText ("4665453776410"))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
        onView(withId(R.id.etIncomeBarcode))
            .perform(pressImeActionButton())
        Thread.sleep(500)

        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).check(matches(withText("1/13")))



        onView(withId(R.id.btnMove))
            .perform(click())

        onView(withId(R.id.etIncomeBarcode))
            .perform(clearText(), replaceText ("A112"))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
        onView(withId(R.id.etIncomeBarcode))
            .perform(pressImeActionButton())
        Thread.sleep(500)

        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).check(matches(withText("0/12")))

        onView(withId(R.id.etIncomeBarcode))
            .perform(clearText(), replaceText ("A112"))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
        onView(withId(R.id.etIncomeBarcode))
            .perform(pressImeActionButton())
        Thread.sleep(500)

        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).check(matches(withText("0/1")))


    }
    @Test
    fun checkMovement(){
        Thread.sleep(500)
        var listGoods: List<Pair<Goods,Catalog>>
        var goods: Pair<Goods,Catalog>
        var catalog: Catalog
        var movement: Movement
        var lessMovement: Movement
        var listMovement: List<Movement>
        var session: SessionIncome
        var cellFrom: Cell
        var cellTo: Cell
        runBlocking {
            listGoods = repo.getGoods().map { Pair(it, repo.getCatalogById(it.catalogId)) }.filter { it.second.name == "Kettle k510" }
        }
        assertEquals(1,listGoods.size)

        onView(withId(R.id.btnThreeDots))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
            .perform(click())



        onView(withText("Scanning mode"))
            .inRoot(isPlatformPopup())
            .check(matches(isDisplayed()))
            .perform(click())

        onView(withId(R.id.etIncomeBarcode))
            .perform(clearText(), replaceText ("A111"))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
        onView(withId(R.id.etIncomeBarcode))
            .perform(pressImeActionButton())

        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).check(matches(withText("0/13")))

        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).perform(click())

        onView(withId(R.id.etSelectedCount))
            .perform(clearText(), typeText("5"))
        onView(withId(R.id.etSelectedCount))
            .perform(pressImeActionButton())

        onView(withId(R.id.btnMove))
            .perform(click())

        onView(withId(R.id.etIncomeBarcode))
            .perform(clearText(), replaceText ("A112"))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
        onView(withId(R.id.etIncomeBarcode))
            .perform(pressImeActionButton())
        Thread.sleep(500)


        runBlocking {
            listGoods = repo.getGoods().map { Pair(it, repo.getCatalogById(it.catalogId)) }.filter { it.second.name == "Kettle k510" }
            listMovement = repo.getAllMovement()
            goods = listGoods.first{it.second.name == "Kettle k510"}
            cellFrom = repo.getCellByName("A111")
            cellTo = repo.getCellByName("A112")
            movement = listMovement.first { it.goodsId == goods.first.id  && it.cellToId == cellTo.id}

        }


        assertEquals(2,listGoods.size)
        assertEquals(13,listGoods[0].first.amount + listGoods[1].first.amount)
        assertEquals(5,movement.qty.toInt())
        assertEquals(movement.entityId, listGoods.first{it.first.amount == 8}.first.id)


    }
    @Test
    fun checkMergeMovement(){
        Thread.sleep(500)
        var listGoods: List<Pair<Goods,Catalog>>
        var goods: Pair<Goods,Catalog>
        var catalog: Catalog
        var movement: Movement
        var mergeMovement: Movement
        var listMovement: List<Movement>
        var session: SessionIncome
        var cellFrom: Cell
        var cellTo: Cell
        var cellMerge: Cell
        runBlocking {
            listGoods = repo.getGoods().map { Pair(it, repo.getCatalogById(it.catalogId)) }.filter { it.second.name == "Kettle k510" }
        }
        assertEquals(1,listGoods.size)

        onView(withId(R.id.btnThreeDots))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
            .perform(click())



        onView(withText("Scanning mode"))
            .inRoot(isPlatformPopup())
            .check(matches(isDisplayed()))
            .perform(click())

        onView(withId(R.id.etIncomeBarcode))
            .perform(clearText(), replaceText ("A111"))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
        onView(withId(R.id.etIncomeBarcode))
            .perform(pressImeActionButton())

        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).check(matches(withText("0/13")))

        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).perform(click())

        onView(withId(R.id.etSelectedCount))
            .perform(clearText(), typeText("5"))
        onView(withId(R.id.etSelectedCount))
            .perform(pressImeActionButton())

        onView(withId(R.id.btnMove))
            .perform(click())

        onView(withId(R.id.etIncomeBarcode))
            .perform(clearText(), replaceText ("A112"))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
        onView(withId(R.id.etIncomeBarcode))
            .perform(pressImeActionButton())

        Thread.sleep(500)


        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).check(matches(withText("0/8")))

        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).perform(click())

        onView(withId(R.id.etSelectedCount))
            .perform(clearText(), typeText("5"))
        onView(withId(R.id.etSelectedCount))
            .perform(pressImeActionButton())

        onView(withId(R.id.btnMove))
            .perform(click())

        onView(withId(R.id.etIncomeBarcode))
            .perform(clearText(), replaceText ("A112"))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
        onView(withId(R.id.etIncomeBarcode))
            .perform(pressImeActionButton())

        Thread.sleep(500)
        runBlocking {
            listGoods = repo.getGoods().map { Pair(it, repo.getCatalogById(it.catalogId)) }.filter { it.second.name == "Kettle k510" }
            listMovement = repo.getAllMovement()
            goods = listGoods.first{it.second.name == "Kettle k510"}
            cellFrom = repo.getCellByName("A111")
            cellTo = repo.getCellByName("A112")
            cellMerge = repo.getCellByName("merge")
            movement = listMovement.first { it.goodsId == goods.first.id  && it.cellToId == cellTo.id}

        }


        assertEquals(2,listGoods.size)
        assertEquals(13,listGoods[0].first.amount + listGoods[1].first.amount)
        assertEquals(5,movement.qty.toInt())
        assertEquals(2, listMovement.size)



    }


    suspend fun appendFunctionality(db: MainDB) {
        var cellType  = CellTypeFactory.create(
            type = "Movement",
            mask = "TODO()"
        )
        var opTye  = CellTypeFactory.create(
            type = "OperationType",
            mask = "TODO()"
        )
        var income = CellFactory.create(
            typeCellId = opTye.id,
            parentCellId = null,
            name = "income"
        )
        var outcome = CellFactory.create(
            typeCellId = opTye.id,
            parentCellId = null,
            name = "outcome"
        )
        var cellLess = CellFactory.create(
            typeCellId = cellType.id,
            parentCellId = null,
            name = "less"
        )
        var cellMore = CellFactory.create(
            typeCellId = cellType.id,
            parentCellId = null,
            name = "more"
        )
        var cellSplit = CellFactory.create(
            typeCellId = cellType.id,
            parentCellId = null,
            name = "split"
        )
        var cellMerge = CellFactory.create(
            typeCellId = cellType.id,
            parentCellId = null,
            name = "merge"
        )
        var catalogTE  = CatalogFactory.create(
            name = "TE",
            sku = "TE",
            supplierId = db.getDao().getAllSuppliers().first().id
        )
        var dao = db.getDao()
        dao.insertCatalog(catalogTE)
        dao.insertCellType(cellType)
        dao.insertCellType(opTye)
        dao.insertCell(income)
        dao.insertCell(outcome)
        dao.insertCell(cellLess)
        dao.insertCell(cellMore)
        dao.insertCell(cellSplit)
        dao.insertCell(cellMerge)
    }
    private fun appendUser(db: MainDB) {
        var credential = CredentialFactory.create(
            type = "User",
            id = 0
        )
        var credentialId = db.getDao().insertCredential(credential)
        var user = UserFactory.create(
            id = 0,
            fistName = "Pavel",
            lastName = "Semenov",
            credentialId = credentialId
        )
        db.getDao().insertUser(user)
    }
    suspend fun appendMoveDummyData(dao: Dao, supplierId: Int){
        val vitekSupplier = Supplier(
            id = supplierId,
            name = "Vitek",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            deletedAt = null,
            isDeleted = false,
            other = null
        )
        dao.insertSupplier(vitekSupplier)

        val incomeType = CellTypeFactory.create(
            type = "Income",
            mask = "IN##"
        )
        dao.insertCellType(incomeType)

        val teType = CellTypeFactory.create(
            type = "BoxTE",
            mask = "N########"
        )
        dao.insertCellType(teType)
        val pickerType = CellTypeFactory.create(
            type = "Picker",
            mask = "*###"
        )
        dao.insertCellType(pickerType)

        var A111 = CellFactory.create(
            typeCellId = pickerType.id,
            parentCellId = null,
            name = "A111"
        )
        var cellChange = ChangeFactory.create(
            payload = Gson().toJson(A111),
            payloadBefore = Gson().toJson(A111),
            entityId = A111.id,
            supplierId = null,
            operationType = OperationType.InsertCell
        )
        dao.insertCellSync(A111, cellChange)
        var N00000001 = CellFactory.create(
            typeCellId = teType.id,
            parentCellId = A111.id,
            name = "N00000001"
        )
        var teChange = ChangeFactory.create(
            payload = Gson().toJson(N00000001),
            payloadBefore = Gson().toJson(N00000001),
            entityId = N00000001.id,
            supplierId = null,
            operationType = OperationType.InsertCell
        )

        dao.insertCellSync(N00000001, teChange)

        var inventoryTask = SessionInventory(
            id = UUID.randomUUID().toString(),
            supplierId = supplierId,
            cellId = A111.id,
            prevSessionId = null,
            status = StatusType.Created.ordinal,
            createdAt = System.currentTimeMillis(),
            startedAt = System.currentTimeMillis(),
            finishedAt = null,
            updatedAt = System.currentTimeMillis(),
            deletedAt = null,
            isDeleted = false,
            other = null
        )
        var inventoryChange = ChangeFactory.create(
            payload = Gson().toJson(inventoryTask),
            payloadBefore = Gson().toJson(inventoryTask),
            entityId = inventoryTask.id,
            supplierId = vitekSupplier.id,
            operationType = OperationType.InsertInventorySession
        )
        dao.insertInventorySessionAsync(inventoryTask, inventoryChange)

        for (enum in 10 .. 29){
            var catalog = CatalogFactory.create(
                name = "Kettle k5${enum}",
                sku = "3241223",
                supplierId = vitekSupplier.id
            )
            var catalogChange = ChangeFactory.create(
                payload = Gson().toJson(catalog),
                payloadBefore = Gson().toJson(catalog),
                entityId = catalog.id,
                supplierId = vitekSupplier.id,
                operationType = OperationType.InsertCatalog
            )
            var barcode = BarcodeFactory.create(
                name = "46654537764${enum}",
                catalogId = catalog.id,
                supplierId = vitekSupplier.id
            )
            var barcodeChanges = ChangeFactory.create(
                payload = Gson().toJson(barcode),
                payloadBefore = Gson().toJson(barcode),
                entityId = barcode.id,
                supplierId = vitekSupplier.id,
                operationType = OperationType.InsertBarcode
            )
            dao.insertCatalogSync(catalog, catalogChange)
            dao.insertBarcodeAsync(barcode, barcodeChanges)


            var goods = GoodsFactory.create(
                amount = 3 + enum,
                cellId = A111.id,
                catalogId = catalog.id,
                isAvailable = true
            )
            var goodsChange = ChangeFactory.create(
                payload = Gson().toJson(goods),
                payloadBefore = Gson().toJson(goods),
                entityId = goods.id,
                supplierId = vitekSupplier.id,
                operationType = OperationType.InsertGoods
            )
            dao.insertGoodsAsync(goods, goodsChange)
        }

        for(enum in 50..52){
            var catalog = CatalogFactory.create(
                name = "Kettle k5${enum}",
                sku = "3241223",
                supplierId = vitekSupplier.id
            )
            var catalogChange = ChangeFactory.create(
                payload = Gson().toJson(catalog.id),
                payloadBefore = Gson().toJson(catalog.id),
                entityId = catalog.id,
                supplierId = vitekSupplier.id,
                operationType = OperationType.InsertCatalog
            )
            var barcode = BarcodeFactory.create(
                name = "46654537764${enum}",
                catalogId = catalog.id,
                supplierId = vitekSupplier.id
            )
            var barcodeChanges = ChangeFactory.create(
                payload = Gson().toJson(barcode),
                payloadBefore = Gson().toJson(barcode),
                entityId = barcode.id,
                supplierId = vitekSupplier.id,
                operationType = OperationType.InsertBarcode
            )
            dao.insertCatalogSync(catalog, catalogChange)
            dao.insertBarcodeAsync(barcode, barcodeChanges)


            var goods = GoodsFactory.create(
                amount = 3 + enum,
                cellId = N00000001.id,
                catalogId = catalog.id,
                isAvailable = true
            )
            var goodsChange = ChangeFactory.create(
                payload = Gson().toJson(goods),
                payloadBefore = Gson().toJson(goods),
                entityId = goods.id,
                supplierId = vitekSupplier.id,
                operationType = OperationType.InsertGoods
            )
            dao.insertGoodsAsync(goods, goodsChange)
        }


    }
}
