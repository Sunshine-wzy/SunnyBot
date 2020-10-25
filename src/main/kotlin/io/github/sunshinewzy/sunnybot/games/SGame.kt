package io.github.sunshinewzy.sunnybot.games

import io.github.sunshinewzy.sunnybot.events.game.SGameEvent

abstract class SGame<T: SGameEvent>(val name: String) {
    abstract suspend fun runGame(event: T)
    
    abstract suspend fun startGame(event: T)
}