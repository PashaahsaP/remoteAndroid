package com.example.wmswherther.data.db.Entityes

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_diff_items",
    foreignKeys = [
        ForeignKey(entity = SessionInventory::class, parentColumns = ["id"], childColumns = ["inventorySessionId"], onDelete = ForeignKey.CASCADE)
    ])
data class InventoryDiffItem(
    @PrimaryKey(autoGenerate = false) val id: String,
    val inventorySessionId: String,
    val catalogId: String,
    val barcode: String,
    val isTE: Boolean,
    val parentCellId: String,
    val diffCount: Int,
    val status: Int,
    val other: String? // JSON
)