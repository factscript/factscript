package io.factdriven.language.visualization.bpmn.model.compensation

import io.factdriven.language.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Inventory(fact: FetchGoodsFromInventory) {

    val orderId = fact.orderId
    var fetched = false

    fun load(fact: GoodsFetchedFromInventory) {
        fetched = true
    }

    fun load(fact: GoodsReturnedToInventory) {
        fetched = false
    }

    companion object {

        init {

            flow <Inventory> {

                on command FetchGoodsFromInventory::class emit {
                    success event GoodsFetchedFromInventory::class
                }

                emit success event { GoodsFetchedFromInventory(orderId) }

                on command ReturnGoodsToInventory::class having "orderId" match { orderId } emit {
                    success event GoodsReturnedToInventory::class
                }

                emit success event { GoodsReturnedToInventory(orderId) }

            }

        }

    }

}

data class FetchGoodsFromInventory(val orderId: String)
data class GoodsFetchedFromInventory(val orderId: String)
data class ReturnGoodsToInventory(val orderId: String)
data class GoodsReturnedToInventory(val orderId: String)
