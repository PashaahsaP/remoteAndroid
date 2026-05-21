package com.example.wmswherther.Adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.Adapters.MoveViewHolder
import com.example.wmsRemote.R
import com.example.wmswherther.Classes.InventoryItem
import com.example.wmswherther.Classes.MoveItem
import com.example.wmswherther.Classes.UiState
import com.example.wmswherther.Fragments.InventorySessionFragment
import com.example.wmswherther.Fragments.MoveSessionFragment
import com.example.wmswherther.viewModel.MainViewModel

class InventoryAdapter (
    var data: MutableList<InventoryItem>,
    var fragment: Fragment,
    var viewModel: MainViewModel
) : RecyclerView.Adapter<MoveViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoveViewHolder {
        val itemView = LayoutInflater.from(parent.context).
        inflate(R.layout.supplier_menu_item,parent,false)
        return MoveViewHolder(itemView)
    }
    override fun getItemCount(): Int {
        return data.size
    }
    fun getItems(): List<InventoryItem> {
        return data
    }
    override fun onBindViewHolder(holder: MoveViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val item = data[position]
        holder.btn.text = item.name
        holder.btn.setOnClickListener {
            // TODO когда сделаю контроль состояния пользователя надо обновить статус сессии
            //change status
            // if return null, refresh a page
            //if ok replace page on a sesstion page
            var newFragment = InventorySessionFragment()//

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
            viewModel.setActiveUi(UiState.InventorySessionMenu(
                prevState = viewModel.uiState.value,
                supplierId = item.id,
                isSupplierModeActive = true))
        }
    }

    fun updateData(newData: MutableList<InventoryItem>){
        data = newData
        notifyDataSetChanged()
    }

}