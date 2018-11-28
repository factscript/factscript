package io.factdriven.flowlang.transformation

import io.factdriven.flowlang.execute
import org.junit.Test
import org.camunda.bpm.model.bpmn.Bpmn
import java.io.File


/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class TransformationTest {

    @Test
    fun test() {
        val definition = execute<PaymentRetrieval> {}
        val modelInstance = transform(definition)
        Bpmn.validateModel(modelInstance);
        val file = File.createTempFile("./bpmn-model-api-", ".bpmn")
        Bpmn.writeModelToFile(file, modelInstance)
    }

}