package com.example.wmswherther.data.factory

import com.example.wmsRemote.data.enums.StatusType
import com.example.wmswherther.data.db.Entityes.PickerItem
import java.util.UUID

object PickerItemFactory {
    fun create(sesionId: String, goodsId: String, cellId: String): PickerItem{
        return PickerItem(
            id = UUID.randomUUID().toString(),
            sessionId = sesionId,
            goodsId = goodsId,
            cellId = cellId,
            status = StatusType.Created.ordinal,
            startedAt = System.currentTimeMillis(),
            finishedAt = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            deletedAt = null,
            isDeleted = false,
            other = null
        )
    }
}