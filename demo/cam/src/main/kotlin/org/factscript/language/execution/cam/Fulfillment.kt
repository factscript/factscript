package org.factscript.language.execution.cam

import org.factscript.language.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
data class Fulfillment(val incoming: org.factscript.language.execution.cam.FulfillOrder) {

    val orderId = incoming.orderId
    val accountId = incoming.accountId

    val total = incoming.total

    var started = false
    var fetched = false
    var paid = false
    var readyToShip = false
    var shipped = false
    var fulfilled = false

    fun apply(fact: org.factscript.language.execution.cam.OrderFulfillmentStarted) { started = true }
    fun apply(fact: org.factscript.language.execution.cam.GoodsFetchedFromInventory) { fetched = true }
    fun apply(fact: org.factscript.language.execution.cam.GoodsReturnedToInventory) { fetched = false }
    fun apply(fact: org.factscript.language.execution.cam.PaymentRetrieved) { paid = true }
    fun apply(fact: org.factscript.language.execution.cam.PaymentFailed) { paid = false }
    fun apply(fact: org.factscript.language.execution.cam.OrderReadyToShip) { readyToShip = true }
    fun apply(fact: org.factscript.language.execution.cam.GoodsShipped) { shipped = true }
    fun apply(fact: org.factscript.language.execution.cam.OrderFulfilled) { fulfilled = true }

    companion object {

        init {

            flow <org.factscript.language.execution.cam.Fulfillment> {

                on command org.factscript.language.execution.cam.FulfillOrder::class emit {
                    success event org.factscript.language.execution.cam.OrderFulfilled::class
                    failure event org.factscript.language.execution.cam.OrderNotFulfilled::class
                }

                emit event { org.factscript.language.execution.cam.OrderFulfillmentStarted(orderId, accountId, total) }

                execute all {

                    execute command {
                        org.factscript.language.execution.cam.FetchGoodsFromInventory(orderId)
                    } but {
                        on failure org.factscript.language.execution.cam.OrderNotFulfilled::class
                        execute command { org.factscript.language.execution.cam.ReturnGoodsToInventory(orderId) }
                    }

                } and {

                    execute command {
                        org.factscript.language.execution.cam.RetrievePayment(orderId, accountId, total)
                    } but {
                        on failure org.factscript.language.execution.cam.PaymentFailed::class
                        emit failure event { org.factscript.language.execution.cam.OrderNotFulfilled(orderId) }
                    }

                }

                emit event { org.factscript.language.execution.cam.OrderReadyToShip(orderId) }

                execute command { org.factscript.language.execution.cam.ShipGoods(orderId) }

                emit success event { org.factscript.language.execution.cam.OrderFulfilled(orderId) }

            }

        }

    }

}

data class FulfillOrder(val orderId: String, val accountId: String, val total: Float)
data class OrderFulfillmentStarted(val orderId: String, val accountId: String, val total: Float)
data class OrderReadyToShip(val orderId: String)
data class OrderFulfilled(val orderId: String)
data class OrderNotFulfilled(val orderId: String)
