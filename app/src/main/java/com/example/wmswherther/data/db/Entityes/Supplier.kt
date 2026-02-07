package com.example.wmswherther.data.db.Entityes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = false) val id: String,
    val name: String,
    val other: String? // JSON
)
