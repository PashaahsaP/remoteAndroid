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
import com.example.wmsRemote.data.db.Cell
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.data.enums.OperationType
import com.example.wmsRemote.data.enums.StatusType
import com.example.wmsRemote.databinding.FragmentIncomeBinding
import com.example.wmswherther.Adapters.IncomeMenuAdapter
import com.example.wmswherther.Classes.TaskMenuItem
import com.example.wmswherther.data.db.Barcode
import com.example.wmswherther.data.db.Catalog
import com.example.wmswherther.data.db.CellType
import com.example.wmswherther.data.db.Change
import com.example.wmswherther.data.db.Goods
import com.example.wmswherther.data.db.IncomeItem
import com.example.wmswherther.data.db.SessionIncome
import com.example.wmswherther.data.db.Supplier
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
       /* lifecycleScope.launch {
            withContext(Dispatchers.IO){
                appendDummyData(MainDB.getDB(requireActivity()))
            }
        }*/

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
        "#####",
        null
    )
    dao.insertCellType(incomeType)

    var IN01 = Cell(
        UUID.randomUUID().toString(),
        incomeType.id,
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
    val pickerType = CellType(
        UUID.randomUUID().toString(),
        "Picker",
        "####",
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

    for (enum in 0 .. 9){
        var A111 = Cell(
            UUID.randomUUID().toString(),
            pickerType.id,
            "A11${enum}"
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
            "Kettle k51${enum}",
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
            "466545377645${enum}",
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
            UUID.randomUUID().toString(),
            3 + enum,
            IN01.id,
            catalog.id,
            System.currentTimeMillis(),
            null
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