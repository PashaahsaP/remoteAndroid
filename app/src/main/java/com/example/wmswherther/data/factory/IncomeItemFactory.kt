package com.example.wmswherther.data.factory

import com.example.wmswherther.Classes.IncomeItem

object IncomeItemFactory {
    fun createNewGoods(
        name: String,
        catalogId: String,
        parentCellId: String,
        parentCellName: String,
        supplierId: String,
        allCount: Int,
        teCount: Int

    ) : IncomeItem.NewGoodsItem{
        var item =  IncomeItem.NewGoodsItem(
            goodsName = name,
            catalogId = catalogId,
            supplierId = supplierId,
            )
        item.isSelected = false
        item.isExpanded = false
        item.isExpandable = false
        item.isShown = true
        item.haveCount = 0
        item.allCount = allCount
        item.parentCellId = parentCellId
        item.parentCellName = parentCellName
        item.teCount = teCount
        return item
    }
    fun copyNewGoodsPlusOne(
        newGoods: IncomeItem.NewGoodsItem

    ) : IncomeItem.NewGoodsItem{
        var item =  IncomeItem.NewGoodsItem(
            goodsName = newGoods.goodsName,
            catalogId = newGoods.catalogId,
            supplierId = newGoods.supplierId,
        )
        item.isSelected = newGoods.isSelected
        item.isExpanded = newGoods.isExpanded
        item.isExpandable = newGoods.isExpandable
        item.isShown = newGoods.isShown
        item.haveCount = newGoods.haveCount + 1
        item.allCount = newGoods.allCount
        item.parentCellId = newGoods.parentCellId
        item.parentCellName = newGoods.parentCellName
        item.teCount = newGoods.teCount
        return item
    }
    fun copyGoodsOrNewGoods(
        goods: IncomeItem,
        parentCellName: String,
        haveCount: Int,
        allCount: Int
    ) : IncomeItem{
        var item: IncomeItem = IncomeItem.GoodsItem(
            goodsName = "",
            id = "",
            catalogId = "",
            supplierId = "")

        if(goods is IncomeItem.NewGoodsItem) {
            item = IncomeItem.NewGoodsItem(
                goodsName = goods.goodsName,
                catalogId = goods.catalogId,
                supplierId = goods.supplierId,
            )
            item.isSelected = goods.isSelected
            item.isExpanded = goods.isExpanded
            item.isExpandable = goods.isExpandable
            item.isShown = false
            item.haveCount = haveCount
            item.allCount = allCount
            item.parentCellId = parentCellName
            item.parentCellName = parentCellName
            item.teCount = 0

        }else if(goods is IncomeItem.GoodsItem){
            item =  IncomeItem.GoodsItem(
                goodsName = goods.goodsName,
                id = goods.id,
                catalogId = goods.catalogId,
                supplierId = goods.supplierId,
            )
            item.isSelected = goods.isSelected
            item.isExpanded = goods.isExpanded
            item.isExpandable = goods.isExpandable
            item.isShown = false
            item.haveCount = haveCount
            item.allCount = allCount
            item.teCount = 0
            item.parentCellId = parentCellName
            item.parentCellName = parentCellName
        }

        return item
    }
    fun copyGoodsPlusOne(
        goods: IncomeItem.GoodsItem
    ) : IncomeItem.GoodsItem{
        var item =  IncomeItem.GoodsItem(
            goodsName = goods.goodsName,
            id = goods.id,
            catalogId = goods.catalogId,
            supplierId = goods.supplierId,
        )
        item.isSelected = goods.isSelected
        item.isExpanded = goods.isExpanded
        item.isExpandable = goods.isExpandable
        item.isShown = goods.isShown
        item.haveCount = goods.haveCount + 1
        item.allCount = goods.allCount
        item.teCount = goods.teCount
        item.parentCellId = goods.parentCellId
        item.parentCellName = goods.parentCellName
        return item
    }
    fun createVisibleGoods(
        name: String,
        id: String,
        catalogId: String,
        parentCellId: String,
        parentCellName: String,
        supplierId: String,
        allCount: Int

    ) : IncomeItem.GoodsItem{
        var item =  IncomeItem.GoodsItem(
            goodsName = name,
            id = id,
            catalogId = catalogId,
            supplierId = supplierId,
        )
        item.isSelected = false
        item.isExpanded = false
        item.isExpandable = false
        item.isShown = true
        item.haveCount = 0
        item.allCount = allCount
        item.teCount = 0
        item.parentCellId = parentCellId
        item.parentCellName = parentCellName
        return item
    }
    fun createGroupingGoods(
        goods: IncomeItem.GoodsItem,
        haveCount: Int,
        allCount: Int,
        teCount: Int
    ) : IncomeItem.GoodsItem{
        var item =  IncomeItem.GoodsItem(
            goodsName = goods.goodsName,
            id = goods.id,
            catalogId = goods.catalogId,
            supplierId = goods.supplierId
        )
        item.haveCount = haveCount
        item.allCount = allCount
        item.teCount = teCount
        item.parentCellId = goods.parentCellId
        item.parentCellName = goods.parentCellName
        item.isExpanded = goods.isExpanded
        item.isExpandable = goods.isExpandable
        item.isShown = goods.isShown
        item.isSelected = goods.isSelected

        return item
    }
    fun createGroupingNewGoods(
        goods: IncomeItem.NewGoodsItem,
        haveCount: Int,
        allCount: Int,
        teCount: Int
    ) : IncomeItem.NewGoodsItem{
        var item =  IncomeItem.NewGoodsItem(
            goodsName = goods.goodsName,
            catalogId = goods.catalogId,
            supplierId = goods.supplierId
        )
        item.haveCount = haveCount
        item.allCount = allCount
        item.teCount = teCount
        item.parentCellId = goods.parentCellId
        item.parentCellName = goods.parentCellName
        item.isExpanded = goods.isExpanded
        item.isExpandable = goods.isExpandable
        item.isShown = goods.isShown
        item.isSelected = goods.isSelected

        return item
    }
    fun createInvisibleGoods(
        name: String,
        id: String,
        catalogId: String,
        parentCellId: String,
        parentCellName: String,
        supplierId: String,
        allCount: Int

    ) : IncomeItem.GoodsItem{
        var item =  IncomeItem.GoodsItem(
            goodsName = name,
            id = id,
            catalogId = catalogId,
            supplierId = supplierId,
        )
        item.isSelected = false
        item.isExpanded = false
        item.isExpandable = false
        item.isShown = false
        item.haveCount = 0
        item.allCount = allCount
        item.parentCellId = parentCellId
        item.parentCellName = parentCellName
        item.teCount = 0

        return item
    }
    fun createVisibleTE(
        name: String,
        id: String,
        parentCellId: String,
        parentCellName: String,
        typeCellId: String,

    ) : IncomeItem.TEItem{
        var item =  IncomeItem.TEItem(
            teName = name,
            id = id,
            typeCellId = typeCellId,
        )
        item.isSelected = false
        item.isExpanded = false
        item.isExpandable = true
        item.isShown = true
        item.haveCount = 0
        item.allCount = 1
        item.parentCellId = parentCellId
        item.parentCellName = parentCellName
        item.teCount = 0

        return item
    }
    fun createNewTE(
        name: String,
        parentCellName: String,

        ) : IncomeItem.NewTEItem{
        var item =  IncomeItem.NewTEItem(
            teName = name
        )
        item.isSelected = false
        item.isExpanded = false
        item.isExpandable = true
        item.isShown = true
        item.haveCount = 0
        item.allCount = 0
        item.parentCellName = parentCellName
        item.teCount = 0

        return item
    }
    fun createInvisibleTE(
        name: String,
        id: String,
        parentCellId: String,
        parentCellName: String,
        typeCellId: String,

        ) : IncomeItem.TEItem{
        var item =  IncomeItem.TEItem(
            teName = name,
            id = id,
            typeCellId = typeCellId,
        )
        item.isSelected = false
        item.isExpanded = false
        item.isExpandable = true
        item.isShown = false
        item.haveCount = 0
        item.allCount = 1
        item.parentCellId = parentCellId
        item.parentCellName = parentCellName
        item.teCount = 0
        return item
    }
}