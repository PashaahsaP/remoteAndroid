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
    val goodsId: String,
    val plannedCount: Int,
    val actualCount: Int,
    val status: Int,
    val other: String? // JSON
)