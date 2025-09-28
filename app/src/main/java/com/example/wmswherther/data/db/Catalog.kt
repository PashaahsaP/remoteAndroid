package com.example.wmswherther.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "catalogs",
    foreignKeys = [
        ForeignKey(entity = Supplier::class, parentColumns = ["id"], childColumns = ["supplierId"], onDelete = ForeignKey.CASCADE)
    ])
data class Catalog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val supplierId: Long?,
    val other: String? // JSON
)