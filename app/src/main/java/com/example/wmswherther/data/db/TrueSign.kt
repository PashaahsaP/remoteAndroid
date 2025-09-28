package com.example.wmswherther.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.wmsRemote.data.db.Cell

@Entity(tableName = "true_signs",
    foreignKeys = [
        ForeignKey(entity = Goods::class, parentColumns = ["id"], childColumns = ["goodsId"], ForeignKey.CASCADE),
        ForeignKey(entity = Catalog::class, parentColumns = ["id"], childColumns = ["catalogId"], ForeignKey.CASCADE),

    ])
data class TrueSign(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goodsId: Long,
    val catalogId: Long,
    val name: String?,
    val other: String? // JSON
)