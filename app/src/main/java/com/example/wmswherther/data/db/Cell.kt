package com.example.wmsRemote.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.wmswherther.data.db.CellType

@Entity(
    tableName = "cells",
    foreignKeys = [
        ForeignKey(entity = CellType::class, parentColumns = ["id"], childColumns = ["typeCellId"])
    ],
    indices = [Index("typeCellId")]
)
data class Cell (
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo(name = "typeCellId")
    val typeCellId: Long,
    @ColumnInfo(name = "name")
    var name: String,
)