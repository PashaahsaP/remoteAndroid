package com.example.wmswherther.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wmswherther.data.db.Entityes.Cell
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.data.enums.OperationType
import com.example.wmsRemote.data.enums.StatusType
import com.example.wmsRemote.databinding.FragmentIncomeBinding
import com.example.wmswherther.Adapters.IncomeMenuAdapter
import com.example.wmswherther.Classes.TaskMenuItem
import com.example.wmswherther.data.db.Entityes.Barcode
import com.example.wmswherther.data.db.Entityes.Catalog
import com.example.wmswherther.data.db.Entityes.CellType
import com.example.wmswherther.data.db.Entityes.Change
import com.example.wmswherther.data.db.Entityes.Goods
import com.example.wmswherther.data.db.Entityes.IncomeItem
import com.example.wmswherther.data.db.Entityes.PickerItem
import com.example.wmswherther.data.db.Entityes.SessionIncome
import com.example.wmswherther.data.db.Entityes.SessionInventory
import com.example.wmswherther.data.db.Entityes.SessionPicker
import com.example.wmswherther.data.db.Entityes.Supplier
import com.example.wmswherther.viewModel.IncomeMenuViewModel
import com.example.wmswherther.viewModel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
                appendPickerDummyData(MainDB.getDB(requireActivity()))
            }
        }
        //viewModel.setCurrFragment(this)
        val localViewModel = ViewModelProvider(requireActivity()).get(IncomeMenuViewModel::class)
        _binding = FragmentIncomeBinding.inflate(inflater)
        var adapter = IncomeMenuAdapter(listOf(), this, viewModel)
        var recyclerView: RecyclerView = binding.rwIncomeMenu
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.adapter = adapter

        localViewModel.tasksList.observe(requireActivity(), Observer { items ->
            adapter.updateMenuItems(items)
        })

        lifecycleScope.launch {
            var data : List<TaskMenuItem> = listOf()
            withContext(Dispatchers.IO) {
                data = localViewModel.updateSupplierList(MainDB.getDB(requireActivity()))
            }
            withContext(Dispatchers.Main) {
                localViewModel.setTaskCollection(data)
            }
        }



        return  binding.root
    }
}

fun appendDummyData(db: MainDB){
    val dao = db.getDao()
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
        UUID.randomUUID().toString(),
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
            id =UUID.randomUUID().toString(),
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
fun appendMoveDummyData(db: MainDB){
    val dao = db.getDao()
    val vitekSupplier = Supplier(
        UUID.randomUUID().toString(),
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
        UUID.randomUUID().toString(),
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
fun appendPickerDummyData(db: MainDB){
    val dao = db.getDao()
    val vitekSupplier = Supplier(
        UUID.randomUUID().toString(),
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
        id = UUID.randomUUID().toString(),
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
    for (enum in 10 .. 11){
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