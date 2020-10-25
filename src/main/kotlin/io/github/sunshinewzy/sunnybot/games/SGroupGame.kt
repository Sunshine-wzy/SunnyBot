package io.github.sunshinewzy.sunnybot.games

import io.github.sunshinewzy.sunnybot.events.game.SGroupGameEvent

abstract class SGroupGame(name: String): SGame<SGroupGameEvent>(name) {
    
}