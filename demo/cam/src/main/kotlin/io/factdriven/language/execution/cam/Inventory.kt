package io.factdriven.language.execution.cam

import io.factdriven.language.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class Inventory(val fact: FetchGoodsFromInventory) {

    val orderId = fact.orderId

    companion object {

        init {

            flow <Inventory> {

                on command FetchGoodsFromInventory::class emit {
                    success event GoodsFetchedFromInventory::class
                }

                emit event {
                    GoodsFetchedFromInventory(orderId)
                }

            }

            flow <Inventory> {

                on command ReturnGoodsToInventory::class emit {
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
