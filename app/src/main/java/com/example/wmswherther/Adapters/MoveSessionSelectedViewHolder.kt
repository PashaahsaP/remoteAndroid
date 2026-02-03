package com.example.wmswherther.Adapters

import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.R
import com.example.wmswherther.Classes.IncomeItem
import com.example.wmswherther.Classes.MoveSessionItem

class MoveSessionSelectedViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val container: ConstraintLayout = itemView as ConstraintLayout
    val tvLeft: TextView = itemView.findViewById(R.id.tvName)
    val etSelectedCount: EditText = itemView.findViewById(R.id.etSelectedCount)
    val tvRight: TextView = itemView.findViewById(R.id.tvCount)
    var isSelected: Boolean = false


    fun bind(item: MoveSessionItem){
        tvLeft.text = item.name
        isSelected = true
        etSelectedCount.setText(item.haveCount.toString())
        tvRight.text = "/${item.allCount.toString()}"
        etSelectedCount.requestFocus()
    }

}