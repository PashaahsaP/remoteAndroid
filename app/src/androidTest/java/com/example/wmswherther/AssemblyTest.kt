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
import com.example.wmswherther.data.factory.BarcodeFactory
import com.example.wmswherther.data.factory.CatalogFactory
import com.example.wmswherther.data.factory.CellFactory
import com.example.wmswherther.data.factory.CellTypeFactory
import com.example.wmswherther.data.factory.ChangeFactory
import com.example.wmswherther.data.factory.CredentialFactory
import com.example.wmswherther.data.factory.GoodsFactory
import com.example.wmswherther.data.factory.IncomeItemFactory
import com.example.wmswherther.data.factory.PickerItemFactory
import com.example.wmswherther.data.factory.SessionIncomeFactory
import com.example.wmswherther.data.factory.SessionPickerFactory
import com.example.wmswherther.data.factory.SupplierFactory
import com.example.wmswherther.data.factory.UserFactory
import com.example.wmswherther.viewModel.MainViewModel
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
        assertEquals(StatusType.Finished.ordinal, session.status)
        println(goods)
        assertEquals(true, goodsAndCell.all { it.second.name == cell.name })
        assertEquals(true, goodsAndCell.all { movements.filter { inner -> inner.goodsId == it.first.id }.size == 1 })
    }







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
    suspend fun appendPickerDummyData(dao: Dao, sessionId: String, supplierId: String){
        var type = CellTypeFactory.create(
            type = "Outcome",
            mask = "OUT##"
        )
        dao.insertCellType(type)

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
        var A100 = CellFactory.create(
            typeCellId = pickerType.id,
            parentCellId = null,
            name = "A100"
        )
        var cellChange = ChangeFactory.create(
            payload = Gson().toJson(A100),
            entityId = A100.id,
            supplierId = null,
            operationType = OperationType.InsertCell
        )
        dao.insertCellSync(A100, cellChange)

        var N00000001 = CellFactory.create(
            typeCellId = teType.id,
            parentCellId = A100.id,
            name = "N00000001"
        )
        var teChange = ChangeFactory.create(
            payload = Gson().toJson(N00000001),
            entityId = N00000001.id,
            supplierId = null,
            operationType = OperationType.InsertCell
        )
        dao.insertCellSync(N00000001, teChange)

        var pickerSession = SessionPicker(
            id = sessionId,
            supplierId = supplierId,
            outCellId = A100.id,
            status = StatusType.Created.ordinal,
            createdAt = System.currentTimeMillis(),
            startedAt = System.currentTimeMillis(),
            finishedAt = null,
            updatedAt = System.currentTimeMillis(),
            deletedAt = null,
            isDeleted = false,
            other = null
        )
        var pickerChange = ChangeFactory.create(
            payload = Gson().toJson(pickerSession),
            entityId = pickerSession.id,
            supplierId = vitekSupplier.id,
            operationType = OperationType.InsertPickerSession
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
        for (enum in 10 .. 11){
            // <editor-fold desc="insert cell">
            var A111 = CellFactory.create(
                typeCellId = pickerType.id,
                parentCellId = null,
                name = "A1${enum}"
            )
            var cellChange = ChangeFactory.create(
                payload = Gson().toJson(A111),
                entityId = A111.id,
                supplierId = null,
                operationType = OperationType.InsertCell
            )
            dao.insertCellSync(A111, cellChange)
            // </editor-fold>
            // <editor-fold desc="insert catalog and barcode">
            var catalog = CatalogFactory.create(
                name = "Kettle k5${enum}",
                sku = "3241223",
                supplierId = vitekSupplier.id
            )
            var catalogChange = ChangeFactory.create(
                payload = Gson().toJson(catalog),
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
                entityId = barcode.id,
                supplierId = vitekSupplier.id,
                operationType = OperationType.InsertBarcode
            )
            dao.insertCatalogSync(catalog, catalogChange)
            dao.insertBarcodeAsync(barcode, barcodeChanges)
            // </editor-fold>
            // <editor-fold desc="insert goods">
            var goods = GoodsFactory.create(
                amount = 3 + enum,
                cellId = A111.id,
                catalogId = catalog.id,
                isAvailable = true
            )
            var goodsChange = ChangeFactory.create(
                payload = Gson().toJson(goods),
                entityId = goods.id,
                supplierId = vitekSupplier.id,
                operationType = OperationType.InsertGoods
            )
            dao.insertGoodsAsync(goods, goodsChange)
            // </editor-fold>
            // <editor-fold desc="insert pickerItem">
            var pickerItem = PickerItemFactory.create(
                sesionId = pickerSession.id,
                goodsId = goods.id,
                cellId = A111.id
            )
            var pickerChange = ChangeFactory.create(
                payload = Gson().toJson(pickerItem),
                entityId = pickerItem.id,
                supplierId = vitekSupplier.id,
                operationType = OperationType.InsertGoods
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
}




