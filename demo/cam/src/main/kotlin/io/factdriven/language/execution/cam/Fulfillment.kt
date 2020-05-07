package io.factdriven.language.execution.cam

import io.factdriven.language.flow

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

                on command FulfillOrder::class promise {
                    report success OrderFulfilled::class
                    report failure OrderNotFulfilled::class
                }

                emit event OrderFulfillmentStarted::class by {
                    OrderFulfillmentStarted(orderId, accountId, total)
                }

                execute all {

                    execute command FetchGoodsFromInventory::class by { FetchGoodsFromInventory (orderId) }

                } and {

                    execute command RetrievePayment::class by {
                        RetrievePayment(orderId, accountId, total)
                    } but {
                        on event PaymentFailed::class
                        emit event OrderNotFulfilled::class by { OrderNotFulfilled(orderId) }
                    }

                }

                emit event OrderReadyToShip::class by {
                    OrderReadyToShip(orderId)
                }

                execute command ShipGoods::class by { ShipGoods(orderId) }

                emit event OrderFulfilled::class by { OrderFulfilled(orderId) }

            }

        }

    }

}

data class FulfillOrder(val orderId: String, val accountId: String, val total: Float)
data class OrderFulfillmentStarted(val orderId: String, val accountId: String, val total: Float)
data class OrderReadyToShip(val orderId: String)
data class OrderFulfilled(val orderId: String)
data class OrderNotFulfilled(val orderId: String)
