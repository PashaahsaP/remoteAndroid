package com.example.wmswherther.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.wmsRemote.data.db.Cell

@Entity(tableName = "sessions_picker",
    foreignKeys = [
        ForeignKey(entity = Supplier::class, parentColumns = ["id"], childColumns = ["supplierId"], ForeignKey.CASCADE),
        ForeignKey(entity = Cell::class, parentColumns = ["id"], childColumns = ["outCellId"], ForeignKey.CASCADE),

    ])
data class SessionPicker(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supplierId: Long?,
    val outCellId: Long?,
    val status: String,
    val createdAt: Long,
    val startedAt: Long?,
    val finishedAt: Long?,
    val other: String? // JSON
)