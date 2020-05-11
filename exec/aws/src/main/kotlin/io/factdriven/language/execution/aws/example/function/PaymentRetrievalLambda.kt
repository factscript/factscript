package io.factdriven.language.execution.aws.example.function

import io.factdriven.aws.lambda.FlowlangLambda
import io.factdriven.language.*
import io.factdriven.language.definition.Flow
import io.factdriven.language.execution.aws.example.function.PaymentRetrieval

class PaymentRetrievalLambda : FlowlangLambda() {
    override fun definition(): Flow {
        PaymentRetrieval.init()
        return Flows.get(PaymentRetrieval::class)
    }
}