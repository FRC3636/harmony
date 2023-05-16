fun main() {
    val poller = Poller<Unit, Unit, Int> {
        for (i in 0 until 3) {
            yield()
        }

        0
    }

    while (true) {
        println(poller.poll())
    }
}
