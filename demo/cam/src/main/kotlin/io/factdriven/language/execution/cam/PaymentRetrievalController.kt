package io.factdriven.language.execution.cam

import io.factdriven.execution.Messages
import io.factdriven.execution.Fact
import io.factdriven.execution.Message
import io.factdriven.language.impl.utils.prettyJson
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import kotlin.reflect.KClass

@RestController
class PaymentRetrievalController {

    @RequestMapping("/retrievePayment", method = [RequestMethod.POST])
    fun index(@RequestParam reference: String = "anOrderId", @RequestParam accountId: String = "anAccountId", @RequestParam payment: Float = 5F): String {
        val fact =
            RetrievePayment(reference, accountId, payment)
        return send(Payment::class, fact).prettyJson
    }

    @RequestMapping("/confirmation", method = [RequestMethod.POST])
    fun index(@RequestParam reference: String = "anOrderId"): String {
        val fact = ConfirmationReceived(reference)
        return send(CreditCard::class, fact).prettyJson
    }

    private fun send(type: KClass<*>, fact: Any): Message {
        val message = Message(type, Fact(fact))
        Messages.process(message)
        return message
    }

}