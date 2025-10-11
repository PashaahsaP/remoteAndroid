package com.example.wmswherther.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _isMenuActive = MutableLiveData<Boolean>(true)
    private val _isIncomeSessionActive = MutableLiveData<Boolean>(false)
    val IsMenuActive: LiveData<Boolean> get() = _isMenuActive
    val IsIncomeSessionActive: LiveData<Boolean> get() = _isIncomeSessionActive

    fun showMenu()
    {
        _isMenuActive.value = true
    }
    fun closeMenu()
    {
        _isMenuActive.value = false
    }
    fun startIncomeSession()
    {
        _isIncomeSessionActive.value = true
    }
    fun finishIncomeSession()
    {
        _isIncomeSessionActive.value = false
    }

}