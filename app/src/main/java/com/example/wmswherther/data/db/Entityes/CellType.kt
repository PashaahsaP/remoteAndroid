package com.example.wmswherther.data.db.Entityes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cell_types")
data class CellType(
    @PrimaryKey(autoGenerate = false) val id: String,
    val type: String,
    val mask: String?,
    val other: String? // JSON
)