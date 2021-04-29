package com.github.drumber.input2esp.backend.utils.event

import androidx.lifecycle.Observer

/**
 * An [Observer] for [EventWrapper]s, simplifying the pattern of checking if the [EventWrapper]'s content has
 * already been handled.
 *
 * [onEventUnhandledContent] is *only* called if the [EventWrapper]'s contents has not been handled.
 *
 * source: https://gist.github.com/JoseAlcerreca/e0bba240d9b3cffa258777f12e5c0ae9
 */
class EventObserver<T>(private val onEventUnhandledContent: (T) -> Unit) : Observer<EventWrapper<T>> {
    override fun onChanged(event: EventWrapper<T>?) {
        event?.getContentIfNotHandled()?.let { value ->
            onEventUnhandledContent(value)
        }
    }
}