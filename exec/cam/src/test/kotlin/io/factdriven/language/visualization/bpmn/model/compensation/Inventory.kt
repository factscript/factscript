package io.factdriven.language.visualization.bpmn.model.compensation

import io.factdriven.language.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class FetchGoodsFromInventory(val orderId: String)
data class GoodsFetchedFromInventory(val orderId: String)
data class ReturnGoodsToInventory(val orderId: String)
data class GoodsReturnedToInventory(val orderId: String)
