package io.factdriven.language.execution.aws.lambda

import io.factdriven.language.execution.aws.example.function.PaymentRetrieval
import io.factdriven.language.definition.Flow
import io.factdriven.language.Flows

class PaymentRetrievalLambda : FlowlangLambda() {
    override fun definition(): Flow {
        PaymentRetrieval.init()
        return Flows.get(PaymentRetrieval::class)
    }
}