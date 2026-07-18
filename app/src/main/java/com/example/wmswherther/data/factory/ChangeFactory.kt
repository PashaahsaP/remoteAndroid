package com.example.wmswherther.data.factory

import com.example.wmsRemote.data.enums.OperationType
import com.example.wmsRemote.data.enums.StatusType
import com.example.wmswherther.data.db.Entityes.Change
import java.util.UUID

object ChangeFactory {
    fun create(payload: String, payloadBefore: String, entityId: String, supplierId: Int?, operationType: OperationType): Change {
        return Change(
            id = UUID.randomUUID().toString(),
            entityId = entityId,
            payload = payload,
            payloadBefore = payloadBefore,
            operationType = operationType.ordinal,
            status = StatusType.Created.ordinal,
            supplierId = supplierId,
            other = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            deletedAt = null,
            isDeleted = false
        )
    }
}