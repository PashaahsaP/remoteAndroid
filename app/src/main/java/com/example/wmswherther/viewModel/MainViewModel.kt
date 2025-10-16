package com.example.wmswherther.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _isMenuActive = MutableLiveData<Boolean>(true)
    private val _isIncomeMenuActive = MutableLiveData<Boolean>(false)
    private val _isIncomeSessionActive = MutableLiveData<Boolean>(false)
    private val _isIncomeSessionTEModeActive = MutableLiveData<Boolean>(false)
    private val _isWorkableTE= MutableLiveData<Boolean>(false)//нужен чтобы отображать диалоговое окно
    private val _TE= MutableLiveData<String>("")

    val IsMenuActive: LiveData<Boolean> get() = _isMenuActive
    val IsIncomeMenuActive: LiveData<Boolean> get() = _isIncomeMenuActive
    val IsIncomeSessionActive: LiveData<Boolean> get() = _isIncomeSessionActive
    val IsIncomeSessionTEModeActive: LiveData<Boolean> get() = _isIncomeSessionTEModeActive
    val IsWorkableTE: LiveData<Boolean> get() = _isWorkableTE
    val TE: LiveData<String> get() = _TE

    fun setTE(text: String){
        _TE.value = text
    }
    fun showMenu()
    {
        _isMenuActive.value = true
    }
    fun closeMenu()
    {
        _isMenuActive.value = false
    }
    fun startIncomeMenu()
    {
        _isIncomeMenuActive.value = true
    }
    fun finishIncomeMenu()
    {
        _isIncomeMenuActive.value = false
    }
    fun startIncomeSession()
    {
        _isIncomeSessionActive.value = true
    }
    fun finishIncomeSession()
    {
        _isIncomeSessionActive.value = false
    }
    fun turnOnTeMode()
    {
        _isIncomeSessionTEModeActive.value = true
    }
    fun turnOffTeMode()
    {
        _isIncomeSessionTEModeActive.value = false
    }
    fun workTe()
    {
        _isWorkableTE.value = true
    }
    fun unworkTe()
    {
        _isWorkableTE.value = false
    }

}