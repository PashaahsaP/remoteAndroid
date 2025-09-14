package com.example.wmsRemote.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assembly_session")
data class AssemblySession(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo(name = "status")
    var status: Int,
    @ColumnInfo(name = "supplier")
    var supplier: Int,
    @ColumnInfo(name = "amount")
    var amount: Int,
    @ColumnInfo(name = "lines")
    var lines: Int,
    @ColumnInfo(name = "createdAt")
    var created_at: String,
    @ColumnInfo(name = "finishedAt")
    var finished_at: String,
    @ColumnInfo(name = "out")
    var out: String?
)
