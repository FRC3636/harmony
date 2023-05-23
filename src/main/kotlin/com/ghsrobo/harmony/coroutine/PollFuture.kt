package com.ghsrobo.harmony.coroutine

import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED

class PollFuture<I, out F>(
    context: CoroutineContext = EmptyCoroutineContext, block: suspend PollScope<I>.() -> F
) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<PollFuture<*, *>>

    private val coroutine = block.createCoroutine(PollScope(), Continuation(context + this) { future = it })

    internal var cancellation: () -> Unit = {}
    internal var next: Continuation<I>? = null

    private var future: Result<F>? = null

    // Begin computing the future.
    //
    // MUST be called at most once, before any calls to `coroutine.poll`.
    fun start(): Poll<F> {
        coroutine.resume(Unit)

        return if (future != null) {
            Poll.Ready(future!!.getOrThrow())
        } else {
            Poll.Pending()
        }
    }

    // Poll the future using the given input.
    //
    // Must not be called before `start`.
    fun poll(input: I): Poll<F> {
        next!!.resume(input)

        return if (future != null) {
            Poll.Ready(future!!.getOrThrow())
        } else {
            Poll.Pending()
        }
    }

    // Cancel the future.
    //
    // This is a no-op if the future has not been started.
    // The future should not be polled after it has been cancelled.
    fun cancel() {
        cancellation()
    }
}

fun <F> PollFuture<Unit, F>.poll(): Poll<F> = poll(Unit)
fun <F> PollFuture<Unit, F>.startOrPoll(): Poll<F> {
    return if (next == null) {
        start()
    } else {
        poll()
    }
}

sealed class Poll<out F> {
    data class Ready<F>(val future: F) : Poll<F>()
    class Pending<F> : Poll<F>() {
        override fun equals(other: Any?): Boolean {
            return other is Pending<*>
        }
    }

    fun readyOrNull(): F? {
        return when (this) {
            is Ready -> this.future
            else -> null
        }
    }
}

open class PollScope<out I> internal constructor() {
    suspend fun yield(onCancel: () -> Unit = {}): I = suspendCoroutine { continuation ->
        val poller = continuation.context[PollFuture]!! as PollFuture<I, *>
        poller.next = continuation
        poller.cancellation = onCancel
        COROUTINE_SUSPENDED
    }
}