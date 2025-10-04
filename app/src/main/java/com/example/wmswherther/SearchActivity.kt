package com.example.wmsRemote
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.wmsRemote.R
import com.example.wmsRemote.databinding.ActivitySearchByCellBinding
import com.example.wmsRemote.data.db.Cell
import com.example.wmsRemote.data.db.MainDB
import com.example.wmswherther.HelperFunction
import com.example.wmswherther.data.db.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class SearchActivity : AppCompatActivity() {
    private var _binding: ActivitySearchByCellBinding? = null
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding for ActivityMain")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        var color = ContextCompat.getColor(this, R.color.regularBlue)
        val db = MainDB.getDB(this)
        _binding = ActivitySearchByCellBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var searchItems : List<Triple<String, String, String>> = listOf()
        val ip = "192.168.6.208"
        lifecycleScope.launch {
            withContext(Dispatchers.Main){
                binding.swipe.isRefreshing = true
            }
            withContext(Dispatchers.IO){
             /*   var req : Request =  Request()
                var data = HelperFunction.retryRequest(this@SearchActivity){req.getAllAtomyGoods(ip)}
                searchItems = data.map{ item ->
                    var obj = JSONObject(item);
                    var catalogId = obj.getString("catalogId")
                    var cellId = obj.getString("cellId")
                    var catalogJson = HelperFunction.retryRequest(this@SearchActivity){req.getAtomyCatalogById(ip,catalogId)}//todo может не выполнится т.к. не будет записи в бд с таким id
                    var cellJson = HelperFunction.retryRequest(this@SearchActivity){req.getCellById(ip, cellId)}
                    Triple(obj["TE"].toString(), catalogJson["firstBarcode"].toString(), cellJson["name"].toString())
                }*/
            }
            withContext(Dispatchers.Main){
                binding.swipe.isRefreshing = false
            }
        }

        with(binding){
            etCell.setOnEditorActionListener { textView, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    // Выполнить действие
                    val text = etCell.text.toString()
                    if(text != "") {
                        handleTextChange(text)
                    }
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false
            }
            btnSearch.setOnClickListener {
                val text = binding.etCell.text.toString()
                if (text != ""){
                    var dao = db.getDao()
                    tvData.text = ""
                    lifecycleScope.launch{
                        withContext(Dispatchers.IO){
                            searchItems.forEach { items ->
                                var str = "${items.first} ${items.second} ${items.third}\n"
                                if (str.contains(text)) {
                                    var remakeStr = highlightSubstring(str, text, color)
                                    withContext(Dispatchers.Main) {
                                        tvData.append(remakeStr)
                                    }
                                }

                            }
                        }
                    }

                }

            }


        }
        requestFocusAndHideKeyboard()
    }

    fun highlightSubstring(text: String, substring: String, highlightColor: Int): Spanned {
        val spannableString = SpannableString(text)
        var startIndex = text.indexOf(substring)

        while (startIndex != -1) {
            val endIndex = startIndex + substring.length
            spannableString.setSpan(ForegroundColorSpan(highlightColor), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            startIndex = text.indexOf(substring, endIndex)
        }
        return spannableString
    }
    private fun handleTextChange(text: String) {
        binding.btnSearch.performClick()
        binding.etCell.text.clear()
        binding.etCell.requestFocus()
    }
    private fun requestFocusAndHideKeyboard() {
        currentFocus?.clearFocus()
        binding.etCell.requestFocus() // Request focus

        Handler(Looper.getMainLooper()).postDelayed({
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (imm.isAcceptingText) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0) // show the keyboard
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY,0) // Hide the keyboard
            }
        }, 100) // Delay for 100ms
    }

}
