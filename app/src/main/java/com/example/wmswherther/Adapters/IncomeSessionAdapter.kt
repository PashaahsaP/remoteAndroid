package com.example.wmswherther.Adapters

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.R
import com.example.wmswherther.Classes.IncomeItem
import com.example.wmswherther.viewModel.IncomeSessionViewModel

class IncomeSessionAdapter(
    var data: List<IncomeItem>,
    var recyclerView: RecyclerView,
    var localViewModel: IncomeSessionViewModel
):  RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var barcodeBuffer = StringBuilder()
    private val barcodeHandler = Handler(Looper.getMainLooper())
    private val barcodeTimeout = 500L // мс, если сканер не успел — сбрасываем
    override fun getItemViewType(position: Int): Int {
        return if (data[position].isSelected) 1 else 0
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

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
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

                holder.bind(item)
            }
            is IncomeSessionSelectedViewHolder ->
            {
                holder.etSelectedCount.setOnKeyListener{ _, keyCode, event ->
                    if (event.action == KeyEvent.ACTION_DOWN) {
                        val char = event.unicodeChar.toChar()

                        if (char == '\n') { // Enter — значит скан закончен
                            val scannedBarcode = barcodeBuffer.toString().trim()
                            barcodeBuffer.clear()
                            var t = holder.etSelectedCount.text.trim()
                            var counter = 0
                            var listIncome : List<IncomeItem> = listOf()
                            localViewModel.items.value?.forEach{ item ->
                                item.isSelected = false
                                if(counter == position){
                                    item.haveCount = t.toString().toInt()
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
                holder.bind(item)
            }
        }

            }

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