package org.factscript.language.execution.aws.service

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.lambda.AWSLambda
import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.amazonaws.services.lambda.model.*
import org.factscript.language.execution.aws.lambda.hashString
import java.lang.Exception
import java.security.MessageDigest
import java.util.*


class LambdaService {
    private fun createClient() : AWSLambda {
        return AWSLambdaClientBuilder.standard()
                .withRegion("eu-central-1")
                .withCredentials(DefaultAWSCredentialsProviderChain())
//                .withClientConfiguration(ClientConfiguration().tls)
                .withClientConfiguration(ClientConfiguration()
                        .withConnectionTimeout(3000)
                        .withRequestTimeout(10000)
                        .withSocketTimeout(10000)
                        .withClientExecutionTimeout(10000))
                .build()
    }

    fun updateTriggers(lambdaArn: String, topicArns: List<String>) {
        val client = createClient()

        println("Updating $topicArns")
        for (topicArn in topicArns) {

            val function = client.getFunction(GetFunctionRequest().withFunctionName("PaymentRetrieval"))
            println(function)
            println("Updating $topicArn")

            try {
                client.addPermission(AddPermissionRequest()
                        .withFunctionName(lambdaArn)
                        .withSourceArn(topicArn)
                        .withPrincipal("sns.amazonaws.com")
                        .withStatementId(hashString(topicArn))
                        .withAction("lambda:InvokeFunction")
                )
            } catch (e: ResourceConflictException){
                // ignore
            }
        }

    }


}