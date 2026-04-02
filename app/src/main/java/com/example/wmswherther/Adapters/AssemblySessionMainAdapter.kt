package com.example.wmsRemote.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.R
import com.example.wmsRemote.viewModel.AssemblySessionViewModel
import com.example.wmswherther.Adapters.AssemblyMainViewHolder
import com.example.wmswherther.Adapters.AssemblyViewHolder
import com.example.wmswherther.Classes.AssemblyItem
import com.example.wmswherther.Classes.PickerItem
import com.example.wmswherther.Fragments.PickerSessionFragment
import kotlinx.coroutines.CoroutineScope

class AssemblySessionMainAdapter(
    private val context: Context,
    private val coroutine: CoroutineScope,
    private val viewModel: AssemblySessionViewModel,
    var data: List<PickerItem>) : RecyclerView.Adapter<AssemblyMainViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssemblyMainViewHolder {
        val inventoryView = LayoutInflater.from(parent.context)
            .inflate(R.layout.assembly_main_item,parent,false)
        return  AssemblyMainViewHolder(inventoryView)
    }
    override fun getItemCount(): Int {
        return  data.size
    }
    fun getUpdatedCollection():  List<PickerItem>{
        return  data
    }
    override fun onBindViewHolder(holder: AssemblyMainViewHolder, position: Int) {
        var item = data[position]
        holder.tvName.text = item.name
        if(item.isSelected){
            holder.tvName.setTextColor(ContextCompat.getColor(context,R.color.white))
        }else{
            holder.tvName.setTextColor(ContextCompat.getColor(context,R.color.regularGrey))
        }

    }
    fun updateData(newData: List<PickerItem>){
        data = newData
        notifyDataSetChanged()
    }

}

