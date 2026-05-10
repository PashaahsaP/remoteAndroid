package com.example.wmswherther.data.factory

import com.example.wmsRemote.data.enums.OperationType
import com.example.wmsRemote.data.enums.StatusType
import com.example.wmswherther.data.db.Entityes.Change
import com.example.wmswherther.data.db.Entityes.InventoryDiffItem
import java.util.UUID

object InventoryDiffFactory {
    fun create(sessionId: String,
               catalogId: String,
               parentCellId: String,
               diffCount: Int,
               status: StatusType,
               isTe: Boolean,
               barcoder: String
               ): InventoryDiffItem {
        return InventoryDiffItem(
            id = UUID.randomUUID().toString(),
            inventorySessionId = sessionId,
            catalogId = catalogId,
            parentCellId = parentCellId,
            diffCount = diffCount,
            status = status.ordinal,
            isTE = isTe,
            barcode = barcoder,
            other = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            deletedAt = null,
            isDeleted = false
        )
    }
}