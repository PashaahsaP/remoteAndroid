package com.example.wmswherther.data.factory

import com.example.wmsRemote.data.enums.OperationType
import com.example.wmswherther.data.db.Entityes.Movement
import java.util.UUID

object MovementFactory {
    fun create (
        cellFromId: String,
        cellToId: String,
        catalogId: String,
        goodsId: String,
        qty: String,
        operationType: OperationType,
        entityId: String
    ) : Movement{
        return Movement(
            id = UUID.randomUUID().toString(),
            cellFromId = cellFromId,
            cellToId = cellToId,
            catalogId = catalogId,
            goodsId = goodsId,
            qty = qty,
            userId = 1,
            executedAt = System.currentTimeMillis(),
            operationType = operationType.ordinal,
            entityId = entityId,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            deletedAt = null,
            isDeleted = false
        )
    }
}