package com.example.wmsRemote.data.enums

enum class OperationType {
    InsertCell,
    InsertCatalog,
    InsertGoods,
    InsertIncomeSession,
    InsertIncomeItem,
    InsertBarcode,
    InsertInventorySession,
    InsertInventoryDiff,
    InsertPickerSession,
    InsertPickerItem,
    InsertMovement,
    DeleteGoods,
    DeleteIncomeSession,
    UpdateGoods,
    UpdatePickerSession,
    UpdateIncomeSession,
    UpdateInventorySession,

    UpdateCell,
    AssemblyMovement,
    IncomeMovement,
    MoreMovement,
    LessMovement,
    Movement
}