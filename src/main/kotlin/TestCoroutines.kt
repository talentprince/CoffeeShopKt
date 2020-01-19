import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.selects.*

suspend fun selectAorB(a: ReceiveChannel<String>, b: ReceiveChannel<String>): String =
    select<String> {
        a.onReceiveOrNull { value ->
            if (value == null)
                "Channel 'a' is closed"
            else
                "a -> '$value'"
        }
        b.onReceiveOrNull { value ->
            if (value == null)
                "Channel 'b' is closed"
            else
                "b -> '$value'"
        }
    }

fun CoroutineScope.switchMapDeferreds(input: ReceiveChannel<Deferred<String>>) = produce<String> {
    var current = input.receive() // start with first received deferred value
    while (isActive) { // loop while not cancelled/closed
        val next = select<Deferred<String>?> { // return next deferred value from this select or null
            input.onReceiveOrNull { update ->
                update // replaces next value to wait
            }
            current.onAwait { value ->
                send(value) // send value that current deferred has produced
                input.receiveOrNull() // and use the next deferred from the input channel
            }
        }
        if (next == null) {
            println("Channel was closed")
            break // out of loop
        } else {
            current = next
        }
    }
}

fun main() = runBlocking<Unit> {
    //sampleStart
//    val a = produce<String> {
//        repeat(4) { send("Hello $it") }
//    }
//    val b = produce<String> {
//        repeat(4) { send("World $it") }
//    }
//    repeat(8) { // print first eight results
//        println(selectAorB(a, b))
//    }
//    coroutineContext.cancelChildren()
//
//    val chan = Channel<Deferred<String>>()
//    val ss = switchMapDeferreds(chan)
//sampleEnd
    val job = GlobalScope.launch {
        log("haha")
        log("hehe")
        val a = async() {
            delay(1000)
            "haha"
        }.await()
        a
    }
}
