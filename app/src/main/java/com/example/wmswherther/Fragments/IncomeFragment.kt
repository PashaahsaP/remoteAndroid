package com.example.wmswherther.Fragments

import android.graphics.Path
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.wmswherther.data.db.Entityes.Cell
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.data.enums.OperationType
import com.example.wmsRemote.data.enums.StatusType
import com.example.wmsRemote.databinding.FragmentIncomeBinding
import com.example.wmsRemote.models.client
import com.example.wmswherther.Adapters.IncomeMenuAdapter
import com.example.wmswherther.Classes.TaskMenuItem
import com.example.wmswherther.data.db.Entityes.Barcode
import com.example.wmswherther.data.db.Entityes.Catalog
import com.example.wmswherther.data.db.Entityes.CellType
import com.example.wmswherther.data.db.Entityes.Change
import com.example.wmswherther.data.db.Entityes.Credential
import com.example.wmswherther.data.db.Entityes.Goods
import com.example.wmswherther.data.db.Entityes.IncomeItem
import com.example.wmswherther.data.db.Entityes.PickerItem
import com.example.wmswherther.data.db.Entityes.SessionIncome
import com.example.wmswherther.data.db.Entityes.SessionInventory
import com.example.wmswherther.data.db.Entityes.SessionPicker
import com.example.wmswherther.data.db.Entityes.Supplier
import com.example.wmswherther.data.db.Entityes.User
import com.example.wmswherther.data.db.PullItem
import com.example.wmswherther.data.db.Repositories.IncomeRepository
import com.example.wmswherther.data.db.Request
import com.example.wmswherther.data.db.SyncWorker
import com.example.wmswherther.data.enums.Entities
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
import com.example.wmswherther.data.factory.SessionInventoryFactory
import com.example.wmswherther.data.factory.SessionPickerFactory
import com.example.wmswherther.data.factory.SupplierFactory
import com.example.wmswherther.data.factory.UserFactory
import com.example.wmswherther.viewModel.IncomeMenuViewModel
import com.example.wmswherther.viewModel.MainViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.util.UUID
import kotlin.getValue

class IncomeFragment : Fragment() {

    private var _binding: FragmentIncomeBinding? = null
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding for FragmentIncome")
    private val viewModel: MainViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        lifecycleScope.launch {
        withContext(Dispatchers.IO){

                //appendDummyData(MainDB.getDB(requireActivity()))
                //appendMoveDummyData(MainDB.getDB(requireActivity()))
               // appendPickerDummyData(MainDB.getDB(requireActivity()))
                //appendUser(MainDB.getDB(requireActivity()))
               //appendFunctionality(MainDB.getDB(requireActivity()))
            }
        }
        //viewModel.setCurrFragment(this)
        val localViewModel = ViewModelProvider(requireActivity()).get(IncomeMenuViewModel::class)
        _binding = FragmentIncomeBinding.inflate(inflater)
        var adapter = IncomeMenuAdapter(listOf(), this, viewModel)
        var recyclerView: RecyclerView = binding.rwIncomeMenu
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.adapter = adapter
        var incomeRepo = IncomeRepository(dao = MainDB.getDB(requireActivity()).getDao())

        localViewModel.tasksList.observe(requireActivity(), Observer { items ->
            adapter.updateMenuItems(items)
        })

        lifecycleScope.launch {
            var data : List<TaskMenuItem> = listOf()
            withContext(Dispatchers.IO) {
                var request: Request = Request()
                data = localViewModel.updateSupplierList(incomeRepo)
                //pullChanges1(requireActivity(), listOf(PullItem(Entities.Supplier,234),PullItem(Entities.Catalog, 235235)))
                pullChanges(requireActivity())
            }
            withContext(Dispatchers.Main) {
                localViewModel.setTaskCollection(data)
            }
        }



        return  binding.root
    }


}

suspend fun appendDummyData(db: MainDB){
    val dao = db.getDao()
    val borkSupplier = SupplierFactory.create("Bork", 0)
    val atomySupplier = SupplierFactory.create("Atomy", 1)
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

    var session = SessionIncomeFactory.create(
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
suspend fun appendMoveDummyDataa(db: MainDB){
    val dao = db.getDao()
    val vitekSupplier = SupplierFactory.create("Vitek",2)
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
        entityId = N00000001.id,
        supplierId = null,
        operationType = OperationType.InsertCell
    )

    dao.insertCellSync(N00000001, teChange)

    var inventoryTask = SessionInventoryFactory.create(
        supplierId = vitekSupplier.id,
        cellId = A111.id,
        prevSessionId = null
    )
    var inventoryChange = ChangeFactory.create(
        payload = Gson().toJson(inventoryTask),
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
    }

    for(enum in 50..52){
        var catalog = CatalogFactory.create(
            name = "Kettle k5${enum}",
            sku = "3241223",
            supplierId = vitekSupplier.id
        )
        var catalogChange = ChangeFactory.create(
            payload = Gson().toJson(catalog.id),
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


        var goods = GoodsFactory.create(
            amount = 3 + enum,
            cellId = N00000001.id,
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
    }


}
suspend fun appendMoveDummyData(db: MainDB){
    val dao = db.getDao()
    val vitekSupplier = SupplierFactory.create("Vitek",6)
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

    dao.insertCell(A111)
    var N00000001 = CellFactory.create(
        typeCellId = teType.id,
        parentCellId = A111.id,
        name = "N00000001"
    )


    dao.insertCell(N00000001)

    var inventoryTask = SessionInventoryFactory.create(
        supplierId = vitekSupplier.id,
        cellId = A111.id,
        prevSessionId = null
    )

    dao.insertInventorySession(inventoryTask)

    for (enum in 10 .. 29){
        var catalog = CatalogFactory.create(
            name = "Kettle k5${enum}",
            sku = "3241223",
            supplierId = vitekSupplier.id
        )

        var barcode = BarcodeFactory.create(
            name = "46654537764${enum}",
            catalogId = catalog.id,
            supplierId = vitekSupplier.id
        )

        dao.insertCatalog(catalog)
        dao.insertBarcode(barcode)


        var goods = GoodsFactory.create(
            amount = 3 + enum,
            cellId = A111.id,
            catalogId = catalog.id,
            isAvailable = true
        )

        dao.insertGoods(goods)
    }

    for(enum in 50..52){
        var catalog = CatalogFactory.create(
            name = "Kettle k5${enum}",
            sku = "3241223",
            supplierId = vitekSupplier.id
        )

        var barcode = BarcodeFactory.create(
            name = "46654537764${enum}",
            catalogId = catalog.id,
            supplierId = vitekSupplier.id
        )

        dao.insertCatalog(catalog)
        dao.insertBarcode(barcode)


        var goods = GoodsFactory.create(
            amount = 3 + enum,
            cellId = N00000001.id,
            catalogId = catalog.id,
            isAvailable = true
        )

        dao.insertGoods(goods)
    }


}
suspend fun appendPickerDummyData(db: MainDB){
    val dao = db.getDao()
    var type = CellTypeFactory.create(
        type = "Outcome",
        mask = "OUT##"
    )
    dao.insertCellType(type)

    val vitekSupplier = SupplierFactory.create("Vitek",4)
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

    var pickerSession =  SessionPickerFactory.create(
        supplierId = vitekSupplier.id,
        outCellId = A100.id
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
/*private fun pullChanges1(requireActivity: FragmentActivity, data: List<PullItem>) {
    val data = Data.Builder()
        .putString("sync_type", "PULL")
        .putString("pullData", Gson().toJson(data))
        .build()

    val request =
        OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(data)
            .build()

    WorkManager.getInstance(requireActivity)
        .enqueue(request)
}*/
private fun pullChanges(requireActivity: FragmentActivity) {
    val data = Data.Builder()
        .putString("sync_type", "FULL")
        .build()

    val request =
        OneTimeWorkRequestBuilder<SyncWorker>()
            .setInputData(data)
            .build()

    WorkManager.getInstance(requireActivity)
        .enqueue(request)
}