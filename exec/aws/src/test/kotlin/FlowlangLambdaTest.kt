import com.amazonaws.services.lambda.runtime.ClientContext
import com.amazonaws.services.lambda.runtime.CognitoIdentity
import org.factscript.language.execution.aws.example.function.PaymentRetrievalLambda
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger

class FlowlangLambdaTest {


    @Test @Disabled
    fun test(){

        PaymentRetrievalLambda().handleRequest(
                mapOf<String, Any?>("id" to "org.factscript.language.execution.aws.example.function-PaymentRetrieval-Exclusive",
                        "TaskToken" to "1",
                        "History" to null
                ),
                context = object: Context {
                    override fun getAwsRequestId(): String {
                        TODO("Not yet implemented")
                    }

                    override fun getLogStreamName(): String {
                        TODO("Not yet implemented")
                    }

                    override fun getClientContext(): ClientContext {
                        TODO("Not yet implemented")
                    }

                    override fun getFunctionName(): String {
                        TODO("Not yet implemented")
                    }

                    override fun getRemainingTimeInMillis(): Int {
                        TODO("Not yet implemented")
                    }

                    override fun getLogger(): LambdaLogger {
                        TODO("Not yet implemented")
                    }

                    override fun getInvokedFunctionArn(): String {
                        TODO("Not yet implemented")
                    }

                    override fun getMemoryLimitInMB(): Int {
                        TODO("Not yet implemented")
                    }

                    override fun getLogGroupName(): String {
                        TODO("Not yet implemented")
                    }

                    override fun getFunctionVersion(): String {
                        TODO("Not yet implemented")
                    }

                    override fun getIdentity(): CognitoIdentity {
                        TODO("Not yet implemented")
                    }
                }
        )
    }

}