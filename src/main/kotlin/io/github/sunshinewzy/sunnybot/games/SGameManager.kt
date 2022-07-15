package io.github.sunshinewzy.sunnybot.games

import io.github.sunshinewzy.sunnybot.enums.RunningState
import io.github.sunshinewzy.sunnybot.events.game.SGroupGameEvent
import io.github.sunshinewzy.sunnybot.objects.*
import io.github.sunshinewzy.sunnybot.sunnyChannel
import io.github.sunshinewzy.sunnybot.sunnyScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.findIsInstance

object SGameManager {
    val sGroupGameHandlers = ArrayList<SGroupGame>()
    
    
    fun gameInit(bot: Bot) {
        regGame()
        
        sunnyChannel.subscribeMessages {
            (startsWith("#") or contains("sunny") or contains("ั๔นโ")) game@{
                if (sender !is Member)
                    return@game
                val member = sender as Member
                val group = member.group
                val groupId = group.id
                val sGroup = group.getSGroup()
                val sDataGroup = group.getSData()
                val msg = message.findIsInstance<PlainText>()?.contentToString() ?: return@game
                
                regPlayer(member)
                callGame(member, group, groupId, sGroup, sDataGroup, msg)
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
    
    private fun callGame(
        member: Member,
        group: Group,
        groupId: Long,
        sGroup: SGroup,
        sDataGroup: SDataGroup,
        msg: String
    ) {
        val sGroupGameEvent = SGroupGameEvent(member, group, groupId, sGroup, sDataGroup, msg)
        val state = sDataGroup.runningState
        
        sunnyScope.launch {
            sGroupGameHandlers.forEach {
                if((state == RunningState.FREE || !state.isMain) && msg.contains(it.name)){
                    it.startGame(sGroupGameEvent)
                    return@forEach
                }
                
                if(it.gameStates.contains(state))
                   it.runGame(sGroupGameEvent)
            }
        }
    }
}