package io.factdriven.aws.lambda

import io.factdriven.aws.example.function.PaymentRetrieval
import io.factdriven.definition.Definition
import io.factdriven.definition.Definitions

class PaymentRetrievalLambda : FlowlangLambda() {
    override fun definition(): Definition {
        PaymentRetrieval.init()
        return Definitions.getDefinitionByType(PaymentRetrieval::class)
    }
}