package com.example.wmswherther.data.db.Entityes

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "movements",
    foreignKeys = [
        ForeignKey(entity = Cell::class, parentColumns = ["id"], childColumns = ["cellFromId"]),
        ForeignKey(entity = Cell::class, parentColumns = ["id"], childColumns = ["cellToId"]),
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"]),
        ForeignKey(entity = Catalog::class, parentColumns = ["id"], childColumns = ["catalogId"]),
    ],
    indices = [Index("cellFromId"), Index("cellToId"), Index("userId")]
)
data class Movement(
    @PrimaryKey(autoGenerate = false) val id: String,
    val cellFromId: String,
    val cellToId: String,
    val catalogId: String,
    val goodsId: String,
    val qty: String,
    val userId: Long,
    val executedAt: Long,
    val operationType: Int
)