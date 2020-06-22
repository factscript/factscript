package io.factdriven.language.execution.example

import io.factdriven.execution.Messages
import io.factdriven.execution.Fact
import io.factdriven.execution.Message
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import kotlin.reflect.KClass

@RestController
class ExampleController {

    @RequestMapping("/charge", method = [RequestMethod.POST])
    fun chargeCreditCard(@RequestParam reference: String, @RequestParam charge: Float) {
        send(ChargeCreditCard::class, ChargeCreditCard(reference, charge))
    }

    @RequestMapping("/creditCardProcessed", method = [RequestMethod.POST])
    fun creditCardProcessed(@RequestParam orderId: String, @RequestParam valid: Boolean = true) {
        send(CreditCard::class, CreditCardProcessed(orderId, valid))
    }

    private fun send(type: KClass<*>, fact: Any) {
        Messages.process(Message(type, Fact(fact)))
    }

}
