package com.example.wmswherther.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class IncomeViewModel : ViewModel() {
    private val _isSupplierMenu = MutableLiveData<Boolean>()
    private val _isIncomeItemMenu = MutableLiveData<Boolean>()
    private val _isActiveSession = MutableLiveData<Boolean>()
    val IsSupplierMenu: LiveData<Boolean> get() = _isSupplierMenu
    val IsIncomeItemMenu: LiveData<Boolean> get() = _isIncomeItemMenu
    val IsActiveSession: LiveData<Boolean> get() = _isActiveSession
}