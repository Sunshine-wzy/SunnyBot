package io.github.sunshinewzy.sunnybot.games

import io.github.sunshinewzy.sunnybot.events.game.SGroupGameEvent
import io.github.sunshinewzy.sunnybot.objects.*
import io.github.sunshinewzy.sunnybot.sunnyScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.PlainText

object SGameManager {
    val sGroupGameHandlers = ArrayList<SGroupGame>()
    
    
    fun gameInit(bot: Bot) {
        regGame()
        
        bot.subscribeMessages {
            (contains("sunny") or contains("ั๔นโ") or startsWith("#")) game@{
                if (sender !is Member)
                    return@game
                val member = sender as Member
                val group = member.group
                val groupId = group.id
                if(!SGroupData.sGroupMap.containsKey(groupId))
                    SGroupData.sGroupMap[groupId] = SGroup(groupId)
                val sGroup = SGroupData.sGroupMap[groupId]!!
                if(!sDataGroup.containsKey(groupId))
                    sDataGroup[groupId] = SDataGroup()
                val sDataGroup = sDataGroup[groupId]!!
                val msg = message[PlainText.Key]?.contentToString() ?: return@game
                
                regPlayer(member)
                callGame(member, group, groupId, sGroup, sDataGroup, msg)
            }
            
            
        }
    }
    
    private fun regGame() {
        registerGroupGame(SGHour24)
        registerGroupGame(SGTicTacToe)
    }
    
    fun registerGroupGame(sGroupGame: SGroupGame) {
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
        val state = sGroup.runningState
        
        sunnyScope.launch {
            sGroupGameHandlers.forEach {
                if((state == "" || state.contains(it.name)) && msg.contains(it.name)){
                    it.startGame(sGroupGameEvent)
                    return@forEach
                }
                
                if(state == it.name)
                   it.runGame(sGroupGameEvent)
            }
        }
    }
}