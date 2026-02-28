package com.example.wmswherther.Adapters

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.R
import com.example.wmswherther.Classes.TaskMenuItem
import com.example.wmswherther.Classes.UiState
import com.example.wmswherther.Fragments.IncomeFragment
import com.example.wmswherther.Fragments.IncomeSessionFragment
import com.example.wmswherther.Fragments.InventorySessionFragment
import com.example.wmswherther.viewModel.MainViewModel

class InventoryOrderMenuAdapter(var data: List<TaskMenuItem>, var fragment: Fragment, var viewModel: MainViewModel): RecyclerView.Adapter<IncomeMenuViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): IncomeMenuViewHolder {
        val inventoryView = LayoutInflater.from(parent.context)
            .inflate(R.layout.tasks_menu,parent,false)

        return IncomeMenuViewHolder(inventoryView)
    }

    override fun onBindViewHolder(
        holder: IncomeMenuViewHolder,
        position: Int
    ) {
        var item = data[position]
        holder.container.setOnClickListener {
            var newFragment = InventorySessionFragment()
            val bundle = Bundle().apply {
                putString("id", data[position].number.toString())//number contain sessionId
            }
            newFragment.arguments = bundle

            fragment.parentFragmentManager.commit {
                setCustomAnimations(
                    R.anim.slide_in_right, // enter
                    R.anim.slide_out_left,  // exit
                    R.anim.slide_in_left,   // popEnter
                    R.anim.slide_out_right  // popExit
                )
                replace(R.id.fragmentContainer, newFragment)
                addToBackStack(null)

            }
            viewModel.setActiveUi(UiState.InventorySessionMenu(prevState = viewModel.uiState.value, sessionId = item.sessionId, supplierId = item.supplierId))
        }

        holder.tvDate.text = item.date
        holder.tvNumber.text = ""
        holder.tvProgress.text = item.progress
        holder.tvSupplier.text = item.supplier
    }

    override fun getItemCount(): Int {
        return data.count()
    }

    fun updateMenuItems(newData: List<TaskMenuItem>){
        data = newData
        notifyDataSetChanged()

    }

}