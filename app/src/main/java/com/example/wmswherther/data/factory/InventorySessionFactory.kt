package com.example.wmswherther.data.factory

import com.example.wmsRemote.data.enums.StatusType
import com.example.wmswherther.data.db.Entityes.SessionInventory
import java.util.UUID

object InventorySessionFactory {
    /**
     * Creating for supplier mode, when create new session in process inventory, not inventory task
     */

    fun createNotInventoryTask(
        supplierId: Int?,
        cellId: String,
        status: StatusType,

    ) : SessionInventory {
        return SessionInventory(
            id = UUID.randomUUID().toString(),
            supplierId = supplierId,
            cellId = cellId,
            prevSessionId = "",
            status = status.ordinal,
            createdAt = System.currentTimeMillis(),
            startedAt = System.currentTimeMillis(),
            finishedAt = System.currentTimeMillis(),
            other = null,
            updatedAt = System.currentTimeMillis(),
            deletedAt = null,
            isDeleted = false
        )
    }
}