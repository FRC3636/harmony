package com.ghsrobo.harmony.coroutine

import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj2.command.Subsystem

class HarmonyCommand(
    private val requirements: Set<Subsystem> = emptySet(),
    private val onCancel: () -> Unit = { },
    private val block: suspend CoroutineScope.() -> Unit
) : Command {
    private lateinit var future: PollFuture<Unit, Unit>
    private var finished: Boolean = false

    override fun initialize() {
        future = PollFuture { block(CoroutineScope) }
        finished = future.start() is Poll.Ready<*>
    }

    override fun execute() {
        finished = future.poll() is Poll.Ready<*>
    }

    override fun isFinished(): Boolean = finished

    override fun end(interrupted: Boolean) {
        if (interrupted) onCancel()
    }

    override fun getRequirements(): Set<Subsystem> {
        return requirements
    }
}

suspend fun CoroutineScope.runCommand(command: Command) {
    command.initialize()
    do {
        yield(onCancel = {
            command.end(true)
        })
        command.execute()
    } while (!command.isFinished)
    command.end(false)
}