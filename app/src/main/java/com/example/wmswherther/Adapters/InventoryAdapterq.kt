package com.example.wmsRemote.Adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.recyclerview.widget.RecyclerView
import com.example.wmsRemote.R
import com.example.wmsRemote.Classes.IInventoryItem
import com.example.wmsRemote.viewModel.InventoryViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class InventoryAdapterq(
    private val context: Context,
    private val coroutine: CoroutineScope,
    private val viewModel: InventoryViewModel,
    var data: List<IInventoryItem>
) : RecyclerView.Adapter<InventoryViewHolder>() {
    private var catalogs: MutableList<CatalogItem> = mutableListOf()

    init {
        //getCatalog()
        println()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val inventoryView = LayoutInflater.from(parent.context)
            .inflate(R.layout.inventory_bork_list,parent,false)
        return  InventoryViewHolder(inventoryView)
    }
    override fun getItemCount(): Int {
        return  data.size
    }
    fun getUpdatedCollection():  List<IInventoryItem>{
        return  data
    }
    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        coroutine.launch {
            var item = data[position]
            if(item.type == "default" || item.type == "new"){
                holder.container.setOnLongClickListener  {
                    val func = AdapterHelper.getDialogForDefaultGoods[viewModel.supplier]
                    func!!.invoke(context, item, position, viewModel).show()
                    true // Indicate that the long press event was handled
                }
            }

            if(item.type == "none"){
                holder.container.setOnLongClickListener  {
                   var collection = getUpdatedCollection()
                    var newData = collection.filter { element ->
                        element != item
                    }
                    updateData(newData)
                    viewModel.updateItems(newData)
                    true
                }
            }




            holder.container.setOnClickListener {

                if (item.type == "none") {

                    var dialog = AlertDialog.Builder(context)
                        .create()
                    dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Сохранить") { _, _ -> }
                    dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Отмена") { dialogInterface, _ -> dialogInterface.dismiss() }
                    val scroller = ScrollView(context)
                    val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_with_spinner, null)
                    scroller.addView(dialogView)
                    dialog.setView(scroller)
                    val dynamicContainer: LinearLayout = dialogView.findViewById(R.id.dynamic_container)
                    holder.container.descendantFocusability =  262144
                    dynamicContainer.descendantFocusability =  262144
                    dialog.show()
                    dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)// after that two line work keyboard when click update line
                    dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                    val func = AdapterHelper.getInventoryDialog[viewModel.supplier]
                    func!!.invoke(dynamicContainer,dialog,position,item,context,viewModel, catalogs)

                }
            }
            //<editor-fold desc="Set color by amount"
            if (item.amount.second < item.amount.first) {//TODO make ui for 0, it's without supplierId
                    val func = AdapterHelper.getRedUiForGoods[viewModel.supplier]
                    func!!.invoke(holder, context, item)
            } else {
                val  func = AdapterHelper.getBlackUiForGoods[viewModel.supplier]
                func!!.invoke(holder, context, item)
            }
            //</editor-fold>
        }


    }

    fun updateData(newData: List<IInventoryItem>){
        data = newData
        notifyDataSetChanged()
    }
    /*fun getCatalog () : Unit {
        var ip = "192.168.6.208"
        coroutine.launch {
            var borkCatalogInJson = HelperFunction.retryRequest(context) { client.getAllBorkCatalog(ip)}
            var borkCatalogs = getCatalogsBorkFromJsonArr(borkCatalogInJson)
            var innerCatalogs: MutableList<CatalogItem> = mutableListOf()
            withContext(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    catalogs = borkCatalogs.map { item-> CatalogItem(
                        id = item.id!!.toInt(),
                        name = item.name
                    ) }.toMutableList()
                }
            }
        }
    }*/
    fun convertToInt(nullableInt: Int?): Int {
        return nullableInt ?: 0  // If nullableInt is null, use 0 as default
    }
    /*fun getCatalogsBorkFromJsonArr(obj: JSONArray) : MutableList<CatalogBork> {
        var mutableList: MutableList<CatalogBork> = mutableListOf()
        for (i in 0 until obj.length()){
            var obj = obj.getJSONObject(i)
            mutableList.add(
                CatalogBork(
                id = obj["id"].toString().toInt(),
                name = obj["name"].toString()

            )
            )
        }
        return mutableList
    }
    fun getCatalogBorkFromJsonObject(obj: JSONObject) : CatalogBork {
        return CatalogBork(
            id = obj["id"].toString().toInt(),
            name = obj["name"].toString()
        )
    }*/

}

