package io.factdriven.execution.camunda.model

import io.factdriven.definition.Flow
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.camunda.bpm.model.bpmn.instance.*
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnDiagram
import org.camunda.bpm.model.bpmn.instance.bpmndi.BpmnPlane
import java.io.File

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
class BpmnModel(node: Flow): Element<Flow, BpmnModelInstance>(node) {

    override val model: BpmnModelInstance = Bpmn.createEmptyModel()

    override val children: List<Element<*,*>> = listOf(Sequence(node, this))

    override val position: Position = Position(160, 92) - margin
    override val dimension: Dimension get() = children.first().dimension
    override fun entry(from: Direction): Position = children.first().entry(from)

    override fun position(child: Element<*,*>): Position = position

    internal val bpmnDefinitions: Definitions = model.newInstance(Definitions::class.java)
    internal val bpmnProcess: Process = model.newInstance(Process::class.java)
    internal val bpmnPlane: BpmnPlane = model.newInstance(BpmnPlane::class.java)
    internal val bpmnDiagram: BpmnDiagram = model.newInstance(BpmnDiagram::class.java)

    private var isInitialized: Boolean = false

    override fun toExecutable(): BpmnModelInstance {
        if (!isInitialized) {
            super.toExecutable();
            isInitialized = true
        }
        Bpmn.validateModel(model);
        return model
    }

    fun toTempFile(openInModeler: Boolean = false): File {
        val file = File.createTempFile("./bpmn-model-api-", ".bpmn")
        Bpmn.writeModelToFile(file, toExecutable())
        if (openInModeler && "Mac OS X" == System.getProperty("os.name")) Runtime.getRuntime().exec("open " + file.absoluteFile)
        return file
    }

    override fun init() {

        with(bpmnDefinitions) {
            targetNamespace = "https://factdriven.io/flowlang"
            model.definitions = this
        }

        with(bpmnProcess) {
            setAttributeValue("id", node.id, true)
            setAttributeValue("name", node.label, false)
            isExecutable = true
            bpmnDefinitions.addChildElement(this)
        }

        with(bpmnPlane) {
            bpmnElement = bpmnProcess
        }

        with(bpmnDiagram) {
            bpmnPlane = this@BpmnModel.bpmnPlane
            bpmnDefinitions.addChildElement(this)
        }

    }

    companion object {

        const val groups = false
        val margin = Dimension(18, 18)

    }

}
