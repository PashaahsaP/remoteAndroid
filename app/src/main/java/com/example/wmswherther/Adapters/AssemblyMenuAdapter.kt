package com.example.wmswherther.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.R
import com.example.wmsRemote.data.db.Dao
import com.example.wmsRemote.viewModel.AssemblyViewModel
import com.example.wmswherther.Classes.TaskMenuItem
import com.example.wmswherther.Classes.UiState
import com.example.wmswherther.Fragments.PickerSessionFragment
import com.example.wmswherther.viewModel.MainViewModel

class AssemblyMenuAdapter(
    var data: List<TaskMenuItem>,
    var fragment: Fragment,
    var viewModel: MainViewModel,
    var localViewModel: AssemblyViewModel,
    var dao: Dao
): RecyclerView.Adapter<IncomeMenuViewHolder>() {
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
            var newFragment = PickerSessionFragment()


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
            viewModel.setActiveUi(UiState.AssemblySessionMenu(prevState = viewModel.uiState.value, sessionId = item.sessionId, supplierId = item.supplierId))
            localViewModel.startSession(dao, item.sessionId)
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