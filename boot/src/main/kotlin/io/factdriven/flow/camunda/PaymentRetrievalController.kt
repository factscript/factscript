package io.factdriven.flow.camunda

import io.factdriven.flow.lang.Fact
import io.factdriven.flow.lang.Message
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class PaymentRetrievalController {

    @RequestMapping("/retrievePayment", method = [RequestMethod.POST])
    fun index(@RequestParam reference: String = "anOrderId", @RequestParam accountId: String = "anAccountId", @RequestParam payment: Float = 5F): String {
        val fact = RetrievePayment(reference, accountId, payment)
        return send(fact).toJson()
    }

    @RequestMapping("/confirmation", method = [RequestMethod.POST])
    fun index(@RequestParam reference: String = "anOrderId"): String {
        val fact = ConfirmationReceived(reference)
        return send(fact).toJson()
    }

    private fun send(fact: Fact): Message<*> {
        val message = Message(fact)
        CamundaBpmFlowExecutor.target(message).map {
            CamundaBpmFlowExecutor.correlate(it)
        }
        return message
    }

}