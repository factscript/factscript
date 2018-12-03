package io.factdriven.flowlang.transformation

import io.factdriven.flowlang.FlowDefinition
import io.factdriven.flowlang.execute
import org.camunda.bpm.model.bpmn.Bpmn
import org.junit.jupiter.api.Test
import java.io.File


/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class TransformationTest {

    @Test
    fun test() {
        val flow = execute <PaymentRetrieval> {
            on message type(RetrievePayment::class)
            execute service {
                create intent { ChargeCreditCard() }
                on message type(CreditCardCharged::class)
            }
            create success { PaymentRetrieved() }
        }
        val modelInstance = transform(flow as FlowDefinition<*>)
        Bpmn.validateModel(modelInstance);
        val file = File.createTempFile("./bpmn-model-api-", ".bpmn")
        Bpmn.writeModelToFile(file, modelInstance)
    }

}