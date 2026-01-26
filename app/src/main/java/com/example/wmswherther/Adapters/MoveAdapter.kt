package com.example.wmswherther.Adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.Adapters.MoveViewHolder
import com.example.wmsRemote.R
import com.example.wmswherther.Classes.MoveItem
import com.example.wmswherther.Classes.UiState
import com.example.wmswherther.Fragments.MoveSessionFragment
import com.example.wmswherther.viewModel.MainViewModel



class MoveAdapter(var data: MutableList<MoveItem>, var fragment: Fragment, var viewModel: MainViewModel
) : RecyclerView.Adapter<MoveViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoveViewHolder {
        val itemView = LayoutInflater.from(parent.context).
        inflate(R.layout.supplier_menu_item,parent,false)
        return MoveViewHolder(itemView)
    }



    override fun getItemCount(): Int {
        return data.size
    }
    fun getItems(): List<MoveItem> {
        return data
    }

    override fun onBindViewHolder(holder: MoveViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = data[position]
        holder.btn.text = item.name

        holder.btn.setOnClickListener {
            var newFragment = MoveSessionFragment()

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
            viewModel.setActiveUi(UiState.MoveSessionMenu(prevState = viewModel.uiState.value, supplierId = item.id))
        }
    }



    fun updateData(newData: MutableList<MoveItem>){
        data = newData
        notifyDataSetChanged()
    }

}