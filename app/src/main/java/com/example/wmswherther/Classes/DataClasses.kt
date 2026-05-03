package com.example.wmswherther.Classes

data class TaskMenuItem(
    var supplierId: String,
    var sessionId: String,
    var supplier: String,
    var progress: String,
    var number: String,
    var date: String,
)
sealed class IncomeItem {
    var isSelected: Boolean = false
    var isExpanded: Boolean = false
    var isExpandable: Boolean = false
    var isShown: Boolean = true
    var haveCount: Int = 0
    var allCount: Int = 0
    var teCount: Int = 0
    var parentCellId: String = ""
    var parentCellName: String = ""

    data class UnknowItem(
        val name: String,
    ) : IncomeItem()
    data class NewGoodsItem(
        var goodsName: String,
        var catalogId: String,
        var supplierId: String,
    ) : IncomeItem()
    data class GoodsItem(
        val goodsName: String,
        val id: String,
        val catalogId: String,
        val supplierId: String,
    ) : IncomeItem()

    data class TEItem(
        val teName: String,
        val id: String,
        val typeCellId: String,

    ) : IncomeItem()
    data class NewTEItem(
        val teName: String,
        ) : IncomeItem()
    fun getName() : String{
        return when{
            this is GoodsItem -> goodsName
            this is NewGoodsItem -> goodsName
            this is TEItem -> teName
            this is NewTEItem -> teName
            this is UnknowItem -> name
            else -> "Unknow"
        }
    }

    /**
     * Get catalogId Of goods item, if other is "nothing"
     */
    fun getCatalogIdOfItem(): String{
        return when{
            this is GoodsItem -> catalogId
            this is NewGoodsItem -> catalogId
            else -> "Unknow"
        }
    }
}

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

