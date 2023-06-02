# Harmony

Harmony is
an experimental, opinionated library
for programming FRC robots using Kotlin.

## Coroutines

Instead of WPILib commands, Harmony uses
a concurrency implementation based on Kotlin's native coroutine support.
This implementation is also completely compatible with existing WPILib command-based code:
Harmony code can invoke commands and Harmony coroutines can be transformed into commands.

```kt
// convert the coroutine into a WPILib command
HarmonyCommand {
    // regular kotlin code
    val product = 6 * 9
    assert(product == 42)
    
    // run two coroutines concurrently
    join(
        intakePiece(),
        {
            // coroutine which returns a value asynchronously
            val goal = driveToNearestGoal()
            
            // run a coroutine which depends on a value produced asynchronously
            scorePiece(goal)
        }
    )
}

suspend fun CoroutineScope.scorePiece(goal: Goal) {
    aimShooter(goal)
    
    // run a WPILib command from a harmony coroutine
    runCommand(TakeShotCommand())
}
```

## Math

Harmony will define its own APIs
for most WPILib things involving vector math (e.g. `Position2d`) and physical quantities.
This will give us checked units and a more consistent UI for positions, rotations, etc.

This is not yet implemented.

## Controllers

Harmony will have declarative stream-based controller primitives
native to the Kotlin coroutine paradigm rather than WPILib's imperative controllers.

This is not yet implemented.