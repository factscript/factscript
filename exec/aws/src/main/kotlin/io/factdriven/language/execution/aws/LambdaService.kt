package io.factdriven.language.execution.aws

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.lambda.AWSLambda
import com.amazonaws.services.lambda.AWSLambdaClient
import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.amazonaws.services.lambda.model.*
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.CreateTopicRequest
import java.util.*


class LambdaService {
    private fun createClient() : AWSLambda {
        return AWSLambdaClientBuilder.standard()
                .withRegion("eu-central-1")
                .withCredentials(DefaultAWSCredentialsProviderChain())
                .build()
    }

    fun updateTriggers(lambdaArn: String, topicArns: List<String>) {
        val client = createClient()

        for (topicArn in topicArns) {
            client.addPermission(AddPermissionRequest()
                    .withFunctionName(lambdaArn)
                    .withSourceArn(topicArn)
                    .withPrincipal("sns.amazonaws.com")
                    .withStatementId("Update-${UUID.randomUUID().toString().replace("-", "_")}")
                    .withAction("lambda:InvokeFunction")
            )
        }

    }
}