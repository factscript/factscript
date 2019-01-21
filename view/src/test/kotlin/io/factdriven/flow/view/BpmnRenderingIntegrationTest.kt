package io.factdriven.flow.view

import io.factdriven.flow.execute
import io.factdriven.flow.lang.FlowExecution
import org.camunda.bpm.model.bpmn.Bpmn
import org.junit.jupiter.api.Test
import java.io.File

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class BpmnRenderingIntegrationTest {

    @Test
    fun testPaymentRetrievalVersion1() {

            val process = execute <PaymentRetrieval> {

                on message (RetrievePayment::class) create acceptance ("RetrievePayment") by {
                    PaymentRetrievalAccepted(paymentId = it.id)
                }

                execute service {
                    create intent ("ChargeCreditCard") by { ChargeCreditCard() }
                    on message CreditCardCharged::class create success("CreditCardCharged")
                }

                create progress ("PaymentCovered")

                execute service {
                    create intent ("NotifyCustomer") by { NotifyCustomer() }
                }

                execute service {
                    on message (CustomerNotified::class) create success("CustomerNotificationConfirmed")
                }

                create success ("PaymentRetrieved") by { PaymentRetrieved() }

            }

        render(process)
    }

    fun render(flow: FlowExecution<*>) {
        val container = translate(flow)
        val bpmnModelInstance = transform(container)
        Bpmn.validateModel(bpmnModelInstance);
        val file = File.createTempFile("./bpmn-model-api-", ".bpmn")
        Bpmn.writeModelToFile(file, bpmnModelInstance)
        if("Mac OS X" == System.getProperty("os.name"))
            Runtime.getRuntime().exec("open " + file.absoluteFile);
    }

}


data class PaymentRetrieval(val init: RetrievePayment) {

    val paymentId = init.id
    val accountId = init.accountId
    var uncovered = init.payment
    var covered = 0F

}

class RetrievePayment(val id: String, val accountId: String, val payment: Float)
class NotifyCustomer(val id: String? = null, val accountId: String? = null, val payment: Float? = null)
class CustomerNotified(val id: String, val accountId: String, val payment: Float)
class PaymentRetrievalAccepted(val paymentId: String? = null)
class PaymentRetrieved(val paymentId: String? = null)
class PaymentFailed(val paymentId: String)
class PaymentCoveredManually(val paymentId: String)
class ChargeCreditCard(val reference: String? = null, val payment: Float? = null)
class CreditCardCharged(val reference: String)
class CreditCardExpired(val reference: String)
class CreditCardDetailsUpdated(val reference: String)
class WithdrawAmount(val reference: String, val payment: Float)
class CreditAmount(val reference: String)
class AmountWithdrawn(val reference: String)