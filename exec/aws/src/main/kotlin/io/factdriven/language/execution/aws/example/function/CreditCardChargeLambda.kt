package io.factdriven.language.execution.aws.example.function

import io.factdriven.aws.lambda.FlowlangLambda
import io.factdriven.language.Flows
import io.factdriven.language.definition.Flow

class CreditCardChargeLambda : FlowlangLambda() {
    override fun definition(): Flow {
        Flows.activate(CreditCardCharge::class)
        return Flows.get(CreditCardCharge::class)
    }
}