package io.factdriven.language.execution.aws

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSClient

class SnsService {
    fun createClient() : AmazonSNS {
        return AmazonSNSClient.builder()
                .withRegion("eu-central-1")
                .withCredentials(DefaultAWSCredentialsProviderChain())
                .build()
    }
}