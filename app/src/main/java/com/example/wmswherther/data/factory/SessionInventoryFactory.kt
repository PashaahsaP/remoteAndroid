package com.example.wmswherther.data.factory

import com.example.wmsRemote.data.enums.StatusType
import com.example.wmswherther.data.db.Entityes.SessionInventory
import java.util.UUID

object SessionInventoryFactory {
    fun create(supplierId: Int, cellId: String, prevSessionId: String?) : SessionInventory{
        return SessionInventory(
            id = UUID.randomUUID().toString(),
            supplierId = supplierId,
            cellId = cellId,
            prevSessionId = prevSessionId,
            status = StatusType.Created.ordinal,
            createdAt = System.currentTimeMillis(),
            startedAt = System.currentTimeMillis(),
            finishedAt = null,
            updatedAt = System.currentTimeMillis(),
            deletedAt = null,
            isDeleted = false,
            other = null
        )
    }
}