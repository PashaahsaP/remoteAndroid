package com.example.wmswherther.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cell_types")
data class CellType(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val mask: String?,
    val other: String? // JSON
)