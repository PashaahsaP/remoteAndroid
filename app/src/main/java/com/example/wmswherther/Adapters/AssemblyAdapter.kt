package com.example.wmsRemote.Adapters

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.Classes.AssemblyItem
import com.example.wmsRemote.R
import com.example.wmsRemote.Classes.AtomyInventoryItem
import com.example.wmsRemote.Classes.IInventoryItem
import com.example.wmsRemote.data.db.CatalogBork
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.data.enums.SupplierType
import com.example.wmsRemote.models.client
import com.example.wmsRemote.viewModel.AssemblyViewModel
import com.example.wmsRemote.viewModel.InventoryItem
import com.example.wmsRemote.viewModel.InventoryViewModel
import com.example.wmswherther.Adapters.AssemblyViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class AssemblyAdapter(
    private val context: Context,
    private val coroutine: CoroutineScope,
    private val viewModel: AssemblyViewModel,
    var data: List<AssemblyItem>) : RecyclerView.Adapter<AssemblyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssemblyViewHolder {
        val inventoryView = LayoutInflater.from(parent.context)
            .inflate(R.layout.assembly_item,parent,false)
        return  AssemblyViewHolder(inventoryView)
    }
    override fun getItemCount(): Int {
        return  data.size
    }
    fun getUpdatedCollection():  List<AssemblyItem>{
        return  data
    }
    override fun onBindViewHolder(holder: AssemblyViewHolder, position: Int) {
        var item = data[position]
        holder.tvName.text = item.name
        holder.tvCell.text = item.cell

    }
    fun updateData(newData: List<AssemblyItem>){
        data = newData
        notifyDataSetChanged()
    }



}

