package com.ghsrobo.harmony.coroutine

object CoroutineScope : PollScope<Unit>() {
    // Join the results of multiple coroutines by running them in parallel until they all complete.
    suspend fun <T> join(vararg blocks: suspend CoroutineScope.() -> T): List<T> {
        val futures = blocks
            .map { block -> PollFuture<Unit, T> { block(CoroutineScope) } }
            .withIndex()
            .associate { it.index to it.value }
            .toMutableMap()
        val results = MutableList<T?>(futures.size) { null }

        while (futures.isNotEmpty()) {
            for ((i, future) in futures) {
                val poll = future.startOrPoll()
                if (poll is Poll.Ready) {
                    results[i] = poll.future
                    futures.remove(i)
                }
            }
            yield()
        }

        return results.map { it!! }
    }

    // Race multiple coroutines by running them in parallel until one completes.
    suspend fun <T> race(vararg blocks: suspend CoroutineScope.() -> T): T {
        val futures = blocks.map { block -> PollFuture<Unit, T> { block(CoroutineScope) } }

        while (true) {
            for (future in futures) {
                val poll = future.startOrPoll()
                if (poll is Poll.Ready) {
                    futures.filter { it !== future }.forEach(PollFuture<Unit, T>::cancel)
                    return poll.future
                }
            }
        }
    }
}