package io.factdriven.language.execution.aws.lambda

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.factdriven.execution.Message
import java.lang.RuntimeException

@JsonIgnoreProperties(ignoreUnknown = true)
data class FlowLangVariables (@JsonProperty("Messages") val messages : List<Message>)

data class FlowLangVariablesOut (@JsonProperty("Messages") val messages : List<String>)


data class LoopContext @JsonCreator constructor(@JsonProperty("counter") val counter: Int, @JsonProperty("continue") val continueLoop : Boolean){
}

data class InclusiveContext(val conditions: MutableMap<String, Boolean>, val next: Int = 0, val counter: Int){
    constructor() : this(conditions = mutableMapOf(), counter = 0)
}

class FactException(val fact : Any) : RuntimeException()