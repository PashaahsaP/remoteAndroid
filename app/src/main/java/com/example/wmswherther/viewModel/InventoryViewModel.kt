package com.example.wmsRemote.viewModel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wmsRemote.Classes.IInventoryItem
import com.example.wmsRemote.InventoryActivity
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.data.enums.SupplierType
import com.example.wmsRemote.models.processInputBarcode
import com.example.wmsRemote.models.processInputCell
import com.example.wmsRemote.models.processSaveBtn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.media.ToneGenerator
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.wmsRemote.R
import kotlinx.coroutines.delay

data class InventoryItem(val catalogId: Int,
                         val  goodsId: Int,
                         val cellId: Int,
                         val supplierId: Int,
                         val TE : String,
                         val barcode: String,
                         val date: String,
                         val type: String,
                         var amount: Pair<Int, Int>)
class InventoryViewModel : ViewModel() {
    private val _items = MutableLiveData<List<IInventoryItem>>()
    private val _cell = MutableLiveData<String>()
    var supplier: Int = SupplierType.Bork.ordinal
    private val _isInventoryActive = MutableLiveData<Boolean>()
    val cell : LiveData<String> get() = _cell
    val isInventoryActive : LiveData<Boolean> get() = _isInventoryActive
    val items: LiveData<List<IInventoryItem>> get() = _items


    // Function to update data
    fun updateItems(newItems: List<IInventoryItem>) {
        _items.value = newItems

    }
    fun updateItemsAt(newItem: IInventoryItem, position: Int){
        var updateList : List<IInventoryItem> = listOf()
        for ((index, item) in items.value!!.withIndex()){
            if(position == index){
                updateList += newItem
            }else {
                updateList += item
            }
        }
        updateItems(updateList)
    }
    fun updateCell(newCell: String){
        _cell.postValue(newCell)
    }
    fun updateIsInventoryActive(newIsInventoryActive: Boolean){
        _isInventoryActive.value = newIsInventoryActive
    }

    fun handleSearchEvent(
        input: String,
        db: MainDB,
        inventoryActivity: InventoryActivity,
        soundPool: SoundPool,
        successSoundId: Int,
        errorSoundId: Int,
        swipe: SwipeRefreshLayout
    )  {
        var result : List<IInventoryItem> = listOf()
        if (isCell(input)) {
            soundPool.play(successSoundId, 1f, 1f, 0, 0, 1f)
            viewModelScope.launch {
                withContext(Dispatchers.Main){
                    swipe.isRefreshing = true
                }

                withContext(Dispatchers.IO) {
                   result = processInputCell(input, db, this@InventoryViewModel, inventoryActivity)
                }
                withContext(Dispatchers.Main){
                    swipe.isRefreshing = false
                    updateItems(result)
                    updateCell(input)
                   }
            }
            updateIsInventoryActive(true)
        } else {
            if (isBarcode(input, supplier)) {
                soundPool.play(successSoundId, 1f, 1f, 0, 0, 1f)
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        swipe.isRefreshing = true
                }
                    withContext(Dispatchers.IO) {
                        result = processInputBarcode(
                            input,
                            db,
                            items.value,
                            viewModelScope,
                            supplier,
                            cell.value.toString(),
                            inventoryActivity
                        )
                    }
                    withContext(Dispatchers.Main) {
                        swipe.isRefreshing = false
                        updateItems(result)
                    }
                }
            }else{
                soundPool.play(errorSoundId, 1f, 1f, 0, 0, 1f)
            }
        }

    }

    private fun isBarcode(input: String, supplier: Int): Boolean {
        if(supplier == 1) {
            input.forEach { letter ->
                if (!letter.isDigit()) {
                    return false
                }
            }
            if (input.length !in 12..14) {
                return false
            }
        }
        return true
    }
    fun playErrorTone() {
        val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100)
        toneGen.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 200) // 200 мс
    }
    fun playSuccessSound(context: Context) {
        val mediaPlayer = MediaPlayer.create(context, R.raw.positive)
        mediaPlayer.setOnCompletionListener {
            it.release()
        }
        mediaPlayer.start()
    }
    private fun isCell(cell: String): Boolean {
        if (cell.length == 4 && cell[0] in 'A' .. 'Z' && cell[1].isDigit() && cell[2].isDigit() && cell[3].isDigit()){
            return true
        }
        return false
    }
    fun convertToInt(nullableInt: Int?): Int {
        return nullableInt ?: 0  // If nullableInt is null, use 0 as default
    }

    fun handelSaveEvent(db: MainDB, inventoryActivity: InventoryActivity, swipe: SwipeRefreshLayout) {
        var result : List<IInventoryItem>? = listOf()
        viewModelScope.launch {
                withContext(Dispatchers.Main) {
                    swipe.isRefreshing = true
                }
                withContext(Dispatchers.IO) {
                    result = processSaveBtn(db, items.value, cell.value, inventoryActivity, inventoryActivity)
                }
                withContext(Dispatchers.Main) {
                    swipe.isRefreshing = false
                    if(result != null) {
                        var nonNull :List<IInventoryItem> = result as List<IInventoryItem>
                        updateItems(nonNull)
                    }else{
                        Toast.makeText(inventoryActivity, "Что-то не так", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

}
fun isNoneExist(list: List<IInventoryItem>): Boolean{
    list.forEach{ item->
        if (item.type == "none"){

            return true
        }
    }
    return false
}
suspend fun <T> retryRequest(
    retries: Int = 15,
    delayMs: Long = 2000,
    block: suspend () -> T
): T {
    repeat(retries - 1) {

        try {
            var result = block()
            return result
        } catch (e: Exception) {
            // Можно логировать ошибку
            delay(delayMs) // подождать перед новой попыткой
        }
    }
    return block() // последняя попытка
}