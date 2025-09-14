package com.example.wmsRemote.data.repository

import com.example.wmsRemote.MoveActivity
import com.example.wmsRemote.data.db.MainDB
import com.example.wmsRemote.data.enums.DataBaseType

class SupplierRepository(val type: DataBaseType, val moveActivity: MoveActivity) {

    suspend fun getCatalogItemFromDb(id: Int, supplierId: Int){
        when(type){
            DataBaseType.DataBaseRoom -> MainDB.getDB(moveActivity).getDao()
            DataBaseType.SQLite -> ""
            DataBaseType.MySql -> ""
            DataBaseType.PostgreSQL -> ""
        }
    }
    suspend fun getCatalogItemsFromDb(supplierId: Int, catalogId: Int){

    }
    suspend fun getSupplier(name: String) : Int{
        return 1
    }
}