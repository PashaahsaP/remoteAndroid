package com.example.wmswherther

import android.view.View
import android.widget.TextView
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
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
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
import com.example.wmswherther.Fragments.InventoryFragment
import com.example.wmswherther.Fragments.InventorySessionFragment
import com.example.wmswherther.Fragments.MoveSessionFragment
import com.example.wmswherther.Fragments.ServiceLocator
import com.example.wmswherther.data.db.Entityes.Barcode
import com.example.wmswherther.data.db.Entityes.Catalog
import com.example.wmswherther.data.db.Entityes.Cell
import com.example.wmswherther.data.db.Entityes.CellType
import com.example.wmswherther.data.db.Entityes.Change
import com.example.wmswherther.data.db.Entityes.Credential
import com.example.wmswherther.data.db.Entityes.Goods
import com.example.wmswherther.data.db.Entityes.InventoryDiffItem
import com.example.wmswherther.data.db.Entityes.Movement
import com.example.wmswherther.data.db.Entityes.SessionIncome
import com.example.wmswherther.data.db.Entityes.SessionInventory
import com.example.wmswherther.data.db.Entityes.Supplier
import com.example.wmswherther.data.db.Entityes.User
import com.example.wmswherther.data.db.Repositories.InventoryRepository
import com.example.wmswherther.data.factory.BarcodeFactory
import com.example.wmswherther.data.factory.CatalogFactory
import com.example.wmswherther.data.factory.CellFactory
import com.example.wmswherther.data.factory.CellTypeFactory
import com.example.wmswherther.data.factory.ChangeFactory
import com.example.wmswherther.data.factory.CredentialFactory
import com.example.wmswherther.data.factory.GoodsFactory
import com.example.wmswherther.data.factory.SessionInventoryFactory
import com.example.wmswherther.data.factory.SupplierFactory
import com.example.wmswherther.data.factory.UserFactory
import com.example.wmswherther.viewModel.MainViewModel
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf.allOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class InventorySession {

    private lateinit var db: MainDB
    private lateinit var repo: InventoryRepository
    private var supplierId : Int = 123123
    private lateinit var inventoryId : String
    lateinit var scenario: ActivityScenario<MainActivity>
    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MainDB::class.java
        ).allowMainThreadQueries().build()
        var dao = db.getDao()
        repo = InventoryRepository(dao)
        ServiceLocator.inventoryRepository = repo

        // 👉 подготовка данных
        runBlocking {
            inventoryId = "11111"
            appendMoveDummyData(dao = dao , supplierId, inventoryId)
            appendFunctionality(dao)
            appendUser(dao)
        }

        scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            val vm = ViewModelProvider(activity)[MainViewModel::class]
            vm.setActiveUi(UiState.InventoryMenu())

            var newFragment = InventoryFragment()

            vm.setActiveUi(UiState.InventoryMenu())

            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, newFragment)
                .commitNow()


        }

    }

    @After
    fun teardown() {
        scenario.close()                      // UI
        db.close()                            // DB
        ServiceLocator.inventoryRepository = null // singleton
    }

    fun loadSession(){
        scenario.onActivity { activity ->
            val vm = ViewModelProvider(activity)[MainViewModel::class]
            vm.setActiveUi(UiState.InventorySessionMenu())

            var newFragment = InventorySessionFragment()

            vm.setActiveUi(UiState.InventorySessionMenu(prevState = vm.uiState.value,
                supplierId = supplierId,
                isSupplierModeActive = true))

            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, newFragment)
                .commitNow()


        }
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
            .perform(clearText(), typeText("A111"))
        onView(withId(R.id.etIncomeBarcode))
            .perform(pressImeActionButton())

        Thread.sleep(500)
    }

    @Test
    fun checkTaskMenu() {
        Thread.sleep(500)
        onView(withText("Vitek"))
            .check(matches(isDisplayed()))
        onView(withText("A111"))
            .check(matches(isDisplayed()))
        onView(withText("Vitek"))
            .perform(click())
        onView(withId(R.id.tvLineCounter))
            .check(matches(withText("0  /")))
        onView(withId(R.id.tvCounterCounter))
            .check(matches(withText("612")))

        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).check(matches(withText("0/13")))

    }
    @Test
    fun checkSupplierMenu() {
        Thread.sleep(500)

        onView(withId(R.id.btnThreeDots))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
            .perform(click())

        onView(withText("Change mode"))
            .inRoot(isPlatformPopup())
            .check(matches(isDisplayed()))
            .perform(click())

        onView(allOf(
            withText("Vitek"),
            isAssignableFrom(MaterialButton::class.java)
        ))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
            .perform(click())

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
            .perform(clearText(), typeText("A111"))


        onView(withId(R.id.etIncomeBarcode))
            .perform(pressImeActionButton())
        Thread.sleep(500)
        onView(withId(R.id.tvLineCounter))
            .check(matches(withText("0  /")))
        onView(withId(R.id.tvCounterCounter))
            .check(matches(withText("612")))

        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).check(matches(withText("0/13")))

    }
    @Test
    fun clickChange() {
        Thread.sleep(500)
        loadSession()


        onView(withId(R.id.tvLineCounter))
            .check(matches(withText("0  /")))
        onView(withId(R.id.tvCounterCounter))
            .check(matches(withText("612")))
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


        Thread.sleep(500)

        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).check(matches(withText("5/13")))

    }

    @Test
    fun barcodeChange() {
        Thread.sleep(500)
        loadSession()


        onView(withId(R.id.tvLineCounter))
            .check(matches(withText("0  /")))
        onView(withId(R.id.tvCounterCounter))
            .check(matches(withText("612")))
        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).check(matches(withText("0/13")))

        onView(withId(R.id.etIncomeBarcode))
            .perform( replaceText("4665453776410"))
            .perform(pressImeActionButton())

        Thread.sleep(500)
        onView(withId(R.id.tvLineCounter))
            .check(matches(withText("1  /")))
        onView(withId(R.id.tvCounterCounter))
            .check(matches(withText("612")))
        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).check(matches(withText("1/13")))




    }
    @Test
    fun selectionCheck() {
        Thread.sleep(500)
        loadSession()


        onView(withId(R.id.tvLineCounter))
            .check(matches(withText("0  /")))
        onView(withId(R.id.tvCounterCounter))
            .check(matches(withText("612")))
        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).check(matches(withText("0/13")))



        onView(withId(R.id.btnThreeDots))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
            .perform(click())

        onView(withText("Select all"))
            .inRoot(isPlatformPopup())
            .check(matches(isDisplayed()))
            .perform(click())


        onView(withId(R.id.tvLineCounter))
            .check(matches(withText("612  /")))
        onView(withId(R.id.tvCounterCounter))
            .check(matches(withText("612")))
        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).check(matches(withText("13/13")))

        onView(withId(R.id.btnThreeDots))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
            .perform(click())

        onView(withText("Deselect all"))
            .inRoot(isPlatformPopup())
            .check(matches(isDisplayed()))
            .perform(click())
        onView(withId(R.id.tvLineCounter))
            .check(matches(withText("0  /")))
        onView(withId(R.id.tvCounterCounter))
            .check(matches(withText("612")))
        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).check(matches(withText("0/13")))


    }
    @Test
    fun checkOverCounter(){
        Thread.sleep(500)
        loadSession()
        onView(withId(R.id.rwInventorySessionList))
            .perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Kettle k520")),
                    click()
                )
            )

        onView(withId(R.id.etSelectedCount))
            .perform(clearText(), typeText("30"))


        onView(withId(R.id.etSelectedCount))
            .perform(pressImeActionButton())

        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k520"))
        )).check(matches(withText("30/23")))
        onView(allOf(
            com.example.wmswherther.withTextColor(-251901),
            hasSibling(withText("Kettle k520")
            )))
            .check(matches(withText("30/23")))
    }
    @Test
    fun finishDiffOk(){
        Thread.sleep(500)
        loadSession()

        onView(withId(R.id.rwInventorySessionList))
            .perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Kettle k510")),
                    click()
                )
            )
        Thread.sleep(500)

        onView(withId(R.id.etSelectedCount))
            .perform(replaceText("13"))


        onView(withId(R.id.etSelectedCount))
            .perform(pressImeActionButton())

        onView(withId(R.id.btnThreeDots))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
            .perform(click())
        onView(withId(R.id.btnFinish))
            .inRoot(isPlatformPopup())
            .check(matches(isDisplayed()))
            .perform(click())

        onView(withId(R.id.btnYes))
            .perform(click())

        var listGoods: List<Pair<Goods, Catalog>>
        var diffs : List< InventoryDiffItem>
        var goods: Pair<Goods, Catalog>
        var listMovement: List<Movement>
        var sessions: List<SessionInventory>
        runBlocking {
            diffs = repo.getDiffs()
            listGoods = repo.getAllGoods().map { Pair(it, repo.getCatalogById(it.catalogId)) }
            listMovement = repo.getAllMovement()
            goods = listGoods.first { it.second.name == "Kettle k520" }
            sessions = repo.getInventorySessions().filter { it.status == StatusType.Finished.ordinal }
        }
        assertEquals(22,diffs.size)
        assertEquals(1,sessions.size)

    }

    @Test
    fun finishDiffLess(){
        Thread.sleep(500)
        loadSession()

        onView(withId(R.id.rwInventorySessionList))
            .perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Kettle k520")),
                    click()
                )
            )

        onView(withId(R.id.etSelectedCount))
            .perform(clearText(), typeText("22"))


        onView(withId(R.id.etSelectedCount))
            .perform(pressImeActionButton())

        onView(withId(R.id.btnThreeDots))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
            .perform(click())
        onView(withId(R.id.btnFinish))
            .inRoot(isPlatformPopup())
            .check(matches(isDisplayed()))
            .perform(click())

        onView(withId(R.id.btnYes))
            .perform(click())

        var listGoods: List<Pair<Goods, Catalog>>
        var diffs : List< InventoryDiffItem>
        var goods: Pair<Goods, Catalog>
        var listMovement: List<Movement>
        var sessions: List<SessionInventory>
        runBlocking {
            diffs = repo.getDiffs()
            listGoods = repo.getAllGoods().map { Pair(it, repo.getCatalogById(it.catalogId)) }
            listMovement = repo.getAllMovement()
            goods = listGoods.first { it.second.name == "Kettle k520" }
            sessions = repo.getInventorySessions().filter { it.status == StatusType.Finished.ordinal }
        }
        assertEquals(23,diffs.size)
        assertEquals(1,sessions.size)

    }


    fun withTextColor(expectedColor: Int): Matcher<View> {
        return object : BoundedMatcher<View, TextView>(TextView::class.java) {

            override fun describeTo(description: Description) {
                description.appendText("with text color: $expectedColor")
            }

            override fun matchesSafely(textView: TextView): Boolean {
                println("${textView.currentTextColor}  ${expectedColor}")
                return textView.currentTextColor == expectedColor
            }
        }
    }
    suspend fun appendFunctionality(dao: Dao) {
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
            supplierId = dao.getAllSuppliers().first().id
        )

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
    private fun appendUser(dao: Dao) {
        var credential = CredentialFactory.create(
            type = "User",
            id = 0
        )
        var credentialId = dao.insertCredential(credential)
        var user = UserFactory.create(
            id = 0,
            fistName = "Pavel",
            lastName = "Semenov",
            credentialId = credentialId
        )
        dao.insertUser(user)
    }
    suspend fun appendMoveDummyData(dao: Dao, supplierId: Int, sessionId: String){
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
            id = sessionId,
            supplierId = vitekSupplier.id,
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