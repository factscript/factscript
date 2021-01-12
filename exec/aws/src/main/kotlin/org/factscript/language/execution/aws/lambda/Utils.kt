package org.factscript.language.execution.aws.lambda

import java.lang.RuntimeException
import java.security.MessageDigest

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

fun hashString(value : String) : String{
    val bytes = value.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("", { str, it -> str + "%02x".format(it) })
}
