import io.factdriven.aws.lambda.PaymentRetrievalLambda
import org.junit.jupiter.api.Test
class FlowlangLambdaTest {


    @Test
    fun test(){
        PaymentRetrievalLambda().handleRequest(emptyMap<String, String>(), null)
    }

}