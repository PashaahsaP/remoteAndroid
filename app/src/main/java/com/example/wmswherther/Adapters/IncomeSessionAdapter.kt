package com.example.wmswherther.Adapters

import android.content.Context
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.R
import com.example.wmsRemote.isBoxTE
import com.example.wmswherther.Classes.IncomeItem
import com.example.wmswherther.Classes.UiState
import com.example.wmswherther.viewModel.IncomeSessionViewModel
import com.example.wmswherther.viewModel.MainViewModel

class IncomeSessionAdapter(
    var data: List<IncomeItem>,
    var recyclerView: RecyclerView,
    var localViewModel: IncomeSessionViewModel,
    var activity: FragmentActivity,
    var viewModel: MainViewModel
):  RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var barcodeBuffer = StringBuilder()
    override fun getItemViewType(position: Int): Int {
        if(data[position].isSelected == true){
            return 1
        }else if (data[position].isExpandable && data[position].isExpanded){
            return 2
        }else if (data[position].isExpandable && data[position].isExpanded == false){
            return 3
        }else if (data[position].isShown == false){
            return 4
        }

        return 0
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> {
                val view = inflater.inflate(R.layout.income_session, parent, false)
                IncomeSessionViewHolder(view)
            }
            1 -> {
                val view = inflater.inflate(R.layout.income_session_selected, parent, false)
                IncomeSessionSelectedViewHolder(view)
            }
            2 -> {
                val view = inflater.inflate(R.layout.income_session_expanded, parent, false)
                IncomeSessionExpandedViewHolder(view)
            }
            3 -> {
                val view = inflater.inflate(R.layout.income_session_collapsed, parent, false)
                IncomeSessionCollapsedViewHolder(view)
            }
            4 -> {
                val view = inflater.inflate(R.layout.income_session_invisible, parent, false)
                IncomeSessionInvisibleViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun  onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data[position]
        when (holder) {
            is IncomeSessionViewHolder -> {
                var counter = 0
                var listIncome : List<IncomeItem> = listOf()
                holder.container.setOnClickListener {

                    localViewModel.items.value?.forEach{ item ->
                        item.isSelected = false
                        if(counter == position){
                            item.isSelected = true
                        }
                        listIncome+= item
                        counter = counter + 1
                    }
                    localViewModel.setSelectedItem(position)
                    localViewModel.updateItems(listIncome)

                }
                holder.container.setOnLongClickListener {

                    var dialog = android.app.AlertDialog.Builder(activity)
                        .create()
                    var text = TextView(activity)
                    text.width = 100
                    text.setPadding(30)
                    text.setText("Вы точно хотите удалить строку?")
                    dialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, "Да") { _, _ ->
                        var newData: MutableList<IncomeItem> = mutableListOf()
                        var allData: MutableList<IncomeItem> = mutableListOf()
                        if(localViewModel.stack.size == 0) {
                            localViewModel.items.value?.forEach { innerItem ->
                                if (innerItem.catalogId != item.catalogId || (innerItem.catalogId == item.catalogId && localViewModel.currentCellName.value != item.TE)) {
                                    newData += innerItem
                                }
                            }
                        }else{
                            localViewModel.items.value?.forEach { innerItem ->
                                if (innerItem.catalogId != item.catalogId || (innerItem.catalogId == item.catalogId && localViewModel.currentCellName.value != innerItem.TE)) {
                                    newData += innerItem
                                }
                            }

                            var value = localViewModel.stack.removeLast().toList()
                            value.forEach { innerItem->
                                if (innerItem.catalogId != item.catalogId || (innerItem.catalogId == item.catalogId && localViewModel.currentCellName.value != innerItem.TE)) {
                                    allData += innerItem
                                }
                            }
                            localViewModel.stack.addLast(allData)
                        }
                        localViewModel.updateItems(newData.toList())
                        //TODO удалить запись из коллекции. Надо искать элемент который в текущей те и по id
                    }
                    dialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, "Нет") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }


                    dialog.setView(text)
                    dialog.show()
                    true
                }
                holder.tvLeft.setTextColor(ContextCompat.getColor(activity, R.color.black))
                holder.tvRight.setTextColor(ContextCompat.getColor(activity, R.color.black))
                holder.tvTE.setTextColor(ContextCompat.getColor(activity, R.color.regularBack))
                if(item.haveCount > item.allCount) {
                    holder.tvLeft.setTextColor(ContextCompat.getColor(activity, R.color.regularRed))
                    holder.tvRight.setTextColor(ContextCompat.getColor(activity, R.color.regularRed))
                }
                if (item.teCount != 0){
                    holder.tvTE.setTextColor(ContextCompat.getColor(activity, R.color.regularRed))
                    holder.tvTE.text
                }
                holder.bind(item)
            }
            is IncomeSessionSelectedViewHolder -> {
                holder.etSelectedCount.setOnKeyListener{ _, keyCode, event ->
                    if (event.action == KeyEvent.ACTION_DOWN) {
                        val char = event.unicodeChar.toChar()

                        if (char == '\n') { // Enter — значит скан закончен
                            val scannedBarcode = barcodeBuffer.toString().trim()
                            barcodeBuffer.clear()
                            var t = holder.etSelectedCount.text.trim()
                            var counter = 0
                            var listIncome : List<IncomeItem> = listOf()
                            var count = t.toString().toInt()
                            localViewModel.items.value?.forEach{ item ->
                                item.isSelected = false
                                if(counter == position){
                                    item.haveCount = count

                                    if((viewModel.uiState.value as UiState.IncomeSessionMenu).isTEModeActive){
                                        item.teCount = count
                                    }
                                }
                                listIncome+= item
                                counter = counter + 1
                            }
                            localViewModel.updateItems(listIncome)
                            return@setOnKeyListener true
                        }
                    }
                    false
                }
                if(item.haveCount > item.allCount) {
                    holder.tvLeft.setTextColor(ContextCompat.getColor(activity, R.color.regularRed))
                    holder.tvRight.setTextColor(
                        ContextCompat.getColor(
                            activity,
                            R.color.regularRed
                        )
                    )
                    holder.etSelectedCount.setTextColor(
                        ContextCompat.getColor(
                            activity,
                            R.color.regularRed
                        )
                    )
                }
                holder.bind(item)
            }
            is IncomeSessionExpandedViewHolder ->{
                holder.tvLeft.text = item.name
                holder.container.setOnClickListener {
                    var value = localViewModel.stack.removeLast().toList()
                    localViewModel.cellStack.removeLast()
                    localViewModel.setCellName(localViewModel.cellStack.last())
                    for (elem in value){
                        localViewModel.items.value?.forEach { item ->
                            if(item.name == elem.TE){
                                elem.isExpanded = false
                            }
                            if(elem.catalogId == item.catalogId && item.TE == elem.TE){
                                elem.haveCount = item.haveCount
                            }
                        }
                    }
                    localViewModel.updateItems(value)
                }

            }
            is IncomeSessionCollapsedViewHolder ->{
                holder.tvLeft.text = item.name
                holder.container.setOnClickListener {
                    var value = localViewModel.items.value!!.toList()
                    localViewModel.stack.addLast(value)
                    var list: MutableList<IncomeItem> = mutableListOf()
                    localViewModel.setCellName(item.TE)
                    localViewModel.cellStack.addLast(localViewModel.currentCellName.value.toString())
                    localViewModel.items.value?.forEach { elem ->
                        if(elem.name == item.name){
                            list.add(elem.copy(isExpanded = true))
                        }else if(elem.TE == item.name){
                            list.add(elem.copy(isShown = true))
                        }
                    }
                    localViewModel.updateItems(list.toList())
                }
                holder.container.setOnLongClickListener {

                    var dialog = android.app.AlertDialog.Builder(activity)
                        .create()
                    var titleText = TextView(activity)
                    var contentText = TextView(activity)
                    var containerLocal = LinearLayout (activity)
                    containerLocal.addView(titleText)
                    containerLocal.addView(contentText)
                    containerLocal.orientation = LinearLayout.VERTICAL
                    titleText.setPadding(30)
                    titleText.setText("Удаление")
                    contentText.setPadding(30)
                    contentText.setText("Удалить вложенные элементы?")
                    dialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, "Да") { _, _ ->
                        var newCollection : MutableList<IncomeItem> = mutableListOf()
                        localViewModel.items.value?.forEach { localItem->
                            if (item.name != localItem.TE){
                                newCollection += localItem
                            }
                        }
                        localViewModel.updateItems(newCollection)
                    }
                    dialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, "Нет") { dialogInterface, _ ->
                        var newCollection : MutableList<IncomeItem> = mutableListOf()
                        //удаление те
                        localViewModel.items.value?.forEach { localItem->
                            if (item.name != localItem.name){
                                if(item.name == localItem.TE){
                                    newCollection += localItem.copy(TE = localViewModel.currentCellName.value.toString(), isShown = true)
                                }else{
                                    newCollection += localItem
                                }
                            }
                        }
                        // сложение дубликатов
                        var result = newCollection
                            .groupBy {it.catalogId}
                            .map { (id, group) ->
                               IncomeItem(
                                   name = group.first().name,
                                   TE = group.first().TE,
                                   catalogId = id,
                                   haveCount = group.sumOf { it.haveCount },
                                   allCount = group.sumOf { it.allCount },
                                   teCount = group.sumOf { it.teCount },
                                   isSelected = group.first().isSelected,
                                   isExpandable = group.first().isExpandable,
                                   isExpanded = group.first().isExpanded,
                                   isShown = group.first().isShown,
                               )
                            }
                        localViewModel.updateItems(result)
                        //TODO Удалить те, а для элементов назначить те, как и у остальных
                        dialogInterface.dismiss()
                    }
                    dialog.setButton(android.app.AlertDialog.BUTTON_NEUTRAL, "Отмена") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    dialog.setOnCancelListener {
                        viewModel.setActiveUi((viewModel.uiState.value as UiState.IncomeSessionMenu).copy(isTEModeActive = true))
                    }

                    dialog.setView(containerLocal)
                    dialog.show()
                    true
                }

            }
        }

    }
//TODO не получается корректно добавлять те внутри других те, но вроде работает сканирование добавление элементов
//TODO если отсканировал и вышел из те, то почему то пропадают элементы. При добавлении новой те внутри те неправильный порядок те
    override fun getItemCount(): Int {
        return data.count()
    }

    fun updateCollection(items: List<IncomeItem>, selectedItem: Int){
        data = items
        notifyDataSetChanged()
        focusOnItem(recyclerView, selectedItem)
    }

}
fun focusOnItem(recyclerView: RecyclerView, position: Int) {
    recyclerView.post {
        val vh = recyclerView.findViewHolderForAdapterPosition(position)
        if (vh is IncomeSessionSelectedViewHolder) {
            vh.etSelectedCount.requestFocus()
            vh.etSelectedCount.post {
                vh.etSelectedCount.selectAll()
                val imm = vh.itemView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(vh.etSelectedCount, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }
}
//Получить список при помощи определенного элемента(те)
//Если элемент списка это те то запустить новую фунцию и скрыть эту те
//Иначе обновить видимость элемента
/*
fun collapseItems(localViewModel: IncomeSessionViewModel, item: IncomeItem) {
    var list: MutableList<IncomeItem> = mutableListOf()
    localViewModel.items.value?.forEach { inner ->
        if(inner.isExpandable && item.name == inner.TE){
            val new = inner.copy(isExpanded = false)
            collapseItems(localViewModel, inner)
            list.add(new)
        }else if(item.name == inner.TE){
            list.add(inner.copy(isShown = false))
        }
    }
    localViewModel.updateItems(list)
}*/
