package org.factscript.language.execution.aws.example.function

import org.factscript.aws.lambda.FlowlangLambda
import org.factscript.language.*
import org.factscript.language.definition.Flow
import org.factscript.language.execution.aws.example.function.PaymentRetrieval

class PaymentRetrievalLambda : FlowlangLambda() {
    override fun definition(): Flow {
        PaymentRetrieval.init()
        return Flows.get(PaymentRetrieval::class)
    }
}