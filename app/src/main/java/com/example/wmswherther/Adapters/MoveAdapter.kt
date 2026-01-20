package com.example.wmswherther.Adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.R

data class MoveItem(val name: String, val id: String)

class MoveAdapter(var data: MutableList<MoveItem>
) : RecyclerView.Adapter<MoveViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoveViewHolder {
        val itemView = LayoutInflater.from(parent.context).
        inflate(R.layout.supplier_menu_item,parent,false)
        return MoveViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return data.size
    }
    fun getItems(): List<MoveItem> {
        return data
    }

    override fun onBindViewHolder(holder: MoveViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = data[position]
        holder.btn.text = item.name

        holder.btn.setOnClickListener {

        }
    }



    fun updateData(newData: MutableList<MoveItem>){
        data = newData
        notifyDataSetChanged()
    }

}