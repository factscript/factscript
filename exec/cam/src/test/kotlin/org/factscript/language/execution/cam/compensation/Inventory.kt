package org.factscript.language.execution.cam.compensation

import org.factscript.language.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Inventory1(fact: FetchGoodsFromInventory) {

    val orderId = fact.orderId
    var fetched = false

    fun load(fact: GoodsFetchedFromInventory) {
        fetched = true
    }

    companion object {

        init {

            flow <Inventory1> {

                on command FetchGoodsFromInventory::class emit {
                    success event GoodsFetchedFromInventory::class
                }

                emit success event { GoodsFetchedFromInventory(orderId) }

            }

        }

    }

}

class Inventory2(fact: ReturnGoodsToInventory) {

    val orderId = fact.orderId
    var returned = false

    fun load(fact: GoodsReturnedToInventory) {
        returned = true
    }

    companion object {

        init {

            flow <Inventory2> {

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
