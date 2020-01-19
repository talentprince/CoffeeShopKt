import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

// convert CoffeeShopOne to process the orders using two coroutines
// the orders are processed twice but they're processed concurrently
fun main(args: Array<String>) = runBlocking {
    val orders = listOf(Menu.Cappuccino(CoffeeBean.Regular, Milk.Whole),
        Menu.Cappuccino(CoffeeBean.Premium, Milk.Breve),
        Menu.Cappuccino(CoffeeBean.Regular, Milk.NonFat),
        Menu.Cappuccino(CoffeeBean.Decaf, Milk.Whole),
        Menu.Cappuccino(CoffeeBean.Regular, Milk.NonFat),
        Menu.Cappuccino(CoffeeBean.Decaf, Milk.NonFat))
    log(orders)
    val t = measureTimeMillis {
        coroutineScope {
            launch(CoroutineName("barista-1")) { makeCoffee(orders) }
            launch(CoroutineName("barista-2")) { makeCoffee(orders) }
        }
    }
    println("Execution time: $t ms")
}

private suspend fun makeCoffee(orders: List<Menu>) {
    for (o in orders) {
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

private fun sleep(time: Long, unit: TimeUnit = TimeUnit.MILLISECONDS) {
    Thread.sleep(unit.convert(time, TimeUnit.MILLISECONDS))
}
