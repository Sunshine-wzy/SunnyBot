package io.github.sunshinewzy.sunnybot.listeners

import io.github.sunshinewzy.sunnybot.*
import io.github.sunshinewzy.sunnybot.commands.SCImage
import io.github.sunshinewzy.sunnybot.commands.SCMiraiCode
import io.github.sunshinewzy.sunnybot.commands.SCommandManager
import io.github.sunshinewzy.sunnybot.enums.RunningState
import io.github.sunshinewzy.sunnybot.games.SGameManager
import io.github.sunshinewzy.sunnybot.objects.SBOwnThink
import io.github.sunshinewzy.sunnybot.objects.SRequest
import io.github.sunshinewzy.sunnybot.objects.data.ImageData
import io.github.sunshinewzy.sunnybot.objects.getSData
import io.github.sunshinewzy.sunnybot.objects.internal.RequestAddImage
import io.github.sunshinewzy.sunnybot.objects.setRunningState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.console.command.CommandSender.Companion.asCommandSender
import net.mamoe.mirai.console.command.CommandSender.Companion.toCommandSender
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.events.MessageRecallEvent
import net.mamoe.mirai.event.events.UserMessageEvent
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.*

object MessageListener {
    private val brackets = hashMapOf("(" to ")", "（" to "）", "[" to "]", "【" to "】", "{" to "}", "<" to ">", "《" to "》")
    

    fun listenMessage() {
        sunnyChannel.subscribeMessages {

            (contains("老子不会")) end@{
                val group = subject as? Group ?: return@end
                val data = group.getSData()

                val state = data.runningState
                if(state == RunningState.FREE) {
                    subject.sendMsg("Game", "当前没有游戏正在进行。")
                    return@end
                }

                val players = data.players
                if(players.contains(sender.id) || players.checkTimeout()) {
                    group.setRunningState(RunningState.FREE)
                    subject.sendMsg("Game", "${state.gameName} 游戏结束")
                } else {
                    subject.sendMsg("Game", """
                    您不是当前游戏的玩家
                    在 ${players.timeLeft()}毫秒 后才能结束当前游戏
                """.trimIndent())
                }

            }

            (contains("再来亿把")) startAgain@{
                val member = sender as? Member ?: return@startAgain
                val group = member.group
                val data = group.getSData()

                val state = data.runningState
                val lastState = data.lastRunningState
                if(state == RunningState.FREE) {
                    if(lastState != RunningState.FREE) {
                        SGameManager.callGame(member, lastState.gameName, true)
                    } else {
                        group.sendMsg("Game", "没有游戏记录")
                    }

                    return@startAgain
                }

                val players = data.players
                if(players.contains(sender.id) || players.checkTimeout()) {
                    SGameManager.callGame(member, state.gameName, true)
                } else {
                    subject.sendMsg("Game", """
                    您不是当前游戏的玩家
                    在 ${players.timeLeft()}毫秒 后才能结束当前游戏
                """.trimIndent())
                }

            }
            
            startsWith("#", removePrefix = true, trim = true) {
                SCommandManager.executeCommand(toCommandSender(), message, it)
            }
            
            startsWith("#来张", removePrefix = true, trim = true) { libName ->
                SCImage.sendRandomImage(sender, libName)
            }



//        (contains("sunny", ignoreCase = true) and (contains("闭嘴") or contains("睡觉") or contains("关"))) sleep@{
//            if(sender !is Member)
//                return@sleep
//            val group = getGroup(sender) ?: return@sleep
//            
//            group.getSGroup().isOpen = false
//            group.sendMessage("Bye~ master")
//        }
//        
//        (contains("sunny", ignoreCase = true) and (contains("醒醒") or contains("启") or contains("开"))) start@{
//            if(sender !is Member)
//                return@start
//            val group = getGroup(sender) ?: return@start
//            
//            group.getSGroup().isOpen = true
//            group.sendMessage("Hi! master")
//        }

            atBot {
                val member = sender as? Member ?: return@atBot
                val text = message.findIsInstance<PlainText>() ?: kotlin.run {
                    member.group.sendIntroduction()
                    return@atBot
                }
                val msg = text.content.replace("\n", "").replace(" ", "")
                if(msg.isEmpty()) {
                    member.group.sendIntroduction()
                    return@atBot
                }

                val ownThink = SRequest("https://api.ownthink.com/bot?spoken=$msg")
                    .resultBean<SBOwnThink>() ?: kotlin.run {
                        member.group.sendMessage("思 考 不 能")
                        return@atBot
                }
                if(ownThink.message == "success") {
                    member.group.sendMessage(QuoteReply(message) + At(member) + " ${ownThink.data.info.text}")
                } else member.group.sendMessage("思 考 不 能")

            }

        }
        
        
        sunnyChannel.subscribeAlways<GroupMessageEvent>(priority = EventPriority.HIGHEST) {
            antiRecall?.saveMessage(group.id, message)
            
            message.findIsInstance<PlainText>()?.let { text -> 
                val content = text.content
                
                if(content.isNotEmpty()) {
                    // 括号补全
//                    val endStr = content.substring(content.length - 1, content.length)
//                    brackets[endStr]?.let {
//                        group.sendMessage(it)
//                    }
                    
                    // 消息图片
                    ImageData.messageImages[content.lowercase()]?.let {
                        withContext(Dispatchers.IO) {
                            group.sendMessage(QuoteReply(message) + it.getImages(group))
                        }
                    }
                }
                
            }
            
        }

        sunnyChannel.subscribeAlways<MessageRecallEvent.GroupRecall> {
            antiRecall?.antiRecallByGroupEvent(this)
        }
        
        sunnyChannel.subscribeAlways<MessageEvent> { 
            SCMiraiCode.userGetMiraiCode[sender]?.let { time ->
                if(System.currentTimeMillis() - time <= 60_000L) {
                    sender.sendMsg(SCMiraiCode.description, QuoteReply(message) + message.serializeToMiraiCode())
                }
                
                SCMiraiCode.userGetMiraiCode.remove(sender)
            }
        }
        
        sunnyChannel.subscribeAlways<UserMessageEvent> { 
            if(sender.isSunnyAdmin()) {
                val firstContent = message.findIsInstance<PlainText>()?.content ?: return@subscribeAlways
                if(firstContent.contentEquals("Y", true)) {
                    val quoteReply = message[QuoteReply] ?: return@subscribeAlways
                    val originMessage = quoteReply.source.originalMessage
                    val text = originMessage.getPlainTextContent()
                    
                    val i = text.indexOf('[').takeIf { it != -1 } ?: return@subscribeAlways
                    val j = text.indexOf(']').takeIf { it != -1 } ?: return@subscribeAlways
                    val uuid = text.substring(i + 1, j).takeIf { it.length == 36 } ?: return@subscribeAlways
                    val requestAddImage = RequestAddImage[uuid] ?: return@subscribeAlways
                    
                    requestAddImage.apply {
                        SCImage.executeCommand(
                            sender.asCommandSender(true),
                            buildMessageChain { 
                                +libName.toPlainText()
                                +imageName.toPlainText()
                                if(message.isNotEmpty()) +message.toPlainText()
                                addAll(images)
                            }
                        )
                    }
                }
            }
        }
        
    }
    
}