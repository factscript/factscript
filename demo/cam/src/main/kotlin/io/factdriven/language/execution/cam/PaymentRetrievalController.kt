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

    @RequestMapping("/order", method = [RequestMethod.POST])
    fun index(@RequestParam orderId: String = "anOrderId", @RequestParam accountId: String = "anAccountId", @RequestParam payment: Float = 5F) {
        val fact = FulfillOrder(orderId, accountId, payment)
        send(Fulfillment::class, fact).prettyJson
    }

    @RequestMapping("/confirm", method = [RequestMethod.POST])
    fun index(@RequestParam orderId: String = "anOrderId", @RequestParam valid: Boolean = true) {
        val fact = ConfirmationReceived(orderId, valid)
        send(CreditCard::class, fact).prettyJson
    }

    private fun send(type: KClass<*>, fact: Any): Message {
        val message = Message(type, Fact(fact))
        Messages.process(message)
        return message
    }

}