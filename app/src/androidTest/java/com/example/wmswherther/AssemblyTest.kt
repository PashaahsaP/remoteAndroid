package com.example.wmswherther

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
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
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
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
import com.example.wmswherther.Fragments.PickerSessionFragment
import com.example.wmswherther.Fragments.ServiceLocator
import com.example.wmswherther.data.db.Entityes.Barcode
import com.example.wmswherther.data.db.Entityes.Catalog
import com.example.wmswherther.data.db.Entityes.Cell
import com.example.wmswherther.data.db.Entityes.CellType
import com.example.wmswherther.data.db.Entityes.Change
import com.example.wmswherther.data.db.Entityes.Credential
import com.example.wmswherther.data.db.Entityes.Goods
import com.example.wmswherther.data.db.Entityes.IncomeItem
import com.example.wmswherther.data.db.Entityes.Movement
import com.example.wmswherther.data.db.Entityes.PickerItem
import com.example.wmswherther.data.db.Entityes.SessionIncome
import com.example.wmswherther.data.db.Entityes.SessionPicker
import com.example.wmswherther.data.db.Entityes.Supplier
import com.example.wmswherther.data.db.Entityes.User
import com.example.wmswherther.data.db.Repositories.AssemblyRepository
import com.example.wmswherther.data.db.Repositories.IncomeRepository
import com.example.wmswherther.viewModel.MainViewModel
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
class AssemblyTest {
    private lateinit var db: MainDB
    private lateinit var repo: AssemblyRepository
    private lateinit var sessionId : String
    private lateinit var supplierId : String
    lateinit var scenario: ActivityScenario<MainActivity>
    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MainDB::class.java
        ).allowMainThreadQueries().build()
        var dao = db.getDao()
        repo = AssemblyRepository(dao)
        ServiceLocator.assemblyRepository = repo

        // 👉 подготовка данных
        runBlocking {
            sessionId = "123234"
            supplierId = "11111"
            appendPickerDummyData(dao, sessionId, supplierId)
            appendFunctionality(dao)
            appendUser(dao)
        }

        scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            val vm = ViewModelProvider(activity)[MainViewModel::class]
            vm.setActiveUi(UiState.AssemblySessionMenu())

            var newFragment = PickerSessionFragment()
            vm.setActiveUi(UiState.AssemblySessionMenu(prevState = vm.uiState.value, sessionId = sessionId, supplierId = supplierId))

            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, newFragment)
                .commitNow()


        }

    }
    @After
    fun teardown() {
        scenario.close()                      // UI
        db.close()                            // DB
        ServiceLocator.assemblyRepository = null // singleton
    }

    @Test
    fun checkBaseData() {
        // запуск Fragment

        Thread.sleep(500)


        onView(withId(R.id.rwListMain))
            .perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Kettle k510"))
                )
            )
        onView(withId(R.id.rwListMain))
            .perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    hasDescendant(withText("A110"))
                )
            )

        onView(withId(R.id.rwListItem))
            .perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Kettle k510"))
                )
            )
        onView(withId(R.id.rwListItem))
            .perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    hasDescendant(withText("A110"))
                )
            )



    }
    @Test
    fun checkColor(){
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
            .perform(replaceText("A110"))

        onView(withId(R.id.etIncomeBarcode))
            .perform(pressImeActionButton())

        onView(withId(R.id.rwListMain))
            .perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    hasDescendant( allOf(
                        withText("A110"),
                        withTextColor(R.color.regularGrey))),
                ))
        onView(withId(R.id.rwListMain))
            .perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    hasDescendant( allOf(
                        withText("Kettle k510"),
                        withTextColor(R.color.white))),
                ))


        onView(withId(R.id.etIncomeBarcode))
            .perform(replaceText("4665453776410"))

        onView(withId(R.id.etIncomeBarcode))
            .perform(pressImeActionButton())

        onView(withId(R.id.rwListMain))
            .perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    hasDescendant( allOf(
                        withText("A110"),
                        withTextColor(R.color.regularGrey))),
                ))
        onView(withId(R.id.rwListMain))
            .perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    hasDescendant( allOf(
                        withText("Kettle k510"),
                        withTextColor(R.color.regularGrey))),
                ))
        onView(withId(R.id.etCount)).check (matches(withTextColor(R.color.white)))
        onView(withId(R.id.etCount)).perform (pressImeActionButton())

    }
    @Test
    fun countOfGoods(){
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
            .perform(replaceText("A110"))

        onView(withId(R.id.etIncomeBarcode))
            .perform(pressImeActionButton())




        onView(withId(R.id.etIncomeBarcode))
            .perform(replaceText("4665453776410"))

        onView(withId(R.id.etIncomeBarcode))
            .perform(pressImeActionButton())


        onView(withId(R.id.etCount)).check (matches(withTextColor(R.color.white)))
        onView(withId(R.id.etCount)).perform (pressImeActionButton())
        onView(withText("A110"))
            .check(doesNotExist())
        onView(withText("Kettle k510"))
            .check(doesNotExist())
    }
    @Test
    fun checkIsOk(){
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
            .perform(replaceText("A110"))
            .perform(pressImeActionButton())
        onView(withId(R.id.etIncomeBarcode))
            .perform(replaceText("4665453776410"))
            .perform(pressImeActionButton())
        onView(withId(R.id.etCount))
            .perform(pressImeActionButton())


        onView(withId(R.id.etIncomeBarcode))
            .perform(replaceText("A111"))
            .perform(pressImeActionButton())
        onView(withId(R.id.etIncomeBarcode))
            .perform(replaceText("4665453776411"))
            .perform(pressImeActionButton())
        onView(withId(R.id.etCount))
            .perform(pressImeActionButton())
        Thread.sleep(500)

        onView(withId(R.id.etDialog))
            .perform(replaceText("OUT01"))
            .perform(pressImeActionButton())

        onView(withId(R.id.btnYes))
            .perform(click())

        Thread.sleep(500)

        var session: SessionPicker
        var items: List<PickerItem>
        var goods: List<Goods>
        var goodsAndCell: List<Pair<Goods, Cell>>
        var movements: List<Movement>
        var cell: Cell
        runBlocking {
            println("######")
            cell = repo.getCellByName("OUT01")
            session = repo.getPickerSessionById(sessionId)

            var temp = repo.getPickerItemBySessionId(sessionId)
            println(temp)
            items = temp.filter { it.sessionId == sessionId }
            goods = items.map { repo.getGoodsById(it.goodsId) }
            goodsAndCell = goods.map { Pair(it, repo.getCellById(it.cellId.toString())) }
            movements = repo.getAllMovements()
        }
        println("######")
        assertEquals(StatusType.Finished.ordinal.toString(), session.status)
        println(goods)
        assertEquals(true, goodsAndCell.all { it.second.name == cell.name })
        assertEquals(true, goodsAndCell.all { movements.filter { inner -> inner.goodsId == it.first.id }.size == 1 })
    }
   /* @Test
    fun checkIsLess(){
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
            .perform(replaceText("A110"))
            .perform(pressImeActionButton())
        onView(withId(R.id.etIncomeBarcode))
            .perform(replaceText("4665453776410"))
            .perform(pressImeActionButton())
        onView(withId(R.id.etCount))
            .perform(replaceText("3"))
            .perform(pressImeActionButton())
        Thread.sleep(500)
        onView(withId(R.id.btnYes))
            .perform(click())


        onView(withId(R.id.etIncomeBarcode))
            .perform(replaceText("A111"))
            .perform(pressImeActionButton())
        onView(withId(R.id.etIncomeBarcode))
            .perform(replaceText("4665453776411"))
            .perform(pressImeActionButton())
        onView(withId(R.id.etCount))
            .perform(pressImeActionButton())
        Thread.sleep(500)

        onView(withId(R.id.etDialog))
            .perform(replaceText("OUT01"))
            .perform(pressImeActionButton())

        onView(withId(R.id.btnYes))
            .perform(click())


        var session: SessionPicker
        var items: List<PickerItem>
        var goods: List<Goods>
        var goodsAndCellAndMovement: List<Triple<Goods, Cell, List<Movement>>>
        var movements: List<Movement>
        var movementsForTwo: List<Movement>
        var movementsForOne: List<Movement>
        var cell: Cell
        var cellLess: Cell
        runBlocking {
            println("######")
            session = repo.getPickerSessionById(sessionId)
            items = repo.getPickerItems().filter { it.sessionId == sessionId }
            goods = items.map { repo.getGoodsById(it.goodsId) }
            goodsAndCellAndMovement = goods.map { Pair(it, repo.getCellById(it.cellId.toString())) }.map { Triple(it.first, it.second,repo.getAllMovements().filter { item -> item.goodsId ==  it.first.id})  }
            cell = repo.getCellByName("OUT01")
            cellLess = repo.getCellByName("less")
            movements = repo.getAllMovements()
            movementsForOne = goodsAndCellAndMovement.filter { it.third.size == 1 }.flatMap { it -> it.third }
            movementsForTwo = goodsAndCellAndMovement.filter { it.third.size == 2 }.flatMap { it -> it.third }
        }
        println("######")
        assertEquals(StatusType.Finished.ordinal.toString(), session.status)
        println(goods)
        assertEquals(true, goodsAndCellAndMovement.all { it.second.name == cell.name })
        assertEquals(13, movementsForOne.sumOf { it.qty.toInt() })
        assertEquals(14, movementsForTwo.sumOf { it.qty.toInt() })
        assertEquals(3, movementsForTwo.size + movementsForOne.size == 3)
        assertEquals(cell.id, movementsForTwo.sortedBy { it.executedAt }.first().cellToId)
        assertEquals(cellLess.id, movementsForTwo.sortedBy { it.executedAt }.last().cellToId)
        assertEquals(2, movementsForTwo.size)

    }
    @Test
    fun checkIsMove(){
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
            .perform(replaceText("A110"))
            .perform(pressImeActionButton())
        onView(withId(R.id.etIncomeBarcode))
            .perform(replaceText("4665453776410"))
            .perform(pressImeActionButton())
        onView(withId(R.id.etCount))
            .perform(pressImeActionButton())


        onView(withId(R.id.etIncomeBarcode))
            .perform(replaceText("A111"))
            .perform(pressImeActionButton())
        onView(withId(R.id.etIncomeBarcode))
            .perform(replaceText("4665453776411"))
            .perform(pressImeActionButton())
        onView(withId(R.id.etCount))
            .perform(pressImeActionButton())
        Thread.sleep(500)

        onView(withId(R.id.etDialog))
            .perform(replaceText("OUT01"))
            .perform(pressImeActionButton())

        onView(withId(R.id.btnYes))
            .perform(click())


        var session: SessionPicker
        var items: List<PickerItem>
        var goods: List<Goods>
        var goodsAndCellAndMovement: List<Triple<Goods, Cell, List<Movement>>>
        var movements: List<Movement>
        var movementsForTwo: List<Movement>
        var movementsForOne: List<Movement>
        var cell: Cell
        var cellMore: Cell
        runBlocking {
            println("######")
            session = repo.getPickerSessionById(sessionId)
            items = repo.getPickerItems()
            goods = items.map { repo.getGoodsById(it.goodsId) }
            goodsAndCellAndMovement = goods.map { Pair(it, repo.getCellById(it.cellId.toString())) }.map { Triple(it.first, it.second,repo.getAllMovements().filter { item -> item.goodsId ==  it.first.id})  }
            cell = repo.getCellByName("OUT01")
            cellMore = repo.getCellByName("more")
            movements = repo.getAllMovements()
            movementsForOne = goodsAndCellAndMovement.filter { it.third.size == 1 }.flatMap { it -> it.third }
            movementsForTwo = goodsAndCellAndMovement.filter { it.third.size == 2 }.flatMap { it -> it.third }
        }
        println("######")
        assertEquals(StatusType.Finished.ordinal.toString(), session.status)
        println(goods)
        assertEquals(true, goodsAndCellAndMovement.all { it.second.name == cell.name })
        assertEquals(13, movementsForOne.sumOf { it.qty.toInt() })
        assertEquals(14, movementsForTwo.sumOf { it.qty.toInt() })
        assertEquals(cell.id, movementsForTwo.sortedBy { it.executedAt }.first().cellToId)
        assertEquals(cellMore.id, movementsForTwo.sortedBy { it.executedAt }.last().cellFromId)
        assertEquals(2, movementsForTwo.size)

    }*/






    fun withTextColor(expectedColor: Int): Matcher<View> {
        return object : BoundedMatcher<View, TextView>(TextView::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("with text color: $expectedColor ###### $expectedColor"  )
            }

            override fun matchesSafely(textView: TextView): Boolean {
                val expected = ContextCompat.getColor(
                    textView.context,
                    expectedColor
                )
                return textView.currentTextColor == expected
            }
        }
    }
    suspend fun appendPickerDummyData(dao: Dao, sessionId: String, supplierId: String) {
        var type = CellType(
            id = UUID.randomUUID().toString(),
            type = "Outcome",
            mask = "OUT##",
            other = null
        )
        dao.insertCellType(type)

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
        var A100 = Cell(
            UUID.randomUUID().toString(),
            pickerType.id,
            null,
            "A100"
        )
        var cellChange = Change(
            UUID.randomUUID().toString(),
            A100.id,
            OperationType.InsertCell.ordinal,
            StatusType.Created.ordinal,
            null,
            null
        )
        dao.insertCellSync(A100, cellChange)

        var N00000001 = Cell(
            UUID.randomUUID().toString(),
            teType.id,
            A100.id,
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

        var pickerSession = SessionPicker(
            id = sessionId,
            supplierId = vitekSupplier.id,
            outCellId = A100.id,
            status = StatusType.Created.ordinal.toString(),
            createdAt = System.currentTimeMillis(),
            startedAt = System.currentTimeMillis(),
            finishedAt = System.currentTimeMillis(),
            other = null
        )
        var pickerChange = Change(
            UUID.randomUUID().toString(),
            pickerSession.id,
            OperationType.InsertPickerSession.ordinal,
            StatusType.Created.ordinal,
            vitekSupplier.id,
            null
        )
        dao.insertPickerSessionAsync(pickerSession, pickerChange)
// <editor-fold desc="te first">
        /* var catalog = Catalog(
             UUID.randomUUID().toString(),
             "Kettle k5345",
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
             "4665453776456",
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
         // </editor-fold>
         // <editor-fold desc="insert goods">
         var goods = Goods(
             id = UUID.randomUUID().toString(),
             amount = 21,
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
         // </editor-fold>
         // <editor-fold desc="insert pickerItem">
         var pickerItem = PickerItem(
             id = UUID.randomUUID().toString(),
             sessionId = pickerSession.id,
             goodsId = goods.id,
             cellId = N00000001.id,
             status = StatusType.Created.ordinal,
             startedAt = System.currentTimeMillis(),
             finishedAt = System.currentTimeMillis(),
             other = null
         )
         pickerChange = Change(
             UUID.randomUUID().toString(),
             pickerItem.id,
             OperationType.InsertGoods.ordinal,
             status = StatusType.Created.ordinal,
             vitekSupplier.id,
             null
         )
         dao.insertPickerItemAsync(pickerItem, pickerChange)*/
// </editor-fold>
        for (enum in 10..11) {
            // <editor-fold desc="insert cell">
            var A111 = Cell(
                UUID.randomUUID().toString(),
                pickerType.id,
                null,
                "A1${enum}"
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
            // </editor-fold>
            // <editor-fold desc="insert catalog and barcode">
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
            // </editor-fold>
            // <editor-fold desc="insert goods">
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
            // </editor-fold>
            // <editor-fold desc="insert pickerItem">
            var pickerItem = PickerItem(
                id = UUID.randomUUID().toString(),
                sessionId = pickerSession.id,
                goodsId = goods.id,
                cellId = A111.id,
                status = StatusType.Created.ordinal,
                startedAt = System.currentTimeMillis(),
                finishedAt = System.currentTimeMillis(),
                other = null
            )
            var pickerChange = Change(
                UUID.randomUUID().toString(),
                pickerItem.id,
                OperationType.InsertGoods.ordinal,
                status = StatusType.Created.ordinal,
                vitekSupplier.id,
                null
            )
            dao.insertPickerItemAsync(pickerItem, pickerChange)
            // </editor-fold>
        }

        /*for(enum in 50..52){
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
            var pickerItem = PickerItem(
                id = UUID.randomUUID().toString(),
                sessionId = pickerSession.id,
                goodsId = goods.id,
                cellId = N00000001.id,
                status = StatusType.Created.ordinal,
                startedAt = System.currentTimeMillis(),
                finishedAt = System.currentTimeMillis(),
                other = null
            )
            var pickerChange = Change(
                UUID.randomUUID().toString(),
                pickerItem.id,
                OperationType.InsertGoods.ordinal,
                status = StatusType.Created.ordinal,
                vitekSupplier.id,
                null
            )
            dao.insertPickerItemAsync(pickerItem, pickerChange)
        }*/


    }

    suspend fun appendFunctionality(dao: Dao) {
        var cellType = CellType(
            id = UUID.randomUUID().toString(),
            type = "Movement",
            mask = "",
            other = null
        )
        var opTye = CellType(
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
        var catalogTE = Catalog(
            id = "",
            name = "TE",
            sku = "TE",
            supplierId = db.getDao().getAllSuppliers().first().id,
            other = null
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

    suspend fun appendUser(dao: Dao) {
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
        dao.insertUser(user)
    }
}




