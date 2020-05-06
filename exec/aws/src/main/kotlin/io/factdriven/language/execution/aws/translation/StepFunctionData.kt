package io.factdriven.language.execution.aws.translation

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty


@JsonInclude(JsonInclude.Include.NON_NULL)
data class NodeParameter(@JsonProperty("FunctionName") val functionName: String, @JsonProperty("Payload") val payload: StepFunctionPayload)

interface StepFunctionPayload{
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Payload(@JsonProperty("TaskToken.$") val taskToken : String = "\$\$.Task.Token", @JsonProperty("id") val id: String, @JsonProperty("Execution.$") val execution : String = "\$\$.Execution.Id", @JsonProperty("History.$") var messages: String? = "$.Messages") : StepFunctionPayload
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ParallelMergePayload(@JsonProperty("TaskToken.$") val taskToken : String = "\$\$.Task.Token", @JsonProperty("Execution.$") val execution : String = "\$\$.Execution.Id", @JsonProperty("MergeList.$") var messages: String? = "$[*]") : StepFunctionPayload

@JsonInclude(JsonInclude.Include.NON_NULL)
data class InclusivePayload(@JsonProperty("TaskToken.$") val taskToken : String = "\$\$.Task.Token", @JsonProperty("id") val id: String, @JsonProperty("History.$") var messages: String? = "$.Messages", @JsonProperty("Execution.$") val execution : String = "\$\$.Execution.Id", @JsonProperty("InclusiveContext.$") var inclusiveContext: String? = "$.InclusiveContext") : StepFunctionPayload

