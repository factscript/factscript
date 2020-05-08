###### Varianten der Korrelation events bzw facts

    on event CreditCardExpired::class having "account" match { account }

vs.

    on event CreditCardExpired::class having {
        "account" match { account }
    }

vs.

    on event CreditCardExpired::class having {
        "account" match { account }
        "number" match { number }
    }

vs.
    
    on event CreditCardExpired::class having CreditCardExpired::account match { account }

vs.
    
    on event having CreditCardExpired::account match { account }

vs.

    on event having {
        CreditCardExpired::account match { account }
    }

vs.

    on event having {
        CreditCardExpired::account match { account }
        CreditCardExpired::number match { number }
    }

###### Kopfgesteuerte Loops

    loop {
        select("Payment fully covered?") either {
            given ("No") condition { covered == total }
            execute command ChargeCreditCard::class by { ChargeCreditCard(paymentId, total - covered) }
        } or {
            given ("Yes")
        }
        until ("Payment fully covered?") condition { covered == total }
    }
    
vs.

    loop {
        until ("Payment fully covered?") condition { covered == total }
        execute command ChargeCreditCard::class by { ChargeCreditCard(paymentId, total - covered) }
    }
    
Umsetzung von (fuß- und kopfgesteuerten) Loops hat Vor- und Nachteile gegenüber dem Einsatz von XOR Gateways. Kopfgesteuerte Loops sind mit Schleifenmarkierer einfach und sauber umsetzbar, wobei für Camunda zu klären wäre, ob die überhaupt unterstützt werden. Der visuelle Clutter wird durch den Einsatz von Teilprozessen allerdings für viele Leser subjektiv eher grösser (zusätzlicher Rahmen, zusätzliche Start- und Endereignisse). Eine Kopfgesteuerte Loop liesse sich alternativ auch mit zwei doppelt verbundenen XOR Gateways Richtung BPMN umsetzen. Man könnte überlegen, einen der Pfade zu verstecken, oder beide übereinanderzulegen. Nachteil "ein Hack", Vorteil: kompakt, gut lesbar.
   
###### Promise Section als reine Möglichkeit eine API zu definieren

    on command RetrievePayment::class promise {
        report success PaymentRetrieved::class
    }
    
    on time cycle ("Alle 14 Tage") { "P14D" } start { nikolo } promise {
        report success PaymentRetrieved::class
    }
        
    report success PaymentRetrieved::class by {}
//    report progress PaymentRetrieved::class by {}
    report failure PaymentRetrieved::class by {}
    
als API vs. folgendem Vorgehen bei expliziter Definition des Flows

    on command RetrievePayment::class promise {
        report success PaymentRetrieved::class
        report success PaymentRetrieved::class
//        report status PaymentPartlyCovered::class
        report failure PaymentRetrieved::class
        report failure PaymentRetrieved::class
    }

    execute command WithdrawAmount::class by { WithdrawAmount(name = accountId, amount = total) }

    loop {
        select("Payment fully covered?") either {
            given ("No") condition { covered == total }
            execute command ChargeCreditCard::class by {
                ChargeCreditCard(
                    reference = paymentId,
                    charge = total - covered
                )
            }
        } or {
            given ("Yes")
        }
        until ("Payment fully covered?") condition { covered == total }
    }
    
    select either {
        
    } or {
        report success PaymentRetrieved::class by { PaymentRetrieved(paymentId = paymentId, payment = total) }
    }
    
    report success | failure
    emit event PaymentPartlyCovered::class        
    
"promise" ist für "on command" (und künftig auch "on query") sinnvoll, nicht aber für "on time", "on condition" etc. Diese Scripts laufen aus "eigener Verantwortung", nicht "beauftragt", können aber ebenfalls erfolgreich oder nicht erfolgreich laufen.
    
###### "on fact having/match" anstelle von "on event having/match"

Event ist ein in den Software Communities leider extrem stark eingeengter und vorbesetzter Begriff, der zum Verständnis dessen wie "fact-driven" scripting mE funktionieren sollte und was mit "on fact" daher möglich sein sollte (nämlich prinzipiell auf "alle" Arten von Nachrichten zu reagieren (also neben klassischen "event" notifications auch command, query, statement/report), etwa um Querschnittsaspekte wie das Ausschicken von Mails, Business Monitotring etc abzudecken.

###### "on failure" (ohne "having/match") zur Reaktion auf Fehler in "but" sections

###### Möglichkeit zu "execute/issue" command "emit event" ohne Angabe der Class prüfen

    execute command WithdrawAmount::class by { WithdrawAmount(accountId, total) }

vs.

    execute command { WithdrawAmount(accountId, total) }

Das technische Problem ist hier, dass die Klasse auslesbar sein muss **ohne** die Instanz erzeugen zu müssen, etwa zur Generierung des Labels. Es ist denkbar, dass das mit inline functions realisierbar ist, aber  nicht ganz trivial, weil diese auf interfaces nicht direkt erlaubt sind.

##### Möglichkeit "by" wegzulassen

    execute command WithdrawAmount::class by { WithdrawAmount(accountId, total) }

vs.

    execute command WithdrawAmount::class { WithdrawAmount(accountId, total) }

vs.

    execute command { WithdrawAmount(accountId, total) }
    
Zweite Variante dürfte aus technischen Gründen nur mit Klammer um "WithdrawAmount::class" umsetzbar sein, weil der Compiler die Zusammenhänge sonst nicht mehr interpretieren kann. Das würde die bisherige Konsistenz etwas mehr brechen als "by" zu verwenden ... bei der dritten Variante (so möglich), würde sich das Problem allerdings gar nicht mehr stellen.

##### Möglichkeit mehrere Flows für eine Entität anzulegen

Hauptflow ist jener, dessen "on fact" eine neue Instanz erzeugt. Alternativ ist es ein on time oder ein condition flow, wenn der Constructor der Entität default ist. Alle anderen Flows wären dann in BPMN Ereignisteilprozesse. Wie entscheiden welcher Flow die Instanz erzeugt, wenn es mehrere nicht "on fact" flows gibt?

##### Termination durch Error End Event

In der BPMN ist es so, dass ein aufgerufener Flow (Call Activity) der einen BPMN Error reportet genaugenommen erst durch den Aufrufer, der einen Boundary Event definiert terminiert wird. Wenn das nun aber nicht der Fall ist, weil ein solcher nicht definiert ist, hat der End Event nicht die Semantik eines Termination End Events. Ich denke, dass die als "failure" definierten End Events der Flow Language aber eine solche Semantik haben sollten, weil überhaupt nicht gesagt werden kann in welchem Ast einer parallelen Ausführung der Fehler geworfen wird ... in der Übersetzung nach BPMN gibt es die Möglichkeit das BPMN Thema zu ignorieren und Fehler Events zu zeigen, die Instanz aber nach Erreichen eines Fehler Events technisch abzubrechen, weil es technisch keine Oberinstanz gibt, die das tun würde. Eine andere Möglichkeit wäre bereits in der Flow Language nur dann zu "terminieren", wenn man es explizit so haben möchte ...'report termination'  als Sonderfall von "report failure" (Gefühl: eher nicht gut). Dem steht entgegen, dass ich glaub ich eher nicht "explizit" Compensation auslösen will, sondern der Failure End Event führt immer zu einer Compensation des Scopes, wenn eben kompensierbare Aktivitäten in diesem definiert sind. Wenn der Error nicht nachvollziehbar im eigenen Flow gefangen wird muss daher alles kompensiert und der Flow danach abgebrochen werden, weil die "Oberinstanz" das ja gar nicht explizit tun kann - und vermutlich auch nicht explizit tun sollte ... in der Übersetzung nach Camunda BPMN könnte man nun so vorgehen, dass alle Error End Events die in BPMNs gefangen werden als Error End Event gezeigt werden. Wohingegen solche, die nicht im Deployment gefangen werden als Termination End Event gezeigt werden aber dieselbe fehlerwerfende Folge im Sinn eines "emit failure" aufweisen. Ja, das funktioniert wohl so.

##### Iterate over collection

    process ("") collection { receipts } sequentially {        
    } but {} and {}
    
    process ("") collection { receipts } concurrently {
    } but {} and {}
    
##### Sub process

    process ("") item {} using {
    } but {} and {}

    process ("") item {} using {
    } but {} and {}

##### Parallel Gateway
    
    process ("") all {
    } and {}
    
##### Process command?
    
    execute command {
    }
    
    execute query {
    }

vs.
    
    process command
    process query

##### Repeat until außen oder innen
    
    repeat loop {
    } until ("") condition {}
    
    repeat until ("") condition {} loop {
    }

    repeat given ("") condition {} loop {
    }

    repeat {
    } given ("") condition {}

##### Keyword "condition" uu weglassen?

    given condition
    until condition
    
##### Default Path

    select either {
        given ("") condition {}
    } or {
        otherwise ("")
    }
    
##### report success/failure im flow

##### api section optional or always ... könnte man konfigurieren, für optional spricht weiters, dass es eine Alternative Definition über Annotations geben wird müssen.

##### by weglassen ?

##### query

    issue query
    on query

##### on command/query having match

Thema der Instanzen die bereits laufen, wenn commands/queries reinkommen
Wirft Frage mehrerer Skripts für einen Participant auf.
Und die Frage welches den Lebenszyklus steuert.

##### on failure
- statt on event in einem but flow
