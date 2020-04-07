package io.factdriven.aws.lambda

import io.factdriven.aws.example.function.PaymentRetrieval
import io.factdriven.definition.api.Flow
import io.factdriven.Flows

class PaymentRetrievalLambda : FlowlangLambda() {
    override fun definition(): Flow {
        PaymentRetrieval.init()
        return Flows.get(PaymentRetrieval::class)
    }
}