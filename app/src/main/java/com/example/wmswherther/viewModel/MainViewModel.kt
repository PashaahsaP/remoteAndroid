package com.example.wmswherther.viewModel

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.wmsRemote.databinding.ActivityMainBinding
import com.example.wmswherther.Classes.UiState

class MainViewModel : ViewModel() {
    private val _uiState = MutableLiveData<UiState>(UiState.MainMenu())
    val uiState: LiveData<UiState> = _uiState

    private val _isActiveSession = MutableLiveData<Boolean>(true)
    private val _mainActivityBinding = MutableLiveData<ActivityMainBinding>()
    private val _isScanningActive = MutableLiveData<Boolean>(false)
    private val _isIncomeSessionTEModeActive = MutableLiveData<Boolean>(false)
    private val _TE = MutableLiveData<String>("")
    private val _barcode = MutableLiveData<String>("")
    private val _IsCloseTE = MutableLiveData<Boolean>(true)
    private val _widhtScanningField = MutableLiveData<Int>(0)
    private val _isActiveSearchWindow = MutableLiveData<Boolean>(false)
    private val _searchLineData = MutableLiveData<String>(null)
    private val _currentSupplierId = MutableLiveData<String>(null)
    private val _isSelectedMoveList = MutableLiveData<Boolean>(false)
    private val _isSelectedIncomeList = MutableLiveData<Boolean>(false)
    private val _isSelectedInventoryList = MutableLiveData<Boolean>(false)
   // private val _moveSupplierId = MutableLiveData<String>(null)


    val WidthScanningField: LiveData<Int> get() = _widhtScanningField
    val IsActiveSession: LiveData<Boolean> get() = _isActiveSession
    val IsScanningActive: LiveData<Boolean> get() = _isScanningActive
    val IsIncomeSessionTEModeActive: LiveData<Boolean> get() = _isIncomeSessionTEModeActive
    val TE: LiveData<String> get() = _TE
    val Barcode: LiveData<String> get() = _barcode
    val IsCloseTE: LiveData<Boolean> get() = _IsCloseTE
    val IsActiveSearchWindow: LiveData<Boolean> get() = _isActiveSearchWindow
    val SearchLineData: LiveData<String> get() = _searchLineData
    val CurrentSupplierId: LiveData<String> get() = _currentSupplierId
    val IsSelectedMoveList: LiveData<Boolean> get() = _isSelectedMoveList
    val IsSelectedIncomeList: LiveData<Boolean> get() = _isSelectedIncomeList
    val IsSelectedInventoryList: LiveData<Boolean> get() = _isSelectedInventoryList
  //  val MoveSupplierId: LiveData<String> get() = _moveSupplierId


    fun switchActivityOfInventorySession(){
        _isActiveSession.value = !IsActiveSession.value!!
    }
    fun getIsSelectedMoveList(): Boolean{
      if (IsSelectedMoveList.value != null && IsSelectedMoveList.value == true)
        return true
      else
          return false
  }
    fun selectMoveList(){
        _isSelectedMoveList.value = true
    }
    fun deselectMoveList(){
        _isSelectedMoveList.value = false
    }
    fun getIsSelectedInventoryList(): Boolean{
        if (IsSelectedInventoryList.value != null && IsSelectedInventoryList.value == true)
            return true
        else
            return false
    }
    fun selectInventoryList(){
        _isSelectedInventoryList.value = true
    }
    fun deselectInventoryList(){
        _isSelectedInventoryList.value = false
    }
    fun getIsSelectedIncomeList(): Boolean{
        if (IsSelectedIncomeList.value != null && IsSelectedIncomeList.value == true)
            return true
        else
            return false
    }
    fun selectIncomeList(){
        _isSelectedIncomeList.value = true
    }
    fun deselectIncomeList(){
        _isSelectedIncomeList.value = false
    }
    fun setCurrentSupplierId(str: String){
        _currentSupplierId.value = str
    }
    fun setSearchData(str: String){
        _searchLineData.value = str
    }
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
    fun setTE(text: String){
        _TE.value = text
    }
    fun setBarcode(text: String){
        _barcode.value = text
    }
    fun setActiveUi(state: UiState){
        _uiState.value = state
    }

}