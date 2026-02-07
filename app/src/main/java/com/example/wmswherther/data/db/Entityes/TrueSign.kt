package com.example.wmswherther.data.db.Entityes

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "true_signs",
    foreignKeys = [
        ForeignKey(entity = Goods::class, parentColumns = ["id"], childColumns = ["goodsId"], ForeignKey.CASCADE),
        ForeignKey(entity = Catalog::class, parentColumns = ["id"], childColumns = ["catalogId"], ForeignKey.CASCADE),

    ])
data class TrueSign(
    @PrimaryKey(autoGenerate = false) val id: String,
    val goodsId: String,
    val catalogId: String,
    val name: String?,
    val other: String? // JSON
)