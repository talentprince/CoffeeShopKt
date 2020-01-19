import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeUnit.MILLISECONDS
import kotlin.system.measureTimeMillis

// make coffee by executing each step sequentially
fun main(args: Array<String>) {
    val orders = listOf(Menu.Cappuccino(CoffeeBean.Regular, Milk.Whole),
        Menu.Cappuccino(CoffeeBean.Premium, Milk.Breve),
        Menu.Cappuccino(CoffeeBean.Regular, Milk.NonFat),
        Menu.Cappuccino(CoffeeBean.Decaf, Milk.Whole),
        Menu.Cappuccino(CoffeeBean.Regular, Milk.NonFat),
        Menu.Cappuccino(CoffeeBean.Decaf, Milk.NonFat))
    log(orders)
    val t = measureTimeMillis {
        makeCoffee(orders)
    }
    println("Execution time: $t ms")
}

// takes a list of orders and prcocesses them one at a time
private fun makeCoffee(orders: List<Menu>) {
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

private fun grindCoffeeBeans(beans: CoffeeBean): CoffeeBean.GroundBeans {
    log("Grinding beans")
    sleep(30)
    return CoffeeBean.GroundBeans(beans)
}

private fun pullEspressoShot(groundBeans: CoffeeBean.GroundBeans): Espresso {
    log("Pulling espresso shot")
    sleep(20)
    return Espresso(groundBeans)
}

private fun steamMilk(milk: Milk): Milk.SteamedMilk {
    log("Steaming milk")
    sleep(10)
    return Milk.SteamedMilk(milk)
}

private fun makeCappuccino(order: Menu.Cappuccino, espressoShot: Espresso, milk: Milk.SteamedMilk): Beverage.Cappuccino {
    log("Combining ingredients")
    sleep(5)
    return Beverage.Cappuccino(order, espressoShot, milk)
}

private fun sleep(time: Long, unit: TimeUnit = MILLISECONDS) {
    Thread.sleep(unit.convert(time, MILLISECONDS))
}
