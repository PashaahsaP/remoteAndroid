package com.example.wmswherther.data.db.Entityes

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "barcodes",
    foreignKeys = [
        ForeignKey(entity = Catalog::class, parentColumns = ["id"], childColumns = ["catalogId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Supplier::class, parentColumns = ["id"], childColumns = ["supplierId"], onDelete = ForeignKey.CASCADE),

    ], indices = [Index("name")])

data class Barcode(
    @PrimaryKey(autoGenerate = false) val id: String,
    val name: String,
    val catalogId: String,
    val supplierId: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
    val isDeleted: Boolean,
    val other: String? // JSON
)