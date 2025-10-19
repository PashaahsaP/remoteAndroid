package com.example.wmswherther.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.R
import com.example.wmswherther.Classes.IncomeItem
import com.example.wmswherther.Classes.TaskMenuItem

class IncomeSessionAdapter(var data: List<IncomeItem>):  RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemViewType(position: Int): Int {
        return if (data[position].isSelected) 1 else 0
    }
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = data[position]
        when (holder) {
            is IncomeSessionViewHolder -> holder.bind(item)
            is IncomeSessionSelectedViewHolder -> holder.bind(item)
        }
    }

    override fun getItemCount(): Int {
        return data.count()
    }


    fun updateCollection(items: List<IncomeItem>){
        data = items
        notifyDataSetChanged()
    }

}