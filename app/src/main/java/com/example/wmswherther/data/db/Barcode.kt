package com.example.wmswherther.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.wmsRemote.data.db.GoodsBork

@Entity(tableName = "barcodes",
    foreignKeys = [
        ForeignKey(entity = Catalog::class, parentColumns = ["Id"], childColumns = ["catalogId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Supplier::class, parentColumns = ["Id"], childColumns = ["supplierId"], onDelete = ForeignKey.CASCADE),

    ], indices = [Index("name")])

data class Barcode(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val catalogId: Long,
    val supplierId: Long?,
    val other: String? // JSON
)