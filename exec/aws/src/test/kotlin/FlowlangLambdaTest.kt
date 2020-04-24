import io.factdriven.language.execution.aws.lambda.PaymentRetrievalLambda
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
class FlowlangLambdaTest {


    @Test @Disabled
    fun test(){

        PaymentRetrievalLambda().handleRequest(mapOf<String, Any>("id" to "io.factdriven.language.execution.aws.example.function-PaymentRetrieval-Exclusive",
                "TaskToken" to "1",
                "History" to arrayListOf<String>()
        ), null)
    }

}