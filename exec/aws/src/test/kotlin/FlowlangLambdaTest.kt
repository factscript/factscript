import io.factdriven.aws.lambda.PaymentRetrievalLambda
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
class FlowlangLambdaTest {


    @Test @Disabled
    fun test(){
        PaymentRetrievalLambda().handleRequest(emptyMap<String, String>(), null)
    }

}