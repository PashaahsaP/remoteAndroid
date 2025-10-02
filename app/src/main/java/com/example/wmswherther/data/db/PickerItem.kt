package com.example.wmswherther.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.wmsRemote.data.db.Cell

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
    val other: String? // JSON
)