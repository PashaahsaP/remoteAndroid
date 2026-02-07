package com.example.wmswherther.data.db.Entityes

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cells",
    foreignKeys = [
        ForeignKey(entity = CellType::class, parentColumns = ["id"], childColumns = ["typeCellId"])
    ],
    indices = [Index("typeCellId"), Index("name")]
)
data class Cell (
    @PrimaryKey(autoGenerate = false)  var id: String,
    @ColumnInfo(name = "typeCellId")
    val typeCellId: String,
    @ColumnInfo(name = "parentCellId")
    val parentCellId: String?,
    @ColumnInfo(name = "name")
    var name: String,
)