package com.example.wmswherther.Fragments

import android.content.Context
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.example.wmsRemote.R
import com.example.wmsRemote.databinding.FragmentSearchBinding
import com.example.wmswherther.viewModel.MainViewModel

class SearchFragment : Fragment(R.layout.fragment_search) {
    private var _binding: FragmentSearchBinding? = null
    private val binding
        get() = _binding ?: throw IllegalStateException("Binding for FragmentMain")
    private val viewModel: MainViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var mainContext : Context = requireActivity()
        viewModel.setCurrFragment(this)

        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        viewModel.
        //var remakeStr = highlightSubstring(str, text, color)




        /*with(binding){
            btnIncome.setOnClickListener {
                parentFragmentManager.commit {
                    setCustomAnimations(
                        R.anim.slide_in_right, // enter
                        R.anim.slide_out_left,  // exit
                        R.anim.slide_in_left,   // popEnter
                        R.anim.slide_out_right  // popExit
                    )
                    replace<IncomeFragment>(R.id.fragmentContainer)
                    addToBackStack(null)
                }

                viewModel.closeMenu()
                viewModel.startIncomeMenu()
            }
        }*/
        return binding.root
    }
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