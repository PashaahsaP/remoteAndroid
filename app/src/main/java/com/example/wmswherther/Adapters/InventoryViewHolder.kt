package com.example.wmsRemote.Adapters

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.R

class InventoryViewHolder(invetoryView: View) : RecyclerView.ViewHolder(invetoryView){
    val container: LinearLayout = invetoryView as LinearLayout
    val tvLeft: TextView = itemView.findViewById(R.id.tvLeft)
    val tvRight: TextView = itemView.findViewById(R.id.tvRight)

}




