package io.github.sunshinewzy.sunnybot.games

import io.github.sunshinewzy.sunnybot.enums.RunningState
import io.github.sunshinewzy.sunnybot.events.game.SGroupGameEvent

abstract class SGroupGame(
    name: String,
    gameStates: List<RunningState>
): SGame<SGroupGameEvent>(name, gameStates) {
    constructor(name: String, gameState: RunningState): this(name, listOf(gameState))
    
    constructor(name: String, vararg gameState: RunningState): this(name, gameState.asList())
}