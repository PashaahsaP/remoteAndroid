package com.example.wmsRemote.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.KeyEvent

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.R
import com.example.wmsRemote.viewModel.MoveSessionViewModel
import com.example.wmswherther.Adapters.IncomeSessionSelectedViewHolder
import com.example.wmswherther.Adapters.IncomeSessionViewHolder
import com.example.wmswherther.Adapters.MoveSessionSelectedViewHolder
import com.example.wmswherther.Adapters.focusOnItem
import com.example.wmswherther.Classes.MoveSessionItem
import com.example.wmswherther.viewModel.MainViewModel

class MoveSessionAdapter(
    var data: List<MoveSessionItem>,
    var activity: FragmentActivity,
    var localViewModel: MoveSessionViewModel,
    var viewModel: MainViewModel,
    var recyclerView: RecyclerView,

    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var barcodeBuffer = StringBuilder()
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
    fun updateCollection(items: List<MoveSessionItem>, selectedItem: Int){
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
                var listMove: List<MoveSessionItem> = listOf()
                holder.container.setOnClickListener {

                    localViewModel.myData.value?.forEach { item ->
                        item.isSelected = false
                        if (counter == position) {
                            item.isSelected = true
                        }
                        listMove += item
                        counter = counter + 1
                    }
                    localViewModel.setSelectedItem(position)
                    localViewModel.updateMyData(listMove.toMutableList())

                }

                holder.tvLeft.setTextColor(ContextCompat.getColor(activity, R.color.black))
                holder.tvRight.setTextColor(ContextCompat.getColor(activity, R.color.black))
                if (item.haveCount > item.allCount) {
                    holder.tvLeft.setTextColor(ContextCompat.getColor(activity, R.color.regularRed))
                    holder.tvRight.setTextColor(
                        ContextCompat.getColor(
                            activity,
                            R.color.regularRed
                        )
                    )
                }

                holder.bind(item)
            }

            is MoveSessionSelectedViewHolder -> {
                holder.etSelectedCount.setOnKeyListener { _, keyCode, event ->
                    if (event.action == KeyEvent.ACTION_DOWN) {
                        val char = event.unicodeChar.toChar()

                        if (char == '\n') { // Enter — значит скан закончен
                            val scannedBarcode = barcodeBuffer.toString().trim()
                            barcodeBuffer.clear()
                            var t = holder.etSelectedCount.text.trim()
                            var counter = 0
                            var listMove: List<MoveSessionItem> = listOf()
                            var count = t.toString().toInt()
                            localViewModel.myData.value?.forEach { item ->
                                item.isSelected = false
                                if (counter == position) {
                                    item.haveCount = count
                                }
                                listMove += item
                                counter = counter + 1
                            }
                            localViewModel.updateMyData(listMove.toMutableList())
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