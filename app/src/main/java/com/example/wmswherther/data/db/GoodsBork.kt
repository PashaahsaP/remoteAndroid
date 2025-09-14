package com.example.wmsRemote.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "goods_bork",
    foreignKeys = [
        ForeignKey(entity = CatalogBork::class, parentColumns = ["id"], childColumns = ["catalogId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Cell::class, parentColumns = ["id"], childColumns = ["cellId"], onDelete = ForeignKey.CASCADE),
    ])
data class GoodsBork (
    @PrimaryKey(autoGenerate = true)
    var Id: Int? = null,
    @ColumnInfo(name = "catalogId")
    var catalogId: Int,
    @ColumnInfo(name = "cellId")
    var cellId: Int,
    @ColumnInfo(name = "amount")
    var amount: Int,
    @ColumnInfo(name = "createdAt")
    var createdAt: String
)