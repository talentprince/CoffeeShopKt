import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlin.system.measureTimeMillis

// The problem with CoffeeShopTwo was that both baristas processed the same set of orders.
// This introduces a way for the baristas and cashier to talk to each other so that the orders
// are only processed once. We do this using channels.
fun main(args: Array<String>) = runBlocking {
    val orders = listOf(Menu.Cappuccino(CoffeeBean.Regular, Milk.Whole),
        Menu.Cappuccino(CoffeeBean.Premium, Milk.Breve),
        Menu.Cappuccino(CoffeeBean.Regular, Milk.NonFat),
        Menu.Cappuccino(CoffeeBean.Decaf, Milk.Whole),
        Menu.Cappuccino(CoffeeBean.Regular, Milk.NonFat),
        Menu.Cappuccino(CoffeeBean.Decaf, Milk.NonFat))
    log(orders)

    // introduce the channel used for the cashier and the baristas to communicate with each other
    val ordersChannel = Channel<Menu>()

    // cashier produces orders and sends them to the order channel
    // if there's no barista available to accept the order, then the cashier enters a suspended state
    // so we use a coroutine here
    launch(CoroutineName("cashier")) {
        for (o in orders) { ordersChannel.send(o) }
        // important that we close this channel when we're done sending all the orders
        // otherwise the two baristas will stick around indefinitely waiting for something new to arrive
        // on the channel
        ordersChannel.close()
    }
    // NOTE: You can improve the above using 'produce'. We'll see this in CoffeeShopFour

    val t = measureTimeMillis {
        coroutineScope {
            launch(CoroutineName("barista-1")) { makeCoffee(ordersChannel) }
            launch(CoroutineName("barista-2")) { makeCoffee(ordersChannel) }
        }
    }
    println("Execution time: $t ms")
}

private suspend fun makeCoffee(ordersChannel: ReceiveChannel<Menu>) {
    for (o in ordersChannel) {
        log("Processing order: $o")
        when (o) {
            is Menu.Cappuccino -> {
                val groundBeans = grindCoffeeBeans(o.beans())
                val espressoShot = pullEspressoShot(groundBeans)
                val steamedMilk = steamMilk(o.milk())
                val cappuccino = makeCappuccino(o, espressoShot, steamedMilk)
                log("Serve: $cappuccino")
            }
        }
    }
}

private suspend fun grindCoffeeBeans(beans: CoffeeBean): CoffeeBean.GroundBeans {
    log("Grinding beans")
    delay(30)
    return CoffeeBean.GroundBeans(beans)
}

private suspend fun pullEspressoShot(groundBeans: CoffeeBean.GroundBeans): Espresso {
    log("Pulling espresso shot")
    delay(20)
    return Espresso(groundBeans)
}

private suspend fun steamMilk(milk: Milk): Milk.SteamedMilk {
    log("Steaming milk")
    delay(10)
    return Milk.SteamedMilk(milk)
}

private suspend fun makeCappuccino(order: Menu.Cappuccino, espressoShot: Espresso, milk: Milk.SteamedMilk): Beverage.Cappuccino {
    log("Combining ingredients")
    delay(5)
    return Beverage.Cappuccino(order, espressoShot, milk)
}
