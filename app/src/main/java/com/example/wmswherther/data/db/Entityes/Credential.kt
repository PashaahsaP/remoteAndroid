package com.example.wmswherther.data.db.Entityes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credentials")
data class Credential(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val type: String,
    val other: String? // JSON
)