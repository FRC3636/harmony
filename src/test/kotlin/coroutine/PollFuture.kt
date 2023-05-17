package coroutine

import org.junit.jupiter.api.Test
import kotlin.test.*

class PollFutureTest {
    @Test
    fun `test sync return`() {
        assertPollResults<Unit, Int>(
            PollFuture { 42 },
            Poll.Ready(42)
        )
    }

    @Test
    fun `test single yield`() {
        assertPollResults<String, String>(
            PollFuture {
                val s = yield()
                s + s
            },
            Poll.Pending(),
            "foo" to Poll.Ready("foofoo")
        )
    }

    @Test
    fun `test three yields`() {
        assertPollResults<Int, Int>(
            PollFuture {
                var acc = 0
                for (i in 0 until 3) {
                    acc += yield()
                }
                acc
            },
            Poll.Pending(),
            1 to Poll.Pending(),
            2 to Poll.Pending(),
            3 to Poll.Ready(6)
        )
    }
}

fun <I, F> assertPollResults(future: PollFuture<I, F>, start: Poll<F>, vararg polls: Pair<I, Poll<F>>) {
    assertEquals(
        start,
        future.start()
    )

    for ((input, expected) in polls) {
        assertEquals(
            expected,
            future.poll(input)
        )
    }
}