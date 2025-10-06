package com.example.wmswherther.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _isMenuActive = MutableLiveData<Boolean>(true)
    val IsMenuActive: LiveData<Boolean> get() = _isMenuActive

    fun changeMenuStatus (status: Boolean){
        _isMenuActive.value = status
    }
}