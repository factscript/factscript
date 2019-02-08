package io.factdriven.flow.lang


/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
enum class ActionClassifier {

    Intention,
    Progress,
    Success,
    Failure,
    Fix

}

interface ClassifiedAction<ENTITY: Entity, FACT: Fact> {

    infix fun by(message: ENTITY.() -> FACT?)

}

interface Action<ENTITY: Entity> {

    infix fun intention(type: FactName)
    infix fun progress(type: FactName)
    infix fun success(type: FactName)
    infix fun failure(type: FactName)

    infix fun <FACT: Fact> intention(type: FactType<FACT>): ClassifiedAction<ENTITY, FACT>
    infix fun <FACT: Fact> progress(type: FactType<FACT>): ClassifiedAction<ENTITY, FACT>
    infix fun <FACT: Fact> success(type: FactType<FACT>): ClassifiedAction<ENTITY, FACT>
    infix fun <FACT: Fact> failure(type: FactType<FACT>): ClassifiedAction<ENTITY, FACT>

    fun asDefinition(): DefinedAction {
        return this as DefinedAction
    }

}

open class ActionImpl<ENTITY: Entity, FACT: Fact> :
    Action<ENTITY>,
    ClassifiedAction<ENTITY, FACT>,
    DefinedAction
{

    internal constructor(parent: DefinedFlow<*>) {
        this.parent = parent
    }

    override val parent: DefinedFlow<*>
    override var factType: FactType<*>? = null
        set(value) { field = value?.let { FactTypes.add(it); it } }
    override lateinit var name: FactName
    override lateinit var classifier: ActionClassifier
    override var function: (Entity.() -> Fact)? = null

    // Flow Action Factories

    override infix fun intention(name: NodeName) {
        this.classifier = ActionClassifier.Intention
        this.name = name
    }

    @SuppressWarnings("unchecked")
    override infix fun <FACT: Fact> intention(type: FactType<FACT>): ClassifiedAction<ENTITY, FACT> {
        this.factType = type
        intention(type.simpleName!!)
        return this as ClassifiedAction<ENTITY, FACT>
    }

    override infix fun progress(name: String) {
        this.classifier = ActionClassifier.Progress
        this.name = name
    }

    override infix fun <O: Fact> progress(type: FactType<O>): ClassifiedAction<ENTITY, O> {
        this.factType = type
        progress(type.simpleName!!)
        return this as ClassifiedAction<ENTITY, O>
    }

    override infix fun success(name: String) {
        this.classifier = ActionClassifier.Success
        this.name = name
    }

    override infix fun <O: Fact> success(type: FactType<O>): ClassifiedAction<ENTITY, O> {
        this.factType = type
        success(type.simpleName!!)
        return this as ClassifiedAction<ENTITY, O>
    }

    override infix fun failure(name: String) {
        this.classifier = ActionClassifier.Failure
        this.name = name
    }

    override infix fun <O: Fact> failure(type: FactType<O>): ClassifiedAction<ENTITY, O> {
        this.factType = type
        failure(type.simpleName!!)
        return this as ClassifiedAction<ENTITY, O>
    }

    override fun by(message: ENTITY.() -> FACT?) {
        @Suppress("UNCHECKED_CAST")
        this.function = message as Entity.() -> Fact
    }

}
