package com.example.wmswherther.data.factory

import com.example.wmswherther.data.db.Entityes.Cell
import java.util.UUID

object CellFactory {
    fun create(typeCellId: String,
               parentCellId: String?,
               name: String): Cell {
        return Cell(
            id = UUID.randomUUID().toString(),
            typeCellId = typeCellId,
            parentCellId = parentCellId,
            name = name,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            deletedAt = null,
            isDeleted = false,
            other = null
        )
    }
}