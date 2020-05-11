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

                on command FetchGoodsFromInventory::class promise {
                    report success GoodsFetchedFromInventory::class
                }

                emit event {
                    GoodsFetchedFromInventory(orderId)
                }

            }

            flow <Inventory> {

                on command ReturnGoodsToInventory::class promise {
                    report success GoodsReturnedToInventory::class
                }

                emit event { GoodsReturnedToInventory(orderId) }

            }

        }

    }

}

data class FetchGoodsFromInventory(val orderId: String)
data class GoodsFetchedFromInventory(val orderId: String)
data class ReturnGoodsToInventory(val orderId: String)
data class GoodsReturnedToInventory(val orderId: String)
