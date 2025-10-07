package com.example.wmswherther.Adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.R
import com.example.wmswherther.Classes.TaskMenuItem

class IncomeMenuAdapter(var data: List<TaskMenuItem>): RecyclerView.Adapter<IncomeMenuViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): IncomeMenuViewHolder {
        val inventoryView = LayoutInflater.from(parent.context)
            .inflate(R.layout.tasks_menu,parent,false)

        return IncomeMenuViewHolder(inventoryView)
    }

    override fun onBindViewHolder(
        holder: IncomeMenuViewHolder,
        position: Int
    ) {
        var item = data[position]
        holder.tvDate.text = item.date
        holder.tvNumber.text = item.number
        holder.tvProgress.text = item.progress
        holder.tvSupplier.text = item.supplier
    }

    override fun getItemCount(): Int {
        return data.count()
    }
}