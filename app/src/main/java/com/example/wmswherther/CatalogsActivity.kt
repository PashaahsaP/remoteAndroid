package com.example.wmsRemote

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.inputmethod.EditorInfo
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.wmsRemote.R
import com.example.wmsRemote.data.db.CatalogBork
import com.example.wmsRemote.databinding.ActivityCatalogsBinding
import com.example.wmsRemote.data.db.MainDB

class CatalogsActivity : AppCompatActivity() {
    private var _binding: ActivityCatalogsBinding? = null
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding for ActivityMain")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = MainDB.getDB(this)
        val color = ContextCompat.getColor(this, R.color.regularBlue)
        enableEdgeToEdge()
        setContentView(R.layout.activity_catalogs)
        _binding = ActivityCatalogsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding){
            etCatalogs.setOnEditorActionListener { textView, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    // Выполнить действие
                    val text = etCatalogs.text.toString()
                    handleTextChange(text)
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false
            }


            btnSearch.setOnClickListener {
          /*      val text = binding.etCatalogs.text.toString()
                if (text != ""){
                    tvData.text = ""
                    lifecycleScope.launch{
                        db.getDao().getAllCatalogs().collect(){items->
                            items.forEach{ item->
                                if(item.name.contains(text)|| item.firstBarcode.contains(text)){
                                    var data = "${item.name}  ${item.firstBarcode}\n  "
                                    var remakeStr = highlightSubstring(data, text, color)
                                    tvData.append(remakeStr)
                                }
                        }
                        }
                    }

                }*/

            }
        }
    }
    private fun handleTextChange(text: String) {
        binding.btnSearch.performClick()
        binding.etCatalogs.text.clear()
        binding.etCatalogs.requestFocus()
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
}