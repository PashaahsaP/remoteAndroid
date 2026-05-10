package com.example.wmswherther.data.db.Entityes

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "picker_items",
    foreignKeys = [
        ForeignKey(entity = SessionPicker::class, parentColumns = ["id"], childColumns = ["sessionId"], ForeignKey.CASCADE),
        ForeignKey(entity = Goods::class, parentColumns = ["id"], childColumns = ["goodsId"], ForeignKey.CASCADE),
        ForeignKey(entity = Cell::class, parentColumns = ["id"], childColumns = ["cellId"], ForeignKey.CASCADE),


    ])
data class PickerItem(
    @PrimaryKey(autoGenerate = false) val id: String,
    val sessionId: String,
    val goodsId: String,
    val cellId: String?,
    val status: Int,
    val startedAt: Long?,
    val finishedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
    val isDeleted: Boolean,
    val other: String? // JSON
)