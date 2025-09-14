package com.example.wmswherther.Adapters

import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.R

class AssemblyViewHolder (itemView: View): RecyclerView.ViewHolder(itemView) {
    val container: androidx.constraintlayout.widget.ConstraintLayout = itemView as androidx.constraintlayout.widget.ConstraintLayout
    val tvName: TextView = itemView.findViewById(R.id.tvName)
    val tvCell: TextView = itemView.findViewById(R.id.tvCell)

}
