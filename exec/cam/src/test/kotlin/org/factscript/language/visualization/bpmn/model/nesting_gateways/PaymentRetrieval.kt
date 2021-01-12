package org.factscript.language.visualization.bpmn.model.nesting_gateways

import org.factscript.language.*
import java.util.*

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class PaymentRetrieval(fact: RetrievePayment) {

    var id = UUID.randomUUID().toString()
    var total = fact.amount
    var covered = 0F

    companion object {

        init {

            flow<PaymentRetrieval> {

                on command RetrievePayment::class

                select("Traffic light?") either {
                    given("Green")
                    execute all {
                        execute command {
                            ChargeCreditCard(
                                id,
                                total - covered
                            )
                        }
                    } and {
                        repeat {
                            repeat {
                                execute command {
                                    ChargeCreditCard(
                                        id,
                                        total - covered
                                    )
                                }
                                select all {
                                    given("A") condition { true }
                                    execute command {
                                        ChargeCreditCard(
                                            id,
                                            total - covered
                                        )
                                    }
                                } or {
                                    given("B") condition { true }
                                    execute command {
                                        ChargeCreditCard(
                                            id,
                                            total - covered
                                        )
                                    }
                                    execute command {
                                        ChargeCreditCard(
                                            id,
                                            total - covered
                                        )
                                    }
                                }
                                await first {
                                    on event CreditCardUnvalidated::class
                                    execute command {
                                        org.factscript.language.visualization.bpmn.model.await_first.ChargeCreditCard(
                                            id,
                                            1F
                                        )
                                    }
                                } or {
                                    on event CreditCardValidated::class
                                }
                                until("The world is flat?") condition { true }
                            }
                            until("The world is flat?") condition { true }
                        }
                    } and {
                        execute command {
                            ChargeCreditCard(
                                id,
                                total - covered
                            )
                        }
                    }
                    execute command {
                        ChargeCreditCard(
                            id,
                            total - covered
                        )
                    }
                } or {
                    given("Red") condition { true }
                    execute command {
                        ChargeCreditCard(
                            id,
                            total - covered
                        )
                    }
                    select("End game?") either {
                        given("Yes") condition { true }
                        execute command {
                            ChargeCreditCard(
                                id,
                                total - covered
                            )
                        }
                    } or {
                        given("No")
                        execute command {
                            ChargeCreditCard(
                                id,
                                total - covered
                            )
                        }
                        execute command {
                            ChargeCreditCard(
                                id,
                                total - covered
                            )
                        }
                    }
                    execute command {
                        ChargeCreditCard(
                            id,
                            total - covered
                        )
                    }
                }

                emit event {
                    PaymentRetrieved(total)
                }

            }

        }

    }

}

data class RetrievePayment(val amount: Float)
data class PaymentRetrieved(val amount: Float)
class CreditCardUnvalidated
class CreditCardValidated
