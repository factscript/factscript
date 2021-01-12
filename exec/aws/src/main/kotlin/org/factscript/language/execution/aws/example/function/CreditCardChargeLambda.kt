package org.factscript.language.execution.aws.example.function

import org.factscript.aws.lambda.FlowlangLambda
import org.factscript.language.Flows
import org.factscript.language.definition.Flow

class CreditCardChargeLambda : FlowlangLambda() {
    override fun definition(): Flow {
        Flows.activate(CreditCardCharge::class)
        return Flows.get(CreditCardCharge::class)
    }
}