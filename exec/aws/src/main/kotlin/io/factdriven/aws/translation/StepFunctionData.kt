package io.factdriven.aws.translation

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty


@JsonInclude(JsonInclude.Include.NON_NULL)
data class NodeParameter(@JsonProperty("FunctionName") val functionName: String, @JsonProperty("Payload") val payload: Payload)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Payload(@JsonProperty("TaskToken.$") val taskToken : String = "\$\$.Task.Token", @JsonProperty("id") val id: String, @JsonProperty("Execution.$") val execution : String = "\$\$.Execution.Id", @JsonProperty("History.$") var messages: String? = "$.Messages")
