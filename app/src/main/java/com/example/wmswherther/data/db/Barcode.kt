package com.example.wmswherther.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "barcodes",
    foreignKeys = [
        ForeignKey(entity = Catalog::class, parentColumns = ["Id"], childColumns = ["catalogId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Supplier::class, parentColumns = ["Id"], childColumns = ["supplierId"], onDelete = ForeignKey.CASCADE),

    ], indices = [Index("name")])

data class Barcode(
    @PrimaryKey(autoGenerate = false) val id: String,
    val name: String,
    val catalogId: String,
    val supplierId: String?,
    val other: String? // JSON
)