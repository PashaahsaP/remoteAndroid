package com.example.wmswherther.Fragments

import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.Adapters.AssemblySessionAdapter
import com.example.wmsRemote.Adapters.AssemblySessionMainAdapter
import com.example.wmsRemote.R
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.data.enums.AssemblySessionMenuType
import com.example.wmsRemote.data.enums.StatusType
import com.example.wmsRemote.databinding.ActivityAssemblyBinding
import com.example.wmsRemote.viewModel.AssemblySessionViewModel
import com.example.wmswherther.Classes.AssemblyItem
import com.example.wmswherther.Classes.UiState
import com.example.wmswherther.viewModel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PickerSessionFragment: Fragment() {

    private var _binding: ActivityAssemblyBinding? = null
    private lateinit var localViewModel: AssemblySessionViewModel
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding for AssemblyMain")
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val db = MainDB.getDB(requireActivity())
        _binding = ActivityAssemblyBinding.inflate(layoutInflater)
        localViewModel = ViewModelProvider(this).get(AssemblySessionViewModel::class.java)
        localViewModel._menuStatus.value = AssemblySessionMenuType.ScanningMode.ordinal
        var adapter = AssemblySessionAdapter(requireActivity(), lifecycleScope, localViewModel, listOf())
        var mainAdapter = AssemblySessionMainAdapter(requireActivity(), lifecycleScope, localViewModel, listOf())
        var recyclerView: RecyclerView = binding.rwListItem
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.adapter = adapter

        var recyclerViewMain: RecyclerView = binding.rwListMain
        recyclerViewMain.layoutManager = LinearLayoutManager(requireActivity())
        recyclerViewMain.adapter = mainAdapter
        var sessionId = (viewModel.uiState.value as UiState.AssemblySessionMenu).sessionId


        viewModel.IsFinishedAssemblySession.observe(viewLifecycleOwner, { status ->
            if(status){
                if(localViewModel._items.value!!.size == 0) {
                    val editText = EditText(requireContext())
                    editText.hint = "Введите текст"

                    val dialog = AlertDialog.Builder(requireActivity())
                        .setTitle("Окончание сборки")
                        .setMessage("Введите out отгрузки.")
                        .setView(editText)
                        .setPositiveButton("Да") { _, _ ->
                            val text = editText.text.toString()
                            localViewModel.finishSession(db.getDao(), text, sessionId)
                        }
                        .setNegativeButton("Нет", null)
                        .create()

                    dialog.show()
                }else{
                    val editText = EditText(requireContext())
                    editText.hint = "Введите текст"

                    val dialog = AlertDialog.Builder(requireActivity())
                        .setTitle("Окончание сборки")
                        .setMessage("Введите out отгрузки.")
                        .setView(editText)
                        .setPositiveButton("Да") { _, _ ->
                            val text = editText.text.toString()
                            localViewModel.finishSession(db.getDao(), text, sessionId)
                        }
                        .setNegativeButton("Нет", null)
                        .create()
                    val dialogNotification = AlertDialog.Builder(requireActivity())
                        .setTitle("Предупреждение")
                        .setMessage("Есть еще не собранный товар. Уверены что хотите завершить сборку")
                        .setPositiveButton("Да") { _, _ ->
                            dialog.show()
                        }
                        .setNegativeButton("Нет", null)
                        .create()

                    dialogNotification.show()



                    //dialog.show()
                }
            }
        })
        localViewModel.items.observe(viewLifecycleOwner, { items ->
            lifecycleScope.launch {
                withContext(Dispatchers.Main){
                    adapter.updateData(items)
                }
            }
        })
        localViewModel.assemblyStatus.observe(viewLifecycleOwner, { status ->
            lifecycleScope.launch {
                withContext(Dispatchers.Main){
                    updateAssemblyStyle(status)
                }
            }
        })
        localViewModel.menuStatus.observe(viewLifecycleOwner, { status ->
            lifecycleScope.launch {
                withContext(Dispatchers.Main){
                    if(status == AssemblySessionMenuType.CountMode.ordinal){
                        binding.etCount.isEnabled = true
                        binding.etCount.requestFocus()
                        binding.etCount.post {
                            binding.etCount.setTextColor(Color.WHITE)
                            binding.etCount.selectAll()
                        }
                        viewModel.switchScanningMode()
                    }else if (status == AssemblySessionMenuType.ScanningMode.ordinal){
                        binding.etCount.setTextColor(ContextCompat.getColor(requireActivity(), R.color.regularGrey))
                        binding.etCount.isEnabled = false
                        viewModel.switchScanningMode()
                    }else{
                        binding.etCount.setText("")
                        binding.rwListMain.visibility = View.GONE
                        binding.etCount.visibility = View.GONE
                        val editText = EditText(requireContext())
                        editText.hint = "Введите текст"

                        val dialog = AlertDialog.Builder(requireActivity())
                            .setTitle("Окончание сборки")
                            .setMessage("Введите out отгрузки.")
                            .setView(editText)
                            .setPositiveButton("Да") { _, _ ->
                                val text = editText.text.toString()
                                localViewModel.finishSession(db.getDao(), text, sessionId)
                            }
                            .setNegativeButton("Нет", null)
                            .create()

                        dialog.show()
                    }
                }
            }
        })
        localViewModel.activeElement.observe(viewLifecycleOwner, { element->
            lifecycleScope.launch {
                withContext(Dispatchers.Main){
                    mainAdapter.updateData(element.pickerList)
                    updateActiveElement(element)
                    binding.etCount.setText(element.amount.toString())
                }
            }
        })
        localViewModel.loadCollection(db,sessionId)
        binding.etCount.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_UNSPECIFIED && event.action == ACTION_DOWN) {
                val text = binding.etCount.text.toString()
                if (text != "") {
                    if (localViewModel.activeElement.value!!.amount == text.toInt()) {
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                var curItem = db.getDao().getPickerItemById(localViewModel.activeElement.value!!.assemblyItemId)
                                db.getDao().updatePickerItem(curItem.copy(finishedAt = System.currentTimeMillis(), status = StatusType.Finished.ordinal))

                                // Если равно
                                    //
                            }
                            // Добавить запись в бд
                            // создать запись goods и movement
                            //если равно

                            //если больше. Создать элемент в more, переместить его в ячейку с товаром, и потом уже выполнить операцию соединения
                            //если меньше.


                            // Иначе надо сменить элемент
                            withContext(Dispatchers.Main) {
                                localViewModel._items.value = localViewModel._items.value!!.drop(1)
                                if(localViewModel._items.value!!.count() != 0){
                                    var coll: AssemblyItem = localViewModel._items.value!!.first()
                                    localViewModel._activeElement.value = coll
                                    localViewModel._menuStatus.value = AssemblySessionMenuType.ScanningMode.ordinal
                                }else{
                                    localViewModel._menuStatus.value = AssemblySessionMenuType.OutMode.ordinal

                                }

                            }
                        }


                    }else{
                        // Если количество меньше или больше заявленного то спросить уверен ли что норм все и при нажатии нет надо вернуть фокус
                        val dialog = AlertDialog.Builder(requireActivity())
                            .setTitle("Что-то не так")
                            .setMessage("Количество товара не соответствует заявленом. Продолжить?")
                            .setPositiveButton("Да") { _, _ ->
                                localViewModel._items.value = localViewModel._items.value!!.drop(1)
                                // Обновить текущий элемент
                                var coll : AssemblyItem = localViewModel._items.value!!.first()
                                localViewModel._activeElement.value = coll
                                // Сменить фокус
                                localViewModel._menuStatus.value = AssemblySessionMenuType.ScanningMode.ordinal
                            }
                            .setNegativeButton("Нет") {_, _ ->
                                binding.etCount.requestFocus()
                                binding.etCount.post {
                                    binding.etCount.setTextColor(Color.WHITE)
                                    binding.etCount.selectAll()
                                }
                            }
                            .create()
                        dialog.show()
                    }

                    binding.etCount.setTextColor(ContextCompat.getColor(requireActivity(), R.color.regularGrey))
                }
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false

        }
        viewModel.Barcode.observe(viewLifecycleOwner, { barcode ->
            if(barcode != "" && viewModel.uiState.value is UiState.AssemblySessionMenu) {
                lifecycleScope.launch {
                    var isCountMode : Boolean = false
                    var activeElementList = localViewModel.activeElement.value!!.pickerList
                    withContext(Dispatchers.IO) {
                        var assemblySession = viewModel.uiState.value as UiState.AssemblySessionMenu
                        var db = MainDB.getDB(requireActivity())
                        var dao = db.getDao()
                        var curOperation = localViewModel.menuStatus.value

                        if(curOperation == AssemblySessionMenuType.ScanningMode.ordinal){
                            // Найти активный элемент в списке
                            for (counter in 0 .. activeElementList.count() - 1){
                                // Получение текущего элемента
                                var currElement = activeElementList[counter]
                                if(currElement.isSelected && currElement.data.any{item -> item == barcode}){
                                    //обновить время начала сборки элемента
                                    if(counter == 0){
                                        var pickerItem = dao.getPickerItemById(localViewModel.activeElement.value!!.assemblyItemId)
                                        dao.updatePickerItem(pickerItem.copy(startedAt = System.currentTimeMillis(), status = StatusType.Work.ordinal))
                                    }
                                    // Если это последний элемент то надо переключить режим на ввод количества
                                    // Обновить адаптер текущего элемента
                                    if(counter + 1  == activeElementList.size){
                                        isCountMode = true
                                        activeElementList.map { item -> item.isSelected = false }

                                    }
                                    // Иначе сделать следующий элемент в списке выбранным
                                    // Обновить адаптер текущего элемента
                                    else{
                                        activeElementList[counter].isSelected = false
                                        activeElementList[counter + 1].isSelected = true
                                        break
                                    }

                                }
                            }
                        }
                    }
                    withContext(Dispatchers.Main){
                        if(isCountMode){
                            localViewModel._menuStatus.value = AssemblySessionMenuType.CountMode.ordinal
                        }else{
                            viewModel.switchScanningMode()
                            viewModel.switchScanningMode()
                        }
                        var t = localViewModel._activeElement.value
                        t!!.pickerList = activeElementList
                        localViewModel._activeElement.value = t

                    }
                }
            }
        })

        return  binding.root
    }

   /* private suspend fun prepareItem(
        db: MainDB,
        activeElement: AssemblyItem?
    ) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO){
                var goods = db.getDao().getGoodsById(activeElement!!.goodsId)
                var sessionItem = db.getDao().getPickerItemById(activeElement.assemblyItemId)
            }
        }

    }*/

    private fun updateAssemblyStyle(status: Int?) {
       /* with(binding){
            if(StatusType.EnterCell.ordinal == status){
                tvCell.setTextColor(resources.getColor(R.color.white))
                tvGoodsName.setTextColor(resources.getColor(R.color.regularGrey))
                tvBarcode.setTextColor(resources.getColor(R.color.regularGrey))
                etCount.setTextColor(resources.getColor(R.color.regularGrey))
                etInput.isEnabled = true
                etInput.requestFocus()
                etCount.isEnabled = false
            }else if(StatusType.EnterBarcode.ordinal == status){
                tvGoodsName.setTextColor(resources.getColor(R.color.white))
                tvBarcode.setTextColor(resources.getColor(R.color.white ))
                tvCell.setTextColor(resources.getColor(R.color.regularGrey))
            }else{
                tvGoodsName.setTextColor(resources.getColor(R.color.regularGrey))
                tvBarcode.setTextColor(resources.getColor(R.color.regularGrey))
                etCount.setTextColor(resources.getColor(R.color.white))
                etCount.isEnabled = true
                etInput.isEnabled = false
                etCount.requestFocus()
            }
        }*/
    }
    private fun updateMenuStyle(status: Int?) {
        if(status == 0){
            binding.llMenuContainer.visibility = View.VISIBLE
            binding.llAssemblyContainer.visibility = View.GONE
        }
        else if(status == 1){
            binding.llMenuContainer.visibility = View.GONE
            binding.llAssemblyContainer.visibility = View.VISIBLE
        }
    }
    private fun updateActiveElement(newItem: AssemblyItem) {
       /* with(binding){
            tvCell.text = newItem!!.cell
            //tvBarcode.text = "456546546"
            tvGoodsName.text = newItem.name
            etCount.setText(newItem.amount.toString())
        }*/
    }
}