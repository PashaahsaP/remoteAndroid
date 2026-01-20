package com.example.wmsRemote.Adapters

import android.annotation.SuppressLint

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.R
import com.example.wmsRemote.MoveActivity
import com.example.wmsRemote.viewModel.MoveSessionItem
import com.example.wmsRemote.viewModel.MovingViewModel

class MoveSessionAdapter(
    var data: MutableList<MoveSessionItem>,
    var activity: MoveActivity,
    var viewModel: MovingViewModel,
) : RecyclerView.Adapter<MoveSessionViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoveSessionViewHolder {
        val itemView = LayoutInflater.from(parent.context).
                inflate(R.layout.list_items,parent,false)
        return MoveSessionViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return data.size
    }
    fun getItems(): List<MoveSessionItem> {

        return data
    }

    override fun onBindViewHolder(holder: MoveSessionViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = data[position]
        holder.container.tag = item.item.first
        holder.tvLeft.text = item.item.second
        holder.tvRight.text = "/${item.item.third.second}"
        holder.etRight.setText("${item.item.third.first}")
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Called before the text is changed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Called as the text is being changed
                //println("Text changed to: $s")
            }

            override fun afterTextChanged(s: Editable?) {
                //TODO don't understand why collection is changed when change data[position], two-side binnding in  MoveActivity?
          /*      // Called after the text has been changed
                if(holder.etRight.text.toString() != "") {
                    val etData = holder.etRight.text.toString().toIntOrNull()
                    if (etData != null) {
                        if (etData > item.item.third.second) {
                            holder.etRight.setText(item.item.third.second)
                                *//*viewModel.updateItem(
                                    MoveItem(
                                        Triple(item.item.first,
                                            item.item.second,
                                            Pair(holder.etRight.text.toString().toInt(), item.item.third.second)),
                                        true
                                    )
                                )*//*
                        }else if(etData < 0){
                            holder.etRight.setText("0")
                        }else{
                            data[position] = item.copy(
                                item = Triple(
                                    item.item.first,
                                    item.item.second,
                                    Pair(etData, item.item.third.second)
                                )
                            )
                        }
                    }
                }*/
            }
        }
        holder.etRight.addTextChangedListener(textWatcher)
        holder.textWatcher = textWatcher
       /* if (item.isSelected){
            holder.etRight.requestFocus()
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(holder.etRight, InputMethodManager.SHOW_IMPLICIT)
            holder.etRight.setSelection(0, holder.etRight.text.length)
        }*/
    }



    fun updateData(newData: MutableList<MoveSessionItem>){
        data = newData
        notifyDataSetChanged()
    }

}