package com.example.wmswherther.Classes

data class TaskMenuItem(
    var supplierId: String,
    var sessionId: String,
    var supplier: String,
    var progress: String,
    var number: String,
    var date: String,
)
data class IncomeItem(
    var name: String,
    var TE: String,
    var catalogId: String,
    var haveCount: Int,
    var allCount: Int,
    var teCount:Int = 0,
    var isSelected: Boolean = false,
    var isExpanded: Boolean = false,
    var isExpandable: Boolean = false,
    var isShown: Boolean = true
)
data class InventorySessionItem(
    var name: String,
    var TE: String,
    var catalogId: String,
    var haveCount: Int,
    var allCount: Int,
    var teCount:Int = 0,
    var isSelected: Boolean = false,
    var isExpanded: Boolean = false,
    var isExpandable: Boolean = false,
    var isShown: Boolean = true
)
data class MoveItem(
    val name: String,
    val id: String
)
data class InventoryItem(
    val name: String,
    val id: String
)
data class MoveSessionItem(
    var name: String,
    var catalogId: String,
    var goodsId: String,
    var allCount: Int,
    var haveCount: Int,
    var isSelected: Boolean,
    var isCell: Boolean,
)
data class AssemblyItem(
    var sessionId: String,
    var catalogId: String,
    var name: String,
    var assemblyItemId: String,
    var goodsId: String,
    var amount: Int,
    var cell: String,
    var status: Int,
    var pickerList: List<PickerItem>// need for adapter
)
data class PickerItem(
    var name: String,
    var data: List<String>,
    var isSelected: Boolean
)

