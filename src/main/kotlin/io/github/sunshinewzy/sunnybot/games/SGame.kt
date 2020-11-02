package io.github.sunshinewzy.sunnybot.games

import io.github.sunshinewzy.sunnybot.enums.RunningState
import io.github.sunshinewzy.sunnybot.events.game.SGameEvent

abstract class SGame<T: SGameEvent>(val name: String, val gameStates: List<RunningState>) {
    constructor(name: String, gameState: RunningState): this(name, listOf(gameState))
    
    constructor(name: String, vararg gameState: RunningState): this(name, gameState.asList())
    
    
    abstract suspend fun runGame(event: T)
    
    abstract suspend fun startGame(event: T)
}