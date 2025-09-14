package com.example.wmsRemote.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "shipment",
    foreignKeys = [
        ForeignKey(entity = AssemblySession::class, parentColumns = ["id"], childColumns = ["assemblyId"], onDelete = ForeignKey.CASCADE),
    ])
data class Shipment(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo(name = "assemblyId")
    var assemblyId: Int,
    @ColumnInfo(name = "createdAt")
    var createdAt: String,
    @ColumnInfo(name = "finishedAt")
    var finishedAt: String,
    @ColumnInfo(name = "status")
    var status: Int,
    @ColumnInfo(name = "out")
    var out: String?,

)
