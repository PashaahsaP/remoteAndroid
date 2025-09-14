package com.example.wmsRemote.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catalog_atomy")
data class CatalogAtomy (
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "firstBarcode")
    var firstBarcode: String,
    @ColumnInfo(name = "secondBarcode")
    var secondBarcode: String?
)