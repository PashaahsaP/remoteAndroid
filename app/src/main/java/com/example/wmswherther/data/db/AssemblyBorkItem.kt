package com.example.wmsRemote.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "assembly_bork_item",
    foreignKeys = [
        ForeignKey(entity = GoodsBork::class, parentColumns = ["Id"], childColumns = ["goodsId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = AssemblySession::class, parentColumns = ["id"], childColumns = ["assemblyId"], onDelete = ForeignKey.CASCADE),
    ])
data class AssemblyBorkItem(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo(name = "goodsId")
    var goodsId: Int,
    @ColumnInfo(name = "assemblyId")
    var assemblyId: Int,
    @ColumnInfo(name = "createdAt")
    var createdAt: String,
    @ColumnInfo(name = "finishedAt")
    var finishedAt: String,
    @ColumnInfo(name = "status")
    var status: Int
)
