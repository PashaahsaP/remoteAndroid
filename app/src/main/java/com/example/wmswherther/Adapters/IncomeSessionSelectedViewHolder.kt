package com.example.wmswherther.Adapters

import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.R
import com.example.wmswherther.Classes.IncomeItem

class IncomeSessionSelectedViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val container: androidx.constraintlayout.widget.ConstraintLayout = itemView as androidx.constraintlayout.widget.ConstraintLayout
    val tvLeft: TextView = itemView.findViewById(R.id.tvName)
    val etSelectedCount: EditText = itemView.findViewById(R.id.etSelectedCount)
    val tvRight: TextView = itemView.findViewById(R.id.tvCount)

    fun bind(item: IncomeItem){
        tvLeft.text = item.getName()
        etSelectedCount.setText(item.haveCount.toString())
        tvRight.text = "/${item.allCount.toString()}"
        etSelectedCount.requestFocus()
    }

}