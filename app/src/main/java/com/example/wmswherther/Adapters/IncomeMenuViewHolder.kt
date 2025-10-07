package com.example.wmswherther.Adapters

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.R

class IncomeMenuViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val container: androidx.constraintlayout.widget.ConstraintLayout = itemView as androidx.constraintlayout.widget.ConstraintLayout
    val tvSupplier: TextView = itemView.findViewById(R.id.tvSupplier)
    val tvNumber: TextView = itemView.findViewById(R.id.tvNumber)
    val tvProgress: TextView = itemView.findViewById(R.id.tvProgress)
    val tvDate: TextView = itemView.findViewById(R.id.tvDate)

}