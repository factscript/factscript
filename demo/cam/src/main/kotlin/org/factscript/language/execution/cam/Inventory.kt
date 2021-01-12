package org.factscript.language.execution.cam

import org.factscript.language.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class Inventory1(fact: org.factscript.language.execution.cam.FetchGoodsFromInventory) {

    val orderId = fact.orderId
    var fetched = false

    fun load(fact: org.factscript.language.execution.cam.GoodsFetchedFromInventory) {
        fetched = true
    }

    companion object {

        init {

            flow <org.factscript.language.execution.cam.Inventory1> {

                on command org.factscript.language.execution.cam.FetchGoodsFromInventory::class emit {
                    success event org.factscript.language.execution.cam.GoodsFetchedFromInventory::class
                }

                emit success event { org.factscript.language.execution.cam.GoodsFetchedFromInventory(orderId) }

            }

        }

    }

}

class Inventory2(fact: org.factscript.language.execution.cam.ReturnGoodsToInventory) {

    val orderId = fact.orderId
    var returned = false

    fun load(fact: org.factscript.language.execution.cam.GoodsReturnedToInventory) {
        returned = true
    }

    companion object {

        init {

            flow <org.factscript.language.execution.cam.Inventory2> {

                on command org.factscript.language.execution.cam.ReturnGoodsToInventory::class emit {
                    success event org.factscript.language.execution.cam.GoodsReturnedToInventory::class
                }

                emit success event { org.factscript.language.execution.cam.GoodsReturnedToInventory(orderId) }

            }

        }

    }

}

data class FetchGoodsFromInventory(val orderId: String)
data class GoodsFetchedFromInventory(val orderId: String)
data class ReturnGoodsToInventory(val orderId: String)
data class GoodsReturnedToInventory(val orderId: String)
