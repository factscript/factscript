package io.factdriven.aws.lambda

import io.factdriven.aws.example.function.PaymentRetrieval
import io.factdriven.definition.Definition

class PaymentRetrievalLambda : FlowlangLambda() {
    override fun definition(): Definition {
        PaymentRetrieval.init()
        return Definition.getDefinitionByType(PaymentRetrieval::class)
    }
}