package io.factdriven.language.execution.cam

import io.factdriven.language.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class Shipment(val fact: ShipGoods) {

    val orderId = fact.orderId

    companion object {

        init {

            flow <Shipment> {

                on command ShipGoods::class emit {
                    success event ShipGoods::class
                }

                emit success event { GoodsShipped(orderId) }

            }

        }

    }

}

data class ShipGoods(val orderId: String)
data class GoodsShipped(val orderId: String)
