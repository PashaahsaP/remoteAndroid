package com.example.wmswherther.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.wmsRemote.data.db.Cell

@Entity(tableName = "sessions_income",
    foreignKeys = [
        ForeignKey(entity = Supplier::class, parentColumns = ["id"], childColumns = ["supplierId"], ForeignKey.CASCADE),
        ForeignKey(entity = Cell::class, parentColumns = ["id"], childColumns = ["incomeCellId"], ForeignKey.CASCADE),
        ForeignKey(entity = Cell::class, parentColumns = ["id"], childColumns = ["toCellId"], ForeignKey.CASCADE)



    ])
data class SessionIncome(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supplierId: Long?,
    val incomeCellId: Long?,
    val toCellId: Long?,
    val status: Int,
    val createdAt: Long,
    val startedAt: Long?,
    val finishedAt: Long?,
    val other: String? // JSON
)