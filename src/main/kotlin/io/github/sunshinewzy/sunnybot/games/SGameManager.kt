package io.github.sunshinewzy.sunnybot.games

import io.github.sunshinewzy.sunnybot.enums.RunningState
import io.github.sunshinewzy.sunnybot.events.game.SGroupGameEvent
import io.github.sunshinewzy.sunnybot.games.game.SGChess
import io.github.sunshinewzy.sunnybot.games.game.SGFiveInARow
import io.github.sunshinewzy.sunnybot.games.game.SGHour24
import io.github.sunshinewzy.sunnybot.games.game.SGTicTacToe
import io.github.sunshinewzy.sunnybot.sunnyChannel
import io.github.sunshinewzy.sunnybot.sunnyScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.findIsInstance

object SGameManager {
    val sGroupGameHandlers = ArrayList<SGroupGame>()
    
    
    fun gameInit(bot: Bot) {
        regGame()
        
        sunnyChannel.subscribeMessages {
            (startsWith("#")) game@{
                val member = sender as? Member ?: return@game
                val msg = message.findIsInstance<PlainText>()?.contentToString() ?: return@game
                
                callGame(member, msg)
            }
            
        }
    }
    
    private fun regGame() {
        registerGroupGame(SGHour24)
        registerGroupGame(SGTicTacToe)
        registerGroupGame(SGChess)
        registerGroupGame(SGFiveInARow)
    }
    
    private fun registerGroupGame(sGroupGame: SGroupGame) {
        sGroupGameHandlers.add(sGroupGame)
    }
    
    fun callGame(member: Member, msg: String) {
        val sGroupGameEvent = SGroupGameEvent(member, msg)
        val state = sGroupGameEvent.sDataGroup.runningState
        
        sunnyScope.launch {
            sGroupGameHandlers.forEach {
                if((state == RunningState.FREE || !state.isMain) && msg.contains(it.name)) {
                    it.startGame(sGroupGameEvent)
                    return@launch
                }
                
                if(it.gameStates.contains(state)) {
                    it.runGame(sGroupGameEvent)
                    return@launch
                }
            }
        }
    }
    
}