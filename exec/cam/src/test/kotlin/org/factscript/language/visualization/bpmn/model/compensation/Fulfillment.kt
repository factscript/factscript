package org.factscript.language.visualization.bpmn.model.compensation

import org.factscript.language.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class Fulfillment(val incoming: FulfillOrder) {

    val orderId = incoming.orderId
    val accountId = incoming.accountId

    val total = incoming.total

    companion object {

        init {

            flow <Fulfillment> {

                on command FulfillOrder::class emit {
                    success event OrderFulfilled::class
                    failure event OrderNotFulfilled::class
                }

                emit event { OrderFulfillmentStarted(orderId, accountId, total) }

                execute all {

                    execute command {
                        FetchGoodsFromInventory (orderId)
                    } but {
                        on failure OrderNotFulfilled::class
                        execute command {
                            ReturnGoodsToInventory(orderId)
                        }
                    }

                } and {

                    execute command {
                        RetrievePayment(orderId, accountId, total)
                    } but {
                        on failure PaymentFailed::class
                        emit failure event { OrderNotFulfilled(orderId) }
                    }

                }

                emit event { OrderReadyToShip(orderId) }

                execute command { ShipGoods(orderId) }

                emit success event { OrderFulfilled(orderId) }

            }

        }

    }

}

data class FulfillOrder(val orderId: String, val accountId: String, val total: Float)
data class OrderFulfillmentStarted(val orderId: String, val accountId: String, val total: Float)
data class OrderReadyToShip(val orderId: String)
data class OrderFulfilled(val orderId: String)
data class OrderNotFulfilled(val orderId: String)
