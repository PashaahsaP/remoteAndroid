package com.example.wmsRemote.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "goods_atomy",
    indices = [Index(value = ["TE"], name = "index_goods_atomy_TE", unique = false)],
    foreignKeys = [
        ForeignKey(entity = CatalogAtomy::class, parentColumns = ["id"], childColumns = ["catalogId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Cell::class, parentColumns = ["id"], childColumns = ["cellId"], onDelete = ForeignKey.CASCADE),
    ])
data class GoodsAtomy (
    @PrimaryKey(autoGenerate = true)
    var Id: Int? = null,
    @ColumnInfo(name = "catalogId")
    var catalogId: Int,
    @ColumnInfo(name = "cellId")
    var cellId: Int,
    @ColumnInfo(name = "amount")
    var amount: Int,
    @ColumnInfo(name = "TE")
    var TE: String,
    @ColumnInfo(name = "date")
    var date: String,
    @ColumnInfo(name = "createdAt")
    var createdAt: String
)