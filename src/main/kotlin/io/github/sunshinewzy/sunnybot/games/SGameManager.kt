package io.github.sunshinewzy.sunnybot.games

import io.github.sunshinewzy.sunnybot.objects.SGroup
import io.github.sunshinewzy.sunnybot.objects.SGroupData
import io.github.sunshinewzy.sunnybot.objects.regPlayer
import io.github.sunshinewzy.sunnybot.sunnyScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.PlainText

object SGameManager {
    private val sGameHandlers = ArrayList<SGame>()
    
    
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
                val msg = message[PlainText.Key]?.contentToString() ?: return@game
                
                regPlayer(member)
                callGame(member, group, groupId, sGroup, msg)
            }
        }
    }
    
    private fun regGame() {
        registerGame(SGHour24)
        registerGame(SGTicTacToe)
    }
    
    fun registerGame(sGame: SGame) {
        sGameHandlers.add(sGame)
    }
    
    private fun callGame(
        member: Member,
        group: Group,
        groupId: Long,
        sGroup: SGroup,
        msg: String
    ) {
        sunnyScope.launch {
            sGameHandlers.forEach {
                it.run(member, group, groupId, sGroup, msg)
            }
        }
    }
}