package com.example.wmswherther.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _isScanningActive = MutableLiveData<Boolean>(false)
    private val _isMenuActive = MutableLiveData<Boolean>(true)
    private val _isIncomeMenuActive = MutableLiveData<Boolean>(false)
    private val _isIncomeSessionActive = MutableLiveData<Boolean>(false)
    private val _isIncomeSessionTEModeActive = MutableLiveData<Boolean>(false)
    private val _isWorkableTE= MutableLiveData<Boolean>(false)//нужен чтобы отображать диалоговое окно
    private val _TE= MutableLiveData<String>("")
    private val _barcode= MutableLiveData<String>("")
    private val _isNeedCheckBarcode= MutableLiveData<Boolean>(false)
    private val _widhtScanningField= MutableLiveData<Int>(0)


    val WidthScanningField: LiveData<Int> get() = _widhtScanningField
    val IsScanningActive: LiveData<Boolean> get() = _isScanningActive
    val IsMenuActive: LiveData<Boolean> get() = _isMenuActive
    val IsIncomeMenuActive: LiveData<Boolean> get() = _isIncomeMenuActive
    val IsIncomeSessionActive: LiveData<Boolean> get() = _isIncomeSessionActive
    val IsIncomeSessionTEModeActive: LiveData<Boolean> get() = _isIncomeSessionTEModeActive
    val IsWorkableTE: LiveData<Boolean> get() = _isWorkableTE
    val Barcode: LiveData<String> get() = _barcode
    val IsNeedCheckBarcode: LiveData<Boolean> get() = _isNeedCheckBarcode
    fun setWidthScanningField(width: Int){
        _widhtScanningField.value = width
    }
    fun switchScanMode(){
        if(_isScanningActive.value == true){
            turnOffScanMode()
        }else{
            turnOnScanMode()
        }
    }
    fun turnOnScanMode(){
        _isScanningActive.value = true
    }
    fun turnOffScanMode(){
        _isScanningActive.value = false
    }
    fun updateBarcode(){
        _isNeedCheckBarcode.value = true
    }
    fun removeBarcode(){
        _isNeedCheckBarcode.value = false
    }
    fun setTE(text: String){
        _TE.value = text
    }
    fun setBarcode(text: String){
        _barcode.value = text
    }
    fun getBarcode() : String? {
        return _barcode.value
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