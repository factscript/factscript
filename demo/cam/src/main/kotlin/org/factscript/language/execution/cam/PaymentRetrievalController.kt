package org.factscript.language.execution.cam

import org.factscript.execution.Messages
import org.factscript.execution.Fact
import org.factscript.execution.Message
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import kotlin.reflect.KClass

@RestController
class PaymentRetrievalController {

    @RequestMapping("/fulfillOrder", method = [RequestMethod.POST])
    fun fulfillOrder(@RequestParam orderId: String = "anOrderId", @RequestParam accountId: String = "anAccountId", @RequestParam payment: Float = 5F) {
        send(org.factscript.language.execution.cam.Fulfillment::class, org.factscript.language.execution.cam.FulfillOrder(orderId, accountId, payment))
    }

    @RequestMapping("/creditCardDetailsUpdated", method = [RequestMethod.POST])
    fun creditCardDetailsUpdated(@RequestParam accountId: String = "anAccountId") {
        send(org.factscript.language.execution.cam.Payment::class, org.factscript.language.execution.cam.CreditCardDetailsUpdated(accountId))
    }

    @RequestMapping("/creditCardProcessed", method = [RequestMethod.POST])
    fun creditCardProcessed(@RequestParam orderId: String = "anOrderId", @RequestParam valid: Boolean = true) {
        send(org.factscript.language.execution.cam.CreditCard::class, org.factscript.language.execution.cam.CreditCardProcessed(orderId, valid))
    }

    private fun send(type: KClass<*>, fact: Any) {
        Messages.process(Message(type, Fact(fact)))
    }

}