package com.example.wmswherther.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.wmsRemote.databinding.ActivityMainBinding

class MainViewModel : ViewModel() {
    private val _mainActivityBinding = MutableLiveData<ActivityMainBinding>()
    private val _isScanningActive = MutableLiveData<Boolean>(false)
    private val _isMenuActive = MutableLiveData<Boolean>(true)
    private val _isIncomeMenuActive = MutableLiveData<Boolean>(false)
    private val _isIncomeSessionActive = MutableLiveData<Boolean>(false)
    private val _isIncomeSessionTEModeActive = MutableLiveData<Boolean>(false)
    private val _isWorkableTE= MutableLiveData<Boolean>(false)//нужен чтобы отображать диалоговое окно
    private val _TE= MutableLiveData<String>("")
    private val _barcode= MutableLiveData<String>("")
    private val _IsCloseTE= MutableLiveData<Boolean>(true)
    private val _widhtScanningField= MutableLiveData<Int>(0)

    val MainActivityBinding: LiveData<ActivityMainBinding> get() = _mainActivityBinding
    val WidthScanningField: LiveData<Int> get() = _widhtScanningField
    val IsScanningActive: LiveData<Boolean> get() = _isScanningActive
    val IsMenuActive: LiveData<Boolean> get() = _isMenuActive
    val IsIncomeMenuActive: LiveData<Boolean> get() = _isIncomeMenuActive
    val IsIncomeSessionActive: LiveData<Boolean> get() = _isIncomeSessionActive
    val IsIncomeSessionTEModeActive: LiveData<Boolean> get() = _isIncomeSessionTEModeActive
    val TE: LiveData<String> get() = _TE
    val IsWorkableTE: LiveData<Boolean> get() = _isWorkableTE
    val Barcode: LiveData<String> get() = _barcode
    val IsCloseTE: LiveData<Boolean> get() = _IsCloseTE


    fun getMainBinding() : ActivityMainBinding? {
        return _mainActivityBinding.value
    }
    fun setMainBinding(binding: ActivityMainBinding) {
        _mainActivityBinding.value = binding
    }
    fun setWidthScanningField(width: Int){
        _widhtScanningField.value = width
    }
    fun switchTeButton(){
        if(_IsCloseTE.value == true) {
            _IsCloseTE.value = false
        }else{
            _IsCloseTE.value = true
        }
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
    fun switchTeMode()
    {
        if(_isIncomeSessionTEModeActive.value == true){
            turnOffTeMode()
        }else{
            turnOnTeMode()
        }
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