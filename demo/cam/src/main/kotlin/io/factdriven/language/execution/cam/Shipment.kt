package io.factdriven.language.execution.cam

import io.factdriven.language.flow

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class Shipment(val fact: ShipGoods) {

    val orderId = fact.orderId

    companion object {

        init {

            flow <Shipment> {

                on command ShipGoods::class promise {
                    report success ShipGoods::class
                }

                emit event GoodsShipped::class by {
                    GoodsShipped(orderId)
                }

            }

        }

    }

}

data class ShipGoods(val orderId: String)
data class GoodsShipped(val orderId: String)
