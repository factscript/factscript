package io.factdriven.language.execution.cam

import io.factdriven.language.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class Fulfillment(val incoming: FulfillOrder) {

    val orderId = incoming.orderId
    val accountId = incoming.accountId

    val total = incoming.total

    var started = false
    var fetched = false
    var paid = false
    var readyToShip = false
    var shipped = false
    var fulfilled = false

    fun apply(fact: OrderFulfillmentStarted) { started = true }
    fun apply(fact: GoodsFetchedFromInventory) { fetched = true }
    fun apply(fact: GoodsReturnedToInventory) { fetched = false }
    fun apply(fact: PaymentRetrieved) { paid = true }
    fun apply(fact: PaymentFailed) { paid = false }
    fun apply(fact: OrderReadyToShip) { readyToShip = true }
    fun apply(fact: GoodsShipped) { shipped = true }
    fun apply(fact: OrderFulfilled) { fulfilled = true }

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
                        execute command { ReturnGoodsToInventory(orderId) }
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
