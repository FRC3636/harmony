import org.junit.jupiter.api.Test
import kotlin.test.*

class PollerTest {
    @Test
    fun `test sync return`() {
        assertPollResults<Unit, Unit, Int>(
            Poller { 42 },
            Unit to PollResult.Ready(42)
        )
    }

    @Test
    fun `test single simple poll`() {
        assertPollResults<String, Unit, String>(
            Poller { yield() },
            "" to PollResult.Pending(Unit),
            "foo" to PollResult.Ready("foo")
        )
    }
}

fun <I, O, F> assertPollResults(poller: Poller<I, O, F>, vararg polls: Pair<I, PollResult<O, F>>) {
    for ((input, expected) in polls) {
        assertEquals(
            expected,
            poller.poll(input)
        )
    }
}