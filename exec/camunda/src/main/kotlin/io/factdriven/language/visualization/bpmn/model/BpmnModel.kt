package io.factdriven.language.visualization.bpmn.model

import io.factdriven.language.definition.Flow
import io.factdriven.language.visualization.bpmn.diagram.*
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
    override val diagram: Box = Container()
    internal val paths: MutableList<Path> = mutableListOf()
    override val elements: List<Element<*,*>> = listOf(Sequence(node, this))

    override val east: Symbol<*, *> get() = elements.first().east
    override val west: Symbol<*, *> get() = elements.last().east

    internal val bpmnDefinitions: Definitions = model.newInstance(Definitions::class.java)
    internal val bpmnProcess: Process = model.newInstance(Process::class.java)
    internal val bpmnPlane: BpmnPlane = model.newInstance(BpmnPlane::class.java)
    internal val bpmnDiagram: BpmnDiagram = model.newInstance(BpmnDiagram::class.java)

    private var isInitialized: Boolean = false

    companion object {

        const val groups = true

    }

    override fun initDiagram() {

        Position.Zero = Position(142,74)

    }

    override fun initModel() {

        with(bpmnDefinitions) {
            targetNamespace = "https://factdriven.io/flow-language"
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

    override fun toExecutable(): BpmnModelInstance {

        if (!isInitialized) {
            fun initDiagram(element: Element<*,*>) { element.initDiagram(); element.elements.forEach { initDiagram(it) } }
            initDiagram(this)
            super.toExecutable();
            paths.forEach { it.toExecutable() }
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

}
