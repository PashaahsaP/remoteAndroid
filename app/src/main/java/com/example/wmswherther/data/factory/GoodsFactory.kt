package com.example.wmswherther.data.factory

import com.example.wmswherther.data.db.Entityes.Goods
import java.util.UUID

object GoodsFactory {
    fun create(
        amount: Int,
        cellId: String,
        catalogId: String,
        isAvailable: Boolean
    ): Goods{
        return Goods(
            id = UUID.randomUUID().toString(),
            amount = amount,
            cellId = cellId,
            catalogId = catalogId,
            createdAt = System.currentTimeMillis(),
            isAvailable = isAvailable,
            other = null
        )
    }
}