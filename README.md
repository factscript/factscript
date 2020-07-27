# lang
[![Build Status](https://travis-ci.com/factdriven/lang.svg?branch=master)](https://travis-ci.com/factdriven/lang)

# Getting started

This tutorial will walk you through creating a very simple process definition in Factscript and deploying and executing it in Camunda.

First, clone this repository and open it in an IDE such as IntelliJ IDEA.

Create a new module as a child module of *factdriven*.

In the new module, you first have to modify the *build.gradle* file. You can use the [*build.gradle*](https://github.com/factdriven/lang/blob/master/demo/cam/build.gradle) file of the existing *cam-demo* module as a template (don't forget to remove *test* )

## Scripting the process

We will be scripting a very simple credit card charging process. You can also find instructions of more complex processes in the Advanced processes section below.

Create a package inside *main/kotlin* where you will put your process definitions and other stuff.

Create a Kotlin file with for your process (use a separate file for each process definition). 

To be able to use Factscript for your process, you need to import the language definition:
```kotlin
import io.factdriven.language.*
```
Create a *data class* for your process. First, define all variables that the process will use. We define these 4 variables, you will see their usage in the process definition later on.

```kotlin
data class CreditCard (

        val reference: String,
        val charge: Float,
        var confirmed: Boolean = false,
        var successful: Boolean = false

){ 
```

Then, define a constructor for the data class and functions that will update your variables upon receiving some messages.
```kotlin
constructor(fact: ChargeCreditCard): this(fact.reference, fact.charge)

    fun apply(fact: CreditCardProcessed) {
        confirmed = fact.valid
    }

    fun apply(fact: CreditCardCharged) {
        successful = true
    }
```

Now, in a companion object ...
```kotlin
companion object {
    init {
```
... you are ready to script your first process in Factscript!
```kotlin
flow <CreditCard> {
```

Every process definition in Factscript starts with a message type that starts the process and an optional promise of how the process can finish. This is information is particularly important if the process is not started manually but called by another process.
```kotlin
 on command ChargeCreditCard::class emit {
    success event CreditCardCharged::class
    failure event CreditCardExpired::class
}
```

We may also want to receive messages at some point during process execution. To do it, we use the *await* construct.
```kotlin
 await event CreditCardProcessed::class having "reference" match { reference }
```
Note that we use the *match* construct to match the incoming message to the correct instance of the process: only the message of type *CreditCardProcessed* with *reference* field having the same content as the *reference* variable will be catched by this statement and let the process continue.

Conditional execution can be expressed with the *select* construct. We add the following code to our process:
```kotlin
 select either {
    given ("Yes") condition { confirmed }
} or {
    otherwise ("No")
    emit failure event { CreditCardExpired(reference, charge)}
}
```
If the variable *confirmed* evaluates to **true**, the first branch with label "Yes" will be chosen. Otherwise, i.e. if no condition is true, the last branch with a label "No" will be executed, and in this case the processs will emit a *failure event* of type *CreditCardExpired*. Note that we declared the possibility for our process to end with this failure event in the promise section in the very beginning, so the calling process can be also prepared to this.

Finally, if the process runs smoothly, we want to report success to the caller. We do so by emitting a *success event*. Also this success event was defined in the promise, so the calling process will know that our process executed correctly by receiving this message.
```kotlin
 emit success event { CreditCardCharged(reference, charge) }
```

We are finished with the definition of our process now, the last thing that we have to do in this file is to define the message types that we used in our process. In our case, there are: *ChargeCreditCard*, *CreditCardProcessed*, *CreditCardExpired* and *CreditCardCharged*.
```kotlin
data class ChargeCreditCard(val reference: String, val charge: Float)
data class CreditCardProcessed(val reference: String, val valid: Boolean)
data class CreditCardExpired(val reference: String, val charge: Float)
data class CreditCardCharged(val reference: String, val charge: Float)
```

## Application

Next, you will need to create an application file. Feel free to use the [PaymentRetreivalApplication.kt](https://github.com/factdriven/lang/blob/master/demo/cam/src/main/kotlin/io/factdriven/language/execution/cam/PaymentRetrievalApplication.kt) as a template. Make sure to include the following line in your file:
```kotlin
import io.factdriven.language.execution.cam.*
```
Also update the class name and put it into `runApplication` statement of the `main` function.

Most importantly, put the classes of the processes that you defined into `Flows.activate` call:
```kotlin
Flows.activate(
    CreditCard::class
)
```
In case you have defined multiple processes, separate them by a comma.

## Controller

You also have to create a controller, which will allow external applications to communicate with your process via HTTP requests. As before, you can use the [PaymentRetreivalController](https://github.com/factdriven/lang/blob/master/demo/cam/src/main/kotlin/io/factdriven/language/execution/cam/PaymentRetrievalController.kt) from the *cam-demo* package as a template. For the example process you will need to define two paths:
```kotlin
@RequestMapping("/charge", method = [RequestMethod.POST])
    fun chargeCreditCard(@RequestParam reference: String, @RequestParam charge: Float) {
        send(ChargeCreditCard::class, ChargeCreditCard(reference, charge))
}

@RequestMapping("/creditCardProcessed", method = [RequestMethod.POST])
    fun creditCardProcessed(@RequestParam orderId: String, @RequestParam valid: Boolean = true) {
        send(CreditCard::class, CreditCardProcessed(orderId, valid))
}
```

## Camunda properties

As the last step towards your executable process, you will need to add a Camunda configuration file called *application.properties* to *main/resources*. You can also use [this file](https://github.com/factdriven/lang/blob/master/demo/cam/src/main/resources/application.properties) as a template.

## Execution

You are done! You can *run* your application, and a Camunda server will be deployed at `localhost:8080`. Log into the *Cockpit* to see the process definition in BPMN and monitor running instances of the process.

Don't have running instances yet? Right! Because you did not start any! To do so, you need to send a message of *ChargeCreditCard* type (remember the first statement in your process definition?), and you can do it by semding a POST request to [localhost:8080/charge](localhost:8080/charge) via [Postman](https://www.postman.com/) or [curl](https://curl.haxx.se/) (don't forget to specify the parameters!).

# Advanced processes

Now that you got your first process up and runnung, you may want to script more advanced processes that actually make sense in a production environment. And of course Factscript allows you to do it! (Otherwise, why would we create it?) So, in this section, we will show you two more advanced processes that use all currently available cool features of Factscript. 

## Payment process

This process can be found in the [Payment.kt](https://github.com/factdriven/lang/blob/master/demo/cam/src/main/kotlin/io/factdriven/language/execution/cam/Payment.kt) file, so we will skip some secondary things like defining variables and message types and focus more on the process itself.

So, create a new *data class* Payment and define the variables, constructor and co. Afterwards, in the *companion object* you can define the ```flow <Payment>```. 

As we said, **every** process in Factscript starts with the so called API definition, i.e. defining the message type that starts a process and all message types with which it can respond.
```kotlin
on command RetrievePayment::class emit {
    success event PaymentRetrieved::class
    failure event PaymentFailed::class
}
```
And this is where the cool stuff begins! Now, you can call another process using the ```execute command``` statement!
```kotlin
 execute command {
    WithdrawAmountFromCustomerAccount(customer = accountId, withdraw = total)
}
```
As you can see, we can execute another process by just sending the respective message to it and we can use our variables defined for the process as parameters. This command will wait for the *successful* execution of the called process and let our process continue afterwards. Note that the process to be called also has to be defined in Factscript. We do not show the definition of the account withdrawal process in our tutorial, so feel free to script it by yourself or use the [this](https://github.com/factdriven/lang/blob/master/demo/cam/src/main/kotlin/io/factdriven/language/execution/cam/Account.kt) mock-up process from our demo.

However, something may go wrong during the execution of called process or even afterwards. For these cases Factscript offers the ```but``` statement--a very powerful exception handling tool. It allows you to catch failure messages from the called process, set timeouts and much more. You will see many possible usages for it in this tutorial.
Right now we will use the ```but``` statement to return money back to the customer account in case the payment process breaks later on. It is quite important as our customer won't be very happy if the order is not fulfilled but the money is still withdrawn :-)
So, our process call will look as follows:
```kotlin
execute command {
    WithdrawAmountFromCustomerAccount(customer = accountId, withdraw = total)
} but {
    on failure PaymentFailed::class
    execute command {
        CreditAmountToCustomerAccount(customer = accountId, credit = covered)
    }
}
```
Here, in case our payment process fails, we return the withdrawn amont back to the customer's account.

Although we have tried to withdraw the **total** amount from the customer's account, it may happen that the customer does not have enough money. So, we have to check the amount **covered** and in case it is smaller than the **total** amount, charge it from the customer's credit card.
```kotlin
select ("Payment fully covered?") either {
    given ("No") condition { covered < total }
    execute command {
        ChargeCreditCard(orderId, total - covered)
    }
} or {
    otherwise ("Yes")
    emit success event { PaymentRetrieved(orderId) }
}
```
So, if the mayment is fully covered (the *otherwise* branch), the corresponding *event* is sent right away and process finithes successfully. What is more interesing for us, is the "No" branch. In this case the *ChargeCreditCard* message is sent. Sounds familiar, doesn't it? Exactly! This message starts the credit card charging process that we defined in the very beginning of this tutorial! And as you may remember, it does not necessarily finish successfully, so we have to adapt our definition above and add some proper exception handling with the ```but``` statement.
```kotlin
...
    given ("No") condition { covered < total }
    execute command {
        ChargeCreditCard(orderId, total - covered)
        } but {
             on failure CreditCardExpired::class
            await first {
                on event CreditCardDetailsUpdated::class having "accountId" match { accountId }
                // repeat the procedure...
            } or {
                on time duration ("Two weeks") { "PT14D" }
                emit failure event { PaymentFailed(orderId) }
            }
        }
    }
...
```
Here, in case the credit card process fails and the *CreditCardExpired* message is caught, we await an update of the credit card but we don't want to wait for it forever. Instead, we set a two weeks timeout, and if the update does not come within before this timeout, we emit a failure event and the process execution stops. Note that in this case the amount previously withdrawn from the customer's account (if any) will be returned.
If the credit card is updated, we want to charge it again. However, this process may again fail, so we again have to wait for an update and so on. How do we script this behavior? The answer is the ```repeat until``` statement that allows us, as you would expect, to repeat some code until some condition is met. So, we surround our execution by this statement and repeat it until the payment is fully covered:
```kotlin
...
given ("No") condition { covered < total }
repeat {
    execute command {
        ChargeCreditCard(orderId, total - covered)
    } but {
        on failure CreditCardExpired::class
        await first {
            on event CreditCardDetailsUpdated::class having "accountId" match { accountId }
        } or {
            on time duration ("Two weeks") { "PT5M" }
            emit failure event { PaymentFailed(orderId) }
        }
    }
     until ("Payment fully covered?") condition { covered == total }
}
...
```
As you see, this loop will be exited if and only if the **covered** amount is equal to the **total** amount. And in this case, we can finish our process by emitting a success enebt:
```kotlin
emit success event { PaymentRetrieved(orderId) }
```
Now, define the messages used in this process as *data classes*. Note that as you already have defined the messages related to the credit card charging process in this package, you don't need to define them again, so only define the new, previously unmentioned message types.

Now, you have successfully defined another process in Factscript. Congratulations! In order to deploy it in Camunda, open your application file and add this new process to the ```Flows.activate``` call:
```kotlin
// ExampleApplication.kt
...
Flows.activate(
    Payment::class, CreditCard::class
)
...
```
You also have to update the controller to be able send a message about credit card update from external systems, as we do in our example:
```kotlin
//ExampleController.kt
...
@RequestMapping("/creditCardDetailsUpdated", method = [RequestMethod.POST])
fun creditCardDetailsUpdated(@RequestParam accountId: String) {
    send(Payment::class, CreditCardDetailsUpdated(accountId))
}
...
```
You can also define a function to send a *RetrievePayment* message to start the process manually. We will not do it as we actually call this process from another one, the order fulfillment process, that we show next.

## Order fulfillment process

The last process we will define in this tutorial is the order fulfillment process. You can find the complete code of this process [here](https://github.com/factdriven/lang/blob/master/demo/cam/src/main/kotlin/io/factdriven/language/execution/cam/Fulfillment.kt). 
As you will see, this process is simpler than the previous one, still it demonstrates a couple of important language features that will be very useful for your processes.

Again, we skip the definition of the constructor, variables and functions. We will also skip the definition of the message types.

As usual, we start by defining the flow and the API of the process.
```kotlin
flow <Fulfillment> {

    on command FulfillOrder::class emit {
        success event OrderFulfilled::class
        failure event OrderNotFulfilled::class
    }
```
Then, we can emit an event stating that we have started the order fulfillment. This is neither a success nor a failure event, so it shouldn't be defined in the API. Instead, it is just an intermediary event that can be observed by the BPMS and probably other processes.
```kotlin
emit event { OrderFulfillmentStarted(orderId, accountId, total) }
```
Next, we want to fetch the goods from the inventory and simultaneously retreive the payment. As these two steps are independent from each other, we do not care much about in the order in which they are executed. What we do care about is that they both have to finish successfully before we continue. As you may know, this is called parallel execution, and in the Factscript it is achieved with the ```execute all``` statement.
```kotlin
execute all {
    execute command {
        FetchGoodsFromInventory (orderId)
    } but {
        on failure OrderNotFulfilled::class
        execute command { ReturnGoodsToInventory(orderId) }
    }
} and {
    execute command {
        RetrievePayment(orderId, accountId, total)
    } but {
        on failure PaymentFailed::class
        emit failure event { OrderNotFulfilled(orderId) }
    }
}
```
As you can see, ```execute all``` executes all the subflows separated by the ```and``` statement. You can also see that inside this statement you can have your usual exception handling: if the payment fails, the whole process execution stops and the *OrderNotFulfilled* message is sent. And when this message is sent, one of the things that happens will be returning the goods to the inventory.

Afterwards, the process continues in a simple sequence finishing with a *success event*:
```kotlin
emit event { OrderReadyToShip(orderId) }
execute command { ShipGoods(orderId) }
emit success event { OrderFulfilled(orderId) }
```
So, by now you have already scripted three processes in Factscript!

Don't forget to add the newly defined process to your application and update the controller to start this process externally.
