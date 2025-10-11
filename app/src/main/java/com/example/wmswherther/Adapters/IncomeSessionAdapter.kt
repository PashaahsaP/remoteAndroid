package com.example.wmswherther.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.R
import com.example.wmswherther.Classes.IncomeItem
import com.example.wmswherther.Classes.TaskMenuItem

class IncomeSessionAdapter(var data: List<IncomeItem>): RecyclerView.Adapter<IncomeSessionViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeSessionViewHolder {
            val inventoryView = LayoutInflater.from(parent.context)
                .inflate(R.layout.income_session,parent,false)

            return IncomeSessionViewHolder(inventoryView)
    }

    override fun getItemCount(): Int {
        return data.count()
    }

    override fun onBindViewHolder(holder: IncomeSessionViewHolder, position: Int) {
        holder.tvLeft.text = data[position].name
        holder.tvRight.text = "${data[position].haveCount}/${data[position].allCount}"
    }

    fun updateCollection(items: List<IncomeItem>){
        data = items
        notifyDataSetChanged()
    }

}