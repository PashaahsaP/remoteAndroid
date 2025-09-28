package com.example.wmswherther.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.wmsRemote.data.db.Cell

@Entity(
    tableName = "goods",
    foreignKeys = [
        ForeignKey(entity = Cell::class, parentColumns = ["id"], childColumns = ["cellId"]),
        ForeignKey(entity = Catalog::class, parentColumns = ["id"], childColumns = ["catalogId"])
    ],
    indices = [Index("cellId"), Index("catalogId")]
)
data class Goods(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Int,
    val cellId: Long,
    val catalogId: Long,
    val createdAt: Long,
    val other: String? // JSON
)