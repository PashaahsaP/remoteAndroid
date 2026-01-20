package com.example.wmswherther.Adapters

import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.R

class MoveViewHolder (itemView: View): RecyclerView.ViewHolder(itemView) {
    val container: androidx.constraintlayout.widget.ConstraintLayout = itemView as androidx.constraintlayout.widget.ConstraintLayout
    val btn: Button = itemView.findViewById(R.id.btnSupplier)
}