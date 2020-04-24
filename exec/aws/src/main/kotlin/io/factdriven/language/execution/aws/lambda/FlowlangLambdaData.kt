package io.factdriven.language.execution.aws.lambda

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.factdriven.execution.Message

@JsonIgnoreProperties(ignoreUnknown = true)
data class FlowLangVariables (@JsonProperty("Messages") val messages : List<Message>)

data class FlowLangVariablesOut (@JsonProperty("Messages") val messages : List<String>)