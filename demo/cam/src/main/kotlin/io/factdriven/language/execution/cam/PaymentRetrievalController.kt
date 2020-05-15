package io.factdriven.language.execution.cam

import io.factdriven.execution.Messages
import io.factdriven.execution.Fact
import io.factdriven.execution.Message
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import kotlin.reflect.KClass

@RestController
class PaymentRetrievalController {

    @RequestMapping("/fulfillOrder", method = [RequestMethod.POST])
    fun fulfillOrder(@RequestParam orderId: String = "anOrderId", @RequestParam accountId: String = "anAccountId", @RequestParam payment: Float = 5F) {
        send(Fulfillment::class, FulfillOrder(orderId, accountId, payment))
    }

    @RequestMapping("/creditCardDetailsUpdated", method = [RequestMethod.POST])
    fun creditCardDetailsUpdated(@RequestParam accountId: String = "anAccountId") {
        send(Payment::class, CreditCardDetailsUpdated(accountId))
    }

    @RequestMapping("/creditCardProcessed", method = [RequestMethod.POST])
    fun creditCardProcessed(@RequestParam orderId: String = "anOrderId", @RequestParam valid: Boolean = true) {
        send(CreditCard::class, CreditCardProcessed(orderId, valid))
    }

    private fun send(type: KClass<*>, fact: Any) {
        Messages.process(Message(type, Fact(fact)))
    }

}