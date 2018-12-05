package io.factdriven.flowlang.transformation

import io.factdriven.flowlang.FlowExecution
import io.factdriven.flowlang.execute
import org.camunda.bpm.model.bpmn.Bpmn
import org.junit.jupiter.api.Test
import java.io.File

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class BpmnRenderingTest {

    @Test
    fun test() {
        render(
            execute <PaymentRetrieval> {
                on message type(RetrievePayment::class) create acceptance()
                execute service {
                    create intent("ChargeCreditCard") by { ChargeCreditCard() }
                    on message type(CreditCardCharged::class) create success()
                }
                create success("PaymentRetrieved") by { PaymentRetrieved() }
            }
        )
    }

    fun render(flow: FlowExecution<*>) {
        val container = transform2(flow)
        val bpmnModelInstance = transform(container)
        Bpmn.validateModel(bpmnModelInstance);
        val file = File.createTempFile("./bpmn-model-api-", ".bpmn")
        Bpmn.writeModelToFile(file, bpmnModelInstance)
        if("Mac OS X" == System.getProperty("os.name"))
            Runtime.getRuntime().exec("open " + file.absoluteFile);
    }

}