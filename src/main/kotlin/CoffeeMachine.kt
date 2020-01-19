import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.selects.*

class EspressoMachine(scope: CoroutineScope): CoroutineScope by scope {
    data class PullEspressoShotRequest(val groundBeans: CoffeeBean.GroundBeans, val espressoChan: SendChannel<Espresso>)

    data class SteamMilkRequest(val milk: Milk, val steamMilkChan: SendChannel<Milk.SteamedMilk>)

    private val portafilterOne: SendChannel<PullEspressoShotRequest> = actor() {
        consumeEach {
            log("Pulling espresso shot on portafilter one")
            delay(20)
            it.espressoChan.send(Espresso(it.groundBeans))
            it.espressoChan.close()
        }
    }

    private val portafilterTwo: SendChannel<PullEspressoShotRequest> = actor() {
        consumeEach {
            log("Pulling espresso shot on portafilter two")
            delay(20)
            it.espressoChan.send(Espresso(it.groundBeans))
            it.espressoChan.close()
        }
    }

    private val steamWandOne: SendChannel<SteamMilkRequest> = actor() {
        consumeEach {
            log("Steaming milk with steam wand one")
            delay(10)
            it.steamMilkChan.send(Milk.SteamedMilk(it.milk))
            it.steamMilkChan.close()
        }
    }

    private val steamWandTwo: SendChannel<SteamMilkRequest> = actor() {
        consumeEach {
            log("Steaming milk with steam wand two")
            delay(10)
            it.steamMilkChan.send(Milk.SteamedMilk(it.milk))
            it.steamMilkChan.close()
        }
    }

    suspend fun pullEspressoShot(groundBeans: CoffeeBean.GroundBeans) = select<Espresso> {
        val channel = Channel<Espresso>()
        val req = PullEspressoShotRequest(groundBeans, channel)
        portafilterOne.onSend(req) {
            channel.receive()
        }
        portafilterTwo.onSend(req) {
            channel.receive()
        }
    }

    suspend fun steamMilk(milk: Milk) = select<Milk.SteamedMilk> {
        val chan = Channel<Milk.SteamedMilk>()
        val req = SteamMilkRequest(milk, chan)
        steamWandOne.onSend(req) {
            chan.receive()
        }
        steamWandTwo.onSend(req) {
            chan.receive()
        }
    }

    fun shutdown() {
        portafilterOne.close()
        portafilterTwo.close()
        steamWandOne.close()
        steamWandTwo.close()
    }
}
