package com.example.wmswherther.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.wmsRemote.data.db.Cell

@Entity(
    tableName = "movements",
    foreignKeys = [
        ForeignKey(entity = Cell::class, parentColumns = ["id"], childColumns = ["cellFromId"]),
        ForeignKey(entity = Cell::class, parentColumns = ["id"], childColumns = ["cellToId"]),
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"]),
        ForeignKey(entity = OperationType::class, parentColumns = ["id"], childColumns = ["operationTypeId"])
    ],
    indices = [Index("cellFromId"), Index("cellToId"), Index("userId"), Index("operationTypeId")]
)
data class Movement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cellFromId: Long?,
    val cellToId: Long?,
    val userId: Long?,
    val executedAt: Long,
    val operationTypeId: Long
)