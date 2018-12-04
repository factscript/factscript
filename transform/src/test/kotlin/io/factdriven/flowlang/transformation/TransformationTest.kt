package io.factdriven.flowlang.transformation

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
            on message type(RetrievePayment::class) create acceptance()
            execute service {
                create intent("ChargeCreditCard") by { ChargeCreditCard() }
                on message type(CreditCardCharged::class) create success()
            }
            create success("PaymentRetrieved") by { PaymentRetrieved() }
        }
        val modelInstance = transform(flow)
        Bpmn.validateModel(modelInstance);
        val file = File.createTempFile("./bpmn-model-api-", ".bpmn")
        Bpmn.writeModelToFile(file, modelInstance)
    }

}