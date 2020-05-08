package io.factdriven.language.execution.aws.lambda

import java.lang.RuntimeException

class EndlessLoopPrevention(){
    companion object {
        fun check (processContext: ProcessContext, loopContext: LoopContext){
            if(loopContext.counter >= processContext.processSettings.maximumLoopCycles){
                throw EndlessLoopPreventionException(processContext, loopContext)
            }
        }
    }
}

class EndlessLoopPreventionException(processContext: ProcessContext, loopContext: LoopContext) :
        RuntimeException("Possible endless loop detected. Loop-Counter is ${loopContext.counter}, threshold is ${processContext.processSettings.maximumLoopCycles}") {

}
