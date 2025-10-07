package com.example.wmswherther.Adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.R

class IncomeMenuAdapter(var data: List<String>): RecyclerView.Adapter<IncomeMenuViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): IncomeMenuViewHolder {
        val inventoryView = LayoutInflater.from(parent.context)
            .inflate(R.layout.supplier_menu_item,parent,false)

        return IncomeMenuViewHolder(inventoryView)
    }

    override fun onBindViewHolder(
        holder: IncomeMenuViewHolder,
        position: Int
    ) {
        var item = data[position]
        holder.btnSupplier.text = item
        Log.d("Adapter", "Binding item: $item at position $position")
    }

    override fun getItemCount(): Int {
        return data.count()
    }
}