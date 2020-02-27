package io.factdriven.aws.example.function

import io.factdriven.aws.lambda.PaymentRetrievalLambda
import org.junit.jupiter.api.Test

class FlowlangLambdaTest {

    val lambda = PaymentRetrievalLambda()

    @Test
    fun testProcessor() {
        lambda.handleRequest(null, null)
    }
}