package org.factscript.language.execution.aws.translation.context

import com.amazonaws.services.stepfunctions.builder.StateMachine
import org.factscript.language.execution.aws.translation.StepFunctionBuilder
import org.factscript.language.execution.aws.translation.naming.NamingStrategy
import org.factscript.language.execution.aws.translation.naming.StatefulNamingStrategy
import org.factscript.language.execution.aws.translation.transition.TransitionStrategy

open class TranslationContext private constructor(val lambdaFunction: LambdaFunction,
                                                  val transitionStrategy: TransitionStrategy,
                                                  val stepFunctionBuilder: StepFunctionBuilder<*>,
                                                  val snsContext: SnsContext,
                                                  val namingStrategy: NamingStrategy = StatefulNamingStrategy()) {
    companion object {
        fun of(lambdaFunction: LambdaFunction,
               transitionStrategy: TransitionStrategy,
               stepFunctionBuilder: StepFunctionBuilder<*>,
               snsContext: SnsContext,
               namingStrategy: NamingStrategy = StatefulNamingStrategy()) : TranslationContext {
            return TranslationContext(lambdaFunction, transitionStrategy, stepFunctionBuilder, snsContext, namingStrategy)
        }
    }

    fun copyWith(lambdaFunction: LambdaFunction = this.lambdaFunction,
                 transitionStrategy: TransitionStrategy = this.transitionStrategy,
                 stepFunctionBuilder: StepFunctionBuilder<*> = this.stepFunctionBuilder,
                 snsContext: SnsContext = this.snsContext,
                 namingStrategy: NamingStrategy = this.namingStrategy): TranslationContext {
        return TranslationContext(lambdaFunction, transitionStrategy, stepFunctionBuilder, snsContext, namingStrategy)
    }

}

data class LambdaFunction(val name: String, val resource: String = "arn:aws:states:::lambda:invoke.waitForTaskToken")






data class TranslationResult(val stateMachine: StateMachine, val translationContext: TranslationContext)

