package com.example.wmsRemote.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.R
import com.example.wmsRemote.viewModel.MoveSessionViewModel
import com.example.wmswherther.Adapters.MoveSessionSelectedViewHolder
import com.example.wmswherther.Classes.MoveSessionItem

class MoveSessionAdapter(
    var data: List<MoveSessionItem>,
    var activity: FragmentActivity,
    var viewModel: MoveSessionViewModel,
    var recyclerView: RecyclerView,

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var barcodeBuffer = StringBuilder()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> {
                val view = inflater.inflate(R.layout.income_session, parent, false)
                MoveSessionViewHolder(view)
            }

            1 -> {
                val view = inflater.inflate(R.layout.income_session_selected, parent, false)
                MoveSessionSelectedViewHolder(view)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (data[position].isSelected == true) {
            return 1
        }
        return 0
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun getItems(): List<MoveSessionItem> {
        return data
    }

    fun updateData(items: List<MoveSessionItem>, selectedItem: Int){
        data = items
        notifyDataSetChanged()
        focusOnItem(recyclerView, selectedItem)
    }
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        val item = data[position]
        when (holder) {
            is MoveSessionViewHolder -> {
                var counter = 0
                var listIncome: List<MoveSessionItem> = listOf()
                holder.container.setOnClickListener {

                    viewModel.myData.value?.forEach { item ->
                        item.isSelected = false
                        if (counter == position) {
                            item.isSelected = true
                        }
                        listIncome += item
                        counter = counter + 1
                    }
                    viewModel.setSelectedItem(position)
                    viewModel.updateMyData(listIncome.toMutableList())

                }
                holder.tvLeft.setTextColor(ContextCompat.getColor(activity, R.color.black))
                holder.tvRight.setTextColor(ContextCompat.getColor(activity, R.color.black))
                /*holder.tvTE.setTextColor(ContextCompat.getColor(activity, R.color.regularBack))*/
                if (item.haveCount > item.allCount) {
                    holder.tvLeft.setTextColor(ContextCompat.getColor(activity, R.color.regularRed))
                    holder.tvRight.setTextColor(
                        ContextCompat.getColor(
                            activity,
                            R.color.regularRed
                        )
                    )
                }
                /*if (item.teCount != 0){
                    holder.tvTE.setTextColor(ContextCompat.getColor(activity, R.color.regularRed))
                    holder.tvTE.text
                }*/
                holder.bind(item)
            }

            is MoveSessionSelectedViewHolder -> {
                holder.etSelectedCount.setOnKeyListener { _, keyCode, event ->
                    if (event.action == KeyEvent.ACTION_DOWN) {
                        val char = event.unicodeChar.toChar()
                        if (char == '\n') { // Enter — значит скан закончен
                            val scannedBarcode = barcodeBuffer.toString().trim()
                            barcodeBuffer.clear()
                            var t = holder.etSelectedCount.text.trim().toString()
                            var counter = 0
                            var listIncome: List<MoveSessionItem> = listOf()
                            var count = if(t != "" && t.isDigitsOnly()) t.toInt() else 0
                            viewModel.myData.value?.forEach { item ->
                                item.isSelected = false
                                if (counter == position) {
                                    if (count > item.allCount){
                                        item.haveCount = item.allCount
                                    }else{
                                        item.haveCount = count
                                    }
                                    /*val diff =   item.haveCount - count
                                    viewModel.setCounter(viewModel.getCounter() + diff)*/


                                    /*if((viewModel.uiState.value as UiState.IncomeSessionMenu).isTEModeActive){
                                        item.teCount = count
                                    }*/
                                }
                                listIncome += item
                                counter = counter + 1
                            }
                            viewModel.updateMyData(listIncome.toMutableList())
                            return@setOnKeyListener true
                        }
                    }
                    false
                }
                if (item.haveCount > item.allCount) {
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

        }



        fun updateData(newData: MutableList<MoveSessionItem>) {
            data = newData
            notifyDataSetChanged()
        }


    }
    fun focusOnItem(recyclerView: RecyclerView, position: Int) {
        recyclerView.post {
            val vh = recyclerView.findViewHolderForAdapterPosition(position)
            if (vh is MoveSessionSelectedViewHolder) {
                vh.etSelectedCount.requestFocus()
                vh.etSelectedCount.post {
                    vh.etSelectedCount.selectAll()
                    val imm = vh.itemView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(vh.etSelectedCount, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }
    }
}