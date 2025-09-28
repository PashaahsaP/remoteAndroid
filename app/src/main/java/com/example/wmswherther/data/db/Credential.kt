package com.example.wmswherther.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credentials")
data class Credential(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val other: String? // JSON
)