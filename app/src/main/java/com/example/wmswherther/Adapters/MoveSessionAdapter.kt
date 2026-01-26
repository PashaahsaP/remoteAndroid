package com.example.wmsRemote.Adapters

import android.annotation.SuppressLint
import android.view.KeyEvent

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.R
import com.example.wmsRemote.viewModel.MoveSessionViewModel
import com.example.wmswherther.Adapters.IncomeSessionCollapsedViewHolder
import com.example.wmswherther.Adapters.IncomeSessionExpandedViewHolder
import com.example.wmswherther.Adapters.IncomeSessionInvisibleViewHolder
import com.example.wmswherther.Adapters.IncomeSessionSelectedViewHolder
import com.example.wmswherther.Adapters.IncomeSessionViewHolder
import com.example.wmswherther.Classes.IncomeItem
import com.example.wmswherther.Classes.MoveSessionItem
import com.example.wmswherther.Classes.UiState

class MoveSessionAdapter(
    var data: List<MoveSessionItem>,
    var activity: FragmentActivity,
    var viewModel: MoveSessionViewModel,

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : RecyclerView.ViewHolder {
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
    override fun getItemViewType(position: Int): Int {
        if(data[position].isSelected == true){
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


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = data[position]
        when (holder) {
            is IncomeSessionViewHolder -> {
                var counter = 0
                var listIncome : List<IncomeItem> = listOf()
                holder.container.setOnClickListener {

                    viewModel.myData.value?.forEach{ item ->
                        item.isSelected = false
                        if(counter == position){
                            item.isSelected = true
                        }
                        listIncome+= item
                        counter = counter + 1
                    }
                    viewModel.setSelectedItem(position)
                    viewModel.updateItems(listIncome)

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

    }



    fun updateData(newData: MutableList<MoveSessionItem>){
        data = newData
        notifyDataSetChanged()
    }

}