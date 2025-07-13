package me.santio.kdb.event

import org.slf4j.LoggerFactory
import java.util.function.Consumer

/**
 * A super quick and simple bus for handling events
 * @author santio
 */
class EventBus<Event: Any> {

    private val logger = LoggerFactory.getLogger(EventBus::class.java)
    private val subscriptions: MutableMap<Class<out Event>, MutableList<Consumer<out Event>>> = mutableMapOf()

    inline fun <reified E: Event> subscribe(handler: Consumer<E>) = subscribe(E::class.java, handler)
    fun <E: Event> subscribe(event: Class<E>, handler: Consumer<E>) {
        subscriptions.getOrPut(event) { mutableListOf() }.add(handler)
    }

    internal fun <E: Event> call(event: E) {
        if (logger.isDebugEnabled) {
            logger.debug("Event ${event::class.simpleName} was called")
        }

        subscriptions[event::class.java]?.forEach {
            (it as Consumer<Event>).accept(event)
        }
    }

}