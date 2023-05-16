import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

class Poller<I, O, out F>(
    context: CoroutineContext = EmptyCoroutineContext, block: suspend PollScope<I, O>.() -> F
) : AbstractCoroutineContextElement(Poller) {
    companion object Key : CoroutineContext.Key<Poller<*, *, *>>

    private val coroutine = block.createCoroutine(PollScope(), Continuation(context + this) { future = it })
    internal var next: NextPoll<I, O>? = null

    private var future: Result<F>? = null

    fun poll(input: I): PollResult<O, F> {
        if (next == null) {
            coroutine.resume(Unit)
        } else {
            next!!.continuation.resume(input)
        }

        return if (future != null) {
            PollResult.Ready(future!!.getOrThrow())
        } else {
            PollResult.Pending(next!!.output)
        }
    }

}

fun <O, F> Poller<Unit, O, F>.poll(): PollResult<O, F> = poll(Unit)

internal data class NextPoll<in I, out O>(
    val continuation: Continuation<I>, val output: O
)

sealed class PollResult<out O, out F> {
    data class Ready<O, F>(val future: F) : PollResult<O, F>()
    data class Pending<O, F>(val output: O) : PollResult<O, F>()
}

class PollScope<out I, in O> internal constructor() {
    suspend fun yield(output: O): I = suspendCoroutine { continuation ->
        val poller = continuation.context[Poller]!! as Poller<I, O, *>
        poller.next = NextPoll(
            continuation, output
        )
        COROUTINE_SUSPENDED
    }
}

suspend fun <I> PollScope<I, Unit>.yield(): I = yield(Unit)




