package com.example.wmswherther.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "catalogs",
    foreignKeys = [
        ForeignKey(entity = Supplier::class, parentColumns = ["id"], childColumns = ["supplierId"], onDelete = ForeignKey.CASCADE)
    ], indices = [Index("name"), Index("sku")])
data class Catalog(
    @PrimaryKey(autoGenerate = false) val id: String,
    val name: String,
    val sku: String?,
    val supplierId: String,
    val other: String? // JSON
)