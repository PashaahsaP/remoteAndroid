package com.example.wmsRemote.models

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import com.example.wmsRemote.Adapters.AdapterHelper
import com.example.wmsRemote.data.db.Cell
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.viewModel.MoveItem
import kotlinx.coroutines.CoroutineScope
import java.time.LocalDateTime


suspend fun processMoving(
    collection: MutableList<MoveItem>?,
    cell: LiveData<String>,
    db: MainDB,
    viewModelScope: CoroutineScope,
    text: String,
    supplier: Int,
    context: Context
): MutableList<MoveItem> {
    val stayItem =  collection!!.filter { inventoryItem -> inventoryItem.item.third.first == 0 }
    val movingItem = collection.filter { inventoryItem -> inventoryItem.item.third.first != 0 }
    var func = AdapterHelper.MoveItems[supplier]
    var resultMoving = func!!.invoke(movingItem, db, text, context)
    return (stayItem + resultMoving).toMutableList()
}



@RequiresApi(Build.VERSION_CODES.O)
suspend fun updateDB(movingCollection: List<MoveItem>, newCell: LiveData<String>, db: MainDB, viewModelScope: CoroutineScope, moveToCell: String): MutableList<MoveItem> {
        val result: MutableList<MoveItem> = mutableListOf()
        var cellId = db.getDao().getCellByName(moveToCell).id
        if (cellId == null) {
            cellId = db.getDao().insertCell(Cell(null, moveToCell)).toInt()
        }

        var goodsInMove = db.getDao().getAllGoodsAtomy().filter { goods -> // goods in cell destination
            goods.cellId == cellId
        }

        movingCollection.forEach { movingItem ->

            val changingItem = db.getDao().getGoodsAtomy(movingItem.item.first)

            var suppliementGoods = goodsInMove.firstOrNull{goods -> goods.catalogId == changingItem.catalogId} // найти товары, которые соответсвуют перемещаемым чтобы сплюсовать количество
            if (movingItem.item.third.first == movingItem.item.third.second) { // если равны то не нужно создавать новый товар
                if(goodsInMove.size != 0 &&  suppliementGoods!= null){
                    suppliementGoods.amount = suppliementGoods.amount + changingItem.amount

                    db.getDao().updateGoodsAtomy(suppliementGoods)
                }else{
                    db.getDao().updateGoodsAtomy(changingItem.copy(cellId = cellId!!))
                }

            } else {// иначе создается новый товар
                val moveGoods = GoodsAtomy(
                    null,
                    changingItem.catalogId,
                    cellId,
                    movingItem.item.third.first,
                    "235235",
                    "634634",
                    LocalDateTime.now().toString()
                )
                val stayGoods = GoodsAtomy(
                    changingItem.Id,
                    changingItem.catalogId,
                    changingItem.cellId,
                    movingItem.item.third.second - movingItem.item.third.first,
                    "235235",
                    "235523523",
                    LocalDateTime.now().toString()
                )
                if(goodsInMove.size != 0 &&  suppliementGoods!= null){
                    suppliementGoods.amount = suppliementGoods.amount + movingItem.item.third.first
                    db.getDao().updateGoodsAtomy(suppliementGoods)
                    db.getDao().updateGoodsAtomy(stayGoods)
                }else{
                    db.getDao().insertGoodsAtomy(moveGoods)
                    db.getDao().updateGoodsAtomy(changingItem.copy(amount = movingItem.item.third.second - movingItem.item.third.first))
                }

                result += MoveItem(Triple(stayGoods.Id!!, movingItem.item.second, Pair(0, movingItem.item.third.second - movingItem.item.third.first)),false)
            }
        }
        return result
}

