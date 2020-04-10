package io.factdriven.flow.camunda

import io.factdriven.impl.execution.Fact
import io.factdriven.impl.execution.Message
import io.factdriven.Messages
import io.factdriven.impl.utils.json
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import kotlin.reflect.KClass

@RestController
class PaymentRetrievalController {

    @RequestMapping("/retrievePayment", method = [RequestMethod.POST])
    fun index(@RequestParam reference: String = "anOrderId", @RequestParam accountId: String = "anAccountId", @RequestParam payment: Float = 5F): String {
        val fact = RetrievePayment(reference, accountId, payment)
        return send(PaymentRetrieval::class, fact).json
    }

    @RequestMapping("/confirmation", method = [RequestMethod.POST])
    fun index(@RequestParam reference: String = "anOrderId"): String {
        val fact = ConfirmationReceived(reference)
        return send(CreditCardCharge::class, fact).json
    }

    private fun send(type: KClass<*>, fact: Any): Message {
        val message = Message(type, Fact(fact))
        Messages.process(message)
        return message
    }

}