package com.example.wmswherther.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.wmsRemote.data.db.Cell

@Entity(tableName = "outcome_items",
    foreignKeys = [

        ForeignKey(entity = SessionOutcome::class, parentColumns = ["id"], childColumns = ["sessionId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Goods::class, parentColumns = ["id"], childColumns = ["goodsId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Cell::class, parentColumns = ["id"], childColumns = ["cellId"], onDelete = ForeignKey.CASCADE)

    ])
data class OutcomeItem(
    @PrimaryKey(autoGenerate = false) val id: String,
    val sessionId: String,
    val goodsId: String,
    val cellId: String?,
    val status: Int,
    val other: String? // JSON
)