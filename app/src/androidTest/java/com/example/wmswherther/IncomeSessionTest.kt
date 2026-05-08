package com.example.wmswherther

import android.os.Bundle
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
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.BoundedMatcher
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
import com.example.wmswherther.data.db.Entityes.SessionIncome
import com.example.wmswherther.data.db.Entityes.Supplier
import com.example.wmswherther.data.db.Entityes.User
import com.example.wmswherther.data.db.Repositories.IncomeRepository
import com.example.wmswherther.viewModel.MainViewModel
import kotlinx.coroutines.runBlocking
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf.allOf
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import java.util.UUID

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

@RunWith(AndroidJUnit4::class)
class IncomeSessionTest {
    private lateinit var db: MainDB
    private lateinit var repo: IncomeRepository
    private lateinit var sessionId : String
    lateinit var scenario: ActivityScenario<MainActivity>
    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MainDB::class.java
        ).allowMainThreadQueries().build()
        var dao = db.getDao()
        repo = IncomeRepository(dao)
        ServiceLocator.incomeRepository = repo

        // 👉 подготовка данных
        runBlocking {
            sessionId = "123234"
            appendDummyData(dao, sessionId)
            appendFunctionality(db)
            appendUser(db)
        }

        scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            val vm = ViewModelProvider(activity)[MainViewModel::class]
            vm.setActiveUi(UiState.IncomeSessionMenu())

            var newFragment = IncomeSessionFragment()
            val bundle = Bundle().apply {
                putString("id", "123234")//number contain sessionId
            }
            newFragment.arguments = bundle

            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, newFragment)
                .commitNow()


        }

    }

    @After
    fun teardown() {
        scenario.close()                      // UI
        db.close()                            // DB
        ServiceLocator.incomeRepository = null // singleton
    }

    @Test
    fun checkBaseData() {
              // запуск Fragment

        Thread.sleep(500)

        // Проверить те
        onView(withId(R.id.rwIncomeSessionList))
            .perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    hasDescendant(withText("N00000001"))
                ))
        // Проверить обычный элемент
        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k520"))
        )).check(matches(withText("0/23")))
        // Проверить ячейку
        onView(withId(R.id.tvCellName))
            .check(matches(withText("IN-01")))
        // Проверить счетчики
        onView(withId(R.id.tvLineCounter))
            .check(matches(withText("0  /")))
        onView(withId(R.id.tvCounterCounter))
            .check(matches(withText("612")))

    }
    @Test
    fun checkChangeOfCountByClick(){

        Thread.sleep(500)

        onView(withId(R.id.rwIncomeSessionList))
            .perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Kettle k520")),
                    click()
                )
            )

        onView(withId(R.id.etSelectedCount))
            .perform(clearText(), typeText("5"))


        onView(withId(R.id.etSelectedCount))
            .perform(pressImeActionButton())

        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k520"))
        )).check(matches(withText("5/23")))
    }
    @Test
    fun checkChangeOfCountByBarcode(){

        Thread.sleep(500)
        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).check(matches(withText("0/13")))

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
            .perform(clearText(), typeText("4665453776410"))

        onView(withId(R.id.etIncomeBarcode))
            .perform(pressImeActionButton())

        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k510"))
        )).check(matches(withText("1/13")))


    }
    @Test
    fun enterInTE(){
        Thread.sleep(500)

        // Проверить те
        onView(withId(R.id.rwIncomeSessionList))
            .perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    hasDescendant(withText("N00000001"))
                ))
        // Проверить обычный элемент
        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k520"))
        )).check(matches(withText("0/23")))
        // Проверить ячейку
        onView(withId(R.id.tvCellName))
            .check(matches(withText("IN-01")))
        // Проверить счетчики
        onView(withId(R.id.tvLineCounter))
            .check(matches(withText("0  /")))
        onView(withId(R.id.tvCounterCounter))
            .check(matches(withText("612")))

        onView(withText("N00000001"))
            .check(matches(isDisplayed()))
            .perform(click())

        onView(withId(R.id.tvCellName))
            .check(matches(withText("N00000001")))
        // Проверить счетчики
        onView(withId(R.id.tvLineCounter))
            .check(matches(withText("0  /")))
        onView(withId(R.id.tvCounterCounter))
            .check(matches(withText("162")))

        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k550"))
        )).check(matches(withText("0/53")))
        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k551"))
        )).check(matches(withText("0/54")))
        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k552"))
        )).check(matches(withText("0/55")))
        onView(allOf(
            withId(R.id.container), // ID твоего Layout (например, root элемента в item_list.xml)
            hasDescendant(withText("N00000001"))
        )).perform(click())



        // Проверить те
        onView(withId(R.id.rwIncomeSessionList))
            .perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    hasDescendant(withText("N00000001"))
                ))
        // Проверить обычный элемент
        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k520"))
        )).check(matches(withText("0/23")))
        // Проверить ячейку
        onView(withId(R.id.tvCellName))
            .check(matches(withText("IN-01")))
        // Проверить счетчики
        onView(withId(R.id.tvLineCounter))
            .check(matches(withText("0  /")))
        onView(withId(R.id.tvCounterCounter))
            .check(matches(withText("612")))

    }
    @Test
    fun CheckColorIfAboveLimit(){
        Thread.sleep(500)

        onView(withId(R.id.rwIncomeSessionList))
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
            withTextColor(-251901),
            hasSibling(withText("Kettle k520")
        )))
            .check(matches(withText("30/23")))


    }
    @Test
    fun checkTeFeature(){
        Thread.sleep(500)
        onView(withId(R.id.btnBarcode))
            .perform(
                    click()
                )
        onView(withId(R.id.rwIncomeSessionList))
            .perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Kettle k520")),
                    click()
                )
            )

        onView(withId(R.id.etSelectedCount))
            .perform(clearText(), typeText("5"))


        onView(withId(R.id.etSelectedCount))
            .perform(pressImeActionButton())

        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k520"))
        )).check(matches(withText("5/23")))

        onView(allOf(
            withId(R.id.tvTE),
            hasSibling(withText("Kettle k520"))
        )).check(matches(withText("5")))

        onView(withId(R.id.btnBarcode))
            .perform(click())

       onView(withId(R.id.etDialog))
           .perform(click(), typeText("N12312312"))
        onView(withId(R.id.btnYes))
            .perform(click())
        Thread.sleep(400)

        onView(withId(R.id.rwIncomeSessionList))
            .perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    hasDescendant(withText("N12312312"))
                ))
        onView(withId(R.id.tvLineCounter))
            .check(matches(withText("5  /")))

        onView(withText("N12312312"))
            .perform(click())


        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k520"))
        )).check(matches(withText("5/5")))


        onView(withId(R.id.tvLineCounter))
            .check(matches(withText("5  /")))
        onView(withId(R.id.tvCounterCounter))
            .check(matches(withText("5")))
        onView(allOf(
            withId(R.id.container), // ID твоего Layout (например, root элемента в item_list.xml)
            hasDescendant(withText("N12312312"))
        )).perform(click())

        onView(allOf(
            withId(R.id.tvCount),
            hasSibling(withText("Kettle k520"))
        )).check(matches(withText("0/18")))
    }
    @Test
    fun checkFinishWithLess(){
        Thread.sleep(500)

        onView(withId(R.id.rwIncomeSessionList))
            .perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Kettle k520")),
                    click()
                )
            )

        onView(withId(R.id.etSelectedCount))
            .perform(clearText(), typeText("5"))


        onView(withId(R.id.etSelectedCount))
            .perform(pressImeActionButton())

        onView(withId(R.id.btnThreeDots))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
            .perform(click())
        onView(withId(R.id.btnFinishSession))
            .perform(click())
        var listGoods: List<Goods>
        var goods: Goods
        var catalog: Catalog
        var movement: Movement
        var lessMovement: Movement
        var listMovement: List<Movement>
        var session: SessionIncome
        var cellLess: Cell
        var cellIn: Cell
        runBlocking {
            println("######")
            listGoods = repo.getAllGoods()
            listMovement = repo.getAllMovement()
            session = repo.getIncomeSessionById(sessionId)
            goods = listGoods.first()
            catalog = repo.getCatalogById(goods.catalogId)
            cellLess = repo.getCellByName("less")
            cellIn = repo.getCellByName("IN-01")
            movement = listMovement.first { it.goodsId == goods.id  && it.cellToId == cellIn.id}
            lessMovement = listMovement.first { it.goodsId == goods.id  && it.cellToId == cellLess.id}

        }
        println("######")
        println(listMovement.filter { it.goodsId == goods.id })
        assertEquals(1,listGoods.size)
        assertEquals("Kettle k520", catalog.name)
        assertEquals(goods.id, movement.goodsId)
        assertEquals(goods.amount, movement.qty.toInt())
        assertEquals(18, lessMovement.qty.toInt())
        assertEquals(session.status, StatusType.Finished.ordinal)
        assertEquals(2, (listMovement.filter { it.goodsId == goods.id }).size)

    }
    @Test
    fun checkFinishWithMore(){
        Thread.sleep(500)

        onView(withId(R.id.rwIncomeSessionList))
            .perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Kettle k520")),
                    click()
                )
            )

        onView(withId(R.id.etSelectedCount))
            .perform(clearText(), typeText("100"))


        onView(withId(R.id.etSelectedCount))
            .perform(pressImeActionButton())

        onView(withId(R.id.btnThreeDots))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
            .check(matches(isEnabled()))
            .perform(click())
        onView(withId(R.id.btnFinishSession))
            .perform(click())
        var listGoods: List<Goods>
        var goods: Goods
        var catalog: Catalog
        var movement: Movement
        var moreMovement: Movement
        var listMovement: List<Movement>
        var session: SessionIncome
        var cellMore: Cell
        var cellIn: Cell
        runBlocking {
            println("######")
            listGoods = repo.getAllGoods()
            listMovement = repo.getAllMovement()
            session = repo.getIncomeSessionById(sessionId)
            goods = listGoods.first()
            catalog = repo.getCatalogById(goods.catalogId)
            cellMore = repo.getCellByName("more")
            cellIn = repo.getCellByName("IN-01")
            movement = listMovement.first { it.goodsId == goods.id  && it.cellToId == cellIn.id}
            moreMovement = listMovement.first { it.goodsId == goods.id  && it.cellFromId == cellMore.id}

        }
        println("######")
        println(listMovement.filter { it.goodsId == goods.id })
        assertEquals(1,listGoods.size)
        assertEquals("Kettle k520", catalog.name)
        assertEquals(goods.id, movement.goodsId)
        assertEquals(goods.amount, movement.qty.toInt())
        assertEquals(77, moreMovement.qty.toInt())
        assertEquals(session.status, StatusType.Finished.ordinal)
        assertEquals(2, listMovement.filter { it.goodsId == goods.id }.size)

    }
    @Test
    fun checkFinishWithOK(){
        Thread.sleep(500)

        onView(withId(R.id.rwIncomeSessionList))
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
        onView(withId(R.id.btnFinishSession))
            .perform(click())
        var listGoods: List<Goods>
        var goods: Goods
        var catalog: Catalog
        var movement: Movement
        var listMovement: List<Movement>
        var session: SessionIncome
        var cellIn: Cell
        runBlocking {
            println("######")
            listGoods = repo.getAllGoods()
            listMovement = repo.getAllMovement()
            session = repo.getIncomeSessionById(sessionId)
            goods = listGoods.first()
            catalog = repo.getCatalogById(goods.catalogId)
            cellIn = repo.getCellByName("IN-01")
            movement = listMovement.first { it.goodsId == goods.id  && it.cellToId == cellIn.id}

        }
        println("######")
        println(listMovement.filter { it.goodsId == goods.id })
        assertEquals(1,listGoods.size)
        assertEquals("Kettle k520", catalog.name)
        assertEquals(goods.id, movement.goodsId)
        assertEquals(goods.amount, movement.qty.toInt())
        assertEquals(session.status, StatusType.Finished.ordinal)
        assertEquals(1, listMovement.filter { it.goodsId == goods.id }.size)

    }
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