package com.example.wmswherther.Adapters

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.R
import com.example.wmswherther.Classes.IncomeItem

class IncomeSessionExpandedViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val container: androidx.constraintlayout.widget.ConstraintLayout =
        itemView as androidx.constraintlayout.widget.ConstraintLayout
    val tvLeft: TextView = itemView.findViewById(R.id.tvName)
    fun bind(item: IncomeItem) {
        tvLeft.text = item.name
    }
}