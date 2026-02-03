package com.example.wmsRemote.Adapters

import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.R
import com.example.wmswherther.Classes.IncomeItem
import com.example.wmswherther.Classes.MoveSessionItem

class MoveSessionViewHolder (itemView: View): RecyclerView.ViewHolder(itemView) {
    val container: androidx.constraintlayout.widget.ConstraintLayout = itemView as androidx.constraintlayout.widget.ConstraintLayout
    val tvLeft: TextView = itemView.findViewById(R.id.tvLeft)
    val tvRight: TextView = itemView.findViewById(R.id.tvRight)
    val etRight: EditText = itemView.findViewById(R.id.etRight)
    var isSelected: Boolean = false

    fun bind(item: MoveSessionItem){
        tvLeft.text = item.name
        tvRight.text = "/${item.allCount}"
        etRight.setText(item.haveCount)
    }
}