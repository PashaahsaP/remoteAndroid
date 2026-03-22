package com.example.wmsRemote.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.Classes.AssemblyItem
import com.example.wmsRemote.R
import com.example.wmsRemote.viewModel.AssemblyViewModel
import com.example.wmswherther.Adapters.AssemblyViewHolder
import kotlinx.coroutines.CoroutineScope

class AssemblySessionAdapter(
    private val context: Context,
    private val coroutine: CoroutineScope,
    private val viewModel: AssemblyViewModel,
    var data: List<AssemblyItem>) : RecyclerView.Adapter<AssemblyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssemblyViewHolder {
        val inventoryView = LayoutInflater.from(parent.context)
            .inflate(R.layout.assembly_item,parent,false)
        return  AssemblyViewHolder(inventoryView)
    }
    override fun getItemCount(): Int {
        return  data.size
    }
    fun getUpdatedCollection():  List<AssemblyItem>{
        return  data
    }
    override fun onBindViewHolder(holder: AssemblyViewHolder, position: Int) {
        var item = data[position]
        holder.tvName.text = item.name
        holder.tvCell.text = item.cell
    }
    fun updateData(newData: List<AssemblyItem>){
        data = newData
        notifyDataSetChanged()
    }

}

