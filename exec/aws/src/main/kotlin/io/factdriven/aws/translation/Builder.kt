package io.factdriven.aws.translation

import com.amazonaws.services.stepfunctions.builder.StateMachine
import com.amazonaws.services.stepfunctions.builder.states.Branch
import com.amazonaws.services.stepfunctions.builder.states.State

interface StepFunctionBuilder<Type> {
    fun startAt (name: String)
    fun state (name: String, stateBuilder: State.Builder)
    fun build() : Type
}

class StateMachineBuilder(private val delegation: StateMachine.Builder) : StepFunctionBuilder<StateMachine> {
    override fun startAt(name: String) {
        delegation.startAt(name)
    }

    override fun state(name: String, stateBuilder: State.Builder) {
        delegation.state(name, stateBuilder)
    }

    override fun build() : StateMachine {
        return delegation.build()
    }
}

class BranchBuilder(private val delegation: Branch.Builder) : StepFunctionBuilder<Branch> {
    override fun startAt(name: String) {
        delegation.startAt(name)
    }

    override fun state(name: String, stateBuilder: State.Builder) {
        delegation.state(name, stateBuilder)
    }

    override fun build() : Branch{
        return delegation.build()
    }
}