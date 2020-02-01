package io.factdriven.flow.camunda

import io.factdriven.play.Fact
import io.factdriven.play.Message
import io.factdriven.play.Player
import io.factdriven.play.toJson
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

    private fun send(fact: Any): Message {
        val message = Message(Fact(fact))
        Player.process(message)
        return message
    }

}