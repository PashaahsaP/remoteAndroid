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
import com.example.wmswherther.viewModel.MainViewModel
import com.google.android.material.button.MaterialButton
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
    private lateinit var supplierId : String
    private lateinit var inventoryId : String
    lateinit var scenario: ActivityScenario<MainActivity>
    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MainDB::class.java
        ).allowMainThreadQueries().build()

        repo = InventoryRepository(db.getDao())
        ServiceLocator.inventoryRepository = repo

        // 👉 подготовка данных
        runBlocking {
            supplierId = "123234"
            inventoryId = "11111"
            appendMoveDummyData(db, supplierId, inventoryId)
            appendFunctionality(db)
            appendUser(db)
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
                    hasDescendant(withText("Kettle k520")),
                    click()
                )
            )

        onView(withId(R.id.etSelectedCount))
            .perform(clearText(), typeText("23"))


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
    suspend fun appendMoveDummyData(db: MainDB, supplierId: String, inventoryId: String){
        val dao = db.getDao()
        val vitekSupplier = Supplier(
            supplierId,
            "Vitek",
            null
        )

        dao.insertSupplier(vitekSupplier)

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
        val pickerType = CellType(
            UUID.randomUUID().toString(),
            "Picker",
            "*###",
            null
        )
        dao.insertCellType(pickerType)

        var A111 = Cell(
            UUID.randomUUID().toString(),
            pickerType.id,
            null,
            "A111"
        )
        var cellChange = Change(
            UUID.randomUUID().toString(),
            A111.id,
            OperationType.InsertCell.ordinal,
            StatusType.Created.ordinal,
            null,
            null
        )
        dao.insertCellSync(A111, cellChange)
        var N00000001 = Cell(
            UUID.randomUUID().toString(),
            teType.id,
            A111.id,
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

        var inventoryTask = SessionInventory(
            inventoryId,
            vitekSupplier.id,
            A111.id,
            null,
            StatusType.Created.ordinal,
            System.currentTimeMillis(),
            null,
            null,
            null
        )
        var inventoryChange = Change(
            UUID.randomUUID().toString(),
            inventoryTask.id,
            OperationType.InsertInventorySession.ordinal,
            StatusType.Created.ordinal,
            vitekSupplier.id,
            null
        )
        dao.insertInventorySessionAsync(inventoryTask, inventoryChange)

        for (enum in 10 .. 29){
            var catalog = Catalog(
                UUID.randomUUID().toString(),
                "Kettle k5${enum}",
                "3241223",
                vitekSupplier.id,
                null
            )
            var catalogChange = Change(
                UUID.randomUUID().toString(),
                catalog.id,
                OperationType.InsertCatalog.ordinal,
                StatusType.Created.ordinal,
                vitekSupplier.id,
                null
            )

            var barcode = Barcode(
                UUID.randomUUID().toString(),
                "46654537764${enum}",
                catalog.id,
                vitekSupplier.id,
                null
            )
            var barcodeChanges = Change(
                UUID.randomUUID().toString(),
                barcode.id,
                OperationType.InsertBarcode.ordinal,
                StatusType.Created.ordinal,
                vitekSupplier.id,
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
                isAvailable = true,
                other = null
            )
            var goodsChange = Change(
                UUID.randomUUID().toString(),
                goods.id,
                OperationType.InsertGoods.ordinal,
                status = StatusType.Created.ordinal,
                vitekSupplier.id,
                null
            )
            dao.insertGoodsAsync(goods, goodsChange)
        }

        for(enum in 50..52){
            var catalog = Catalog(
                UUID.randomUUID().toString(),
                "Kettle k5${enum}",
                "3241223",
                vitekSupplier.id,
                null
            )
            var catalogChange = Change(
                UUID.randomUUID().toString(),
                catalog.id,
                OperationType.InsertCatalog.ordinal,
                StatusType.Created.ordinal,
                vitekSupplier.id,
                null
            )

            var barcode = Barcode(
                UUID.randomUUID().toString(),
                "46654537764${enum}",
                catalog.id,
                vitekSupplier.id,
                null
            )
            var barcodeChanges = Change(
                UUID.randomUUID().toString(),
                barcode.id,
                OperationType.InsertBarcode.ordinal,
                StatusType.Created.ordinal,
                vitekSupplier.id,
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
                isAvailable = true,
                other = null
            )
            var goodsChange = Change(
                UUID.randomUUID().toString(),
                goods.id,
                OperationType.InsertGoods.ordinal,
                status = StatusType.Created.ordinal,
                vitekSupplier.id,
                null
            )
            dao.insertGoodsAsync(goods, goodsChange)
        }


    }

    private fun appendUser(db: MainDB) {
        var credential = Credential(
            id = 0,
            type = "User",
            other = null
        )
        var credentialId = db.getDao().insertCredential(credential)
        var user = User(
            id = 0,
            firstName = "Pavel",
            lastName = "Semenov",
            credentialId = credentialId,
            other = null
        )
        db.getDao().insertUser(user)
    }
    suspend fun appendFunctionality(db: MainDB) {
        var cellType  = CellType(
            id = UUID.randomUUID().toString(),
            type = "Movement",
            mask = "",
            other = null
        )
        var opTye  = CellType(
            id = UUID.randomUUID().toString(),
            type = "OperationType",
            mask = "",
            other = null
        )
        var income = Cell(
            id = UUID.randomUUID().toString(),
            typeCellId = opTye.id,
            parentCellId = null,
            name = "income"
        )
        var outcome = Cell(
            id = UUID.randomUUID().toString(),
            typeCellId = opTye.id,
            parentCellId = null,
            name = "outcome"
        )
        var cellLess = Cell(
            id = UUID.randomUUID().toString(),
            typeCellId = cellType.id,
            parentCellId = null,
            name = "less"
        )
        var cellMore = Cell(
            id = UUID.randomUUID().toString(),
            typeCellId = cellType.id,
            parentCellId = null,
            name = "more"
        )
        var cellSplit = Cell(
            id = UUID.randomUUID().toString(),
            typeCellId = cellType.id,
            parentCellId = null,
            name = "split"
        )
        var cellMerge = Cell(
            id = UUID.randomUUID().toString(),
            typeCellId = cellType.id,
            parentCellId = null,
            name = "merge"
        )
        var catalogTE  = Catalog(
            id = "",
            name = "TE",
            sku = "TE",
            supplierId = db.getDao().getAllSuppliers().first().id,
            other = null
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
}