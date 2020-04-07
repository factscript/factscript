package io.factdriven.aws.lambda

import io.factdriven.aws.example.function.PaymentRetrieval
import io.factdriven.definition.api.Flowing
import io.factdriven.definition.Flows

class PaymentRetrievalLambda : FlowlangLambda() {
    override fun definition(): Flowing {
        PaymentRetrieval.init()
        return Flows.get(PaymentRetrieval::class)
    }
}