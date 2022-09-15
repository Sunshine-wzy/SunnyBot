package io.github.sunshinewzy.sunnybot.listeners

import io.github.sunshinewzy.sunnybot.objects.getSGroup
import io.github.sunshinewzy.sunnybot.sendMsg
import io.github.sunshinewzy.sunnybot.sunnyBot
import io.github.sunshinewzy.sunnybot.sunnyChannel
import io.github.sunshinewzy.sunnybot.sunnyInit
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.At
import java.util.*

object BotListener {
    private lateinit var msgListener: Listener<MessageEvent>
    var isOnline = false

    fun listenBot() {
        sunnyChannel.apply {
            msgListener = subscribeAlways { 
                if(!isOnline) {
                    sunnyBot = bot
                    isOnline = true
                    sunnyInit()

                    msgListener.complete()
                }
            }
            
            
//            subscribeAlways<BotOnlineEvent> {
//                sunnyBot = bot
//                cnt++
//
//                if (cnt == 1)
//                    sunnyInit()
//            }

            subscribeAlways<BotInvitedJoinGroupRequestEvent> {
                invitor?.sendMessage(
                    "请加群423179929，@群主 说明您对机器人的使用需求，等待群主手动同意(将会48小时内给予回复)"
                )
            }

            subscribeAlways<NewFriendRequestEvent> {
                accept()
            }
            
            subscribeAlways<MemberJoinEvent> { 
                val msg = group.getSGroup().welcomeMessage
                
                if(msg != "")
                    group.sendMessage(At(member) + " $msg")
            }
            
            subscribeAlways<MemberLeaveEvent> {
                val msg = group.getSGroup().leaveMessage

                if(msg != "")
                    group.sendMessage("${member.nameCardOrNick} (${member.id}) $msg")
            }
            
            subscribeAlways<MemberJoinRequestEvent> { 
                val group = group ?: return@subscribeAlways
                val sGroup = group.getSGroup()
                if(sGroup.autoApply.isEmpty()) return@subscribeAlways
                val msg = message.lowercase(Locale.getDefault()).substringAfter("答案：")
                
                sGroup.autoApply.forEach { key ->
                    if(msg.contains(key.lowercase(Locale.getDefault()))){
                        accept()
                        group.sendMsg("加群审批 - 自动同意", """
                            $fromNick ($fromId) 申请加入本群，申请信息:
                            $msg
                            
                            符合关键字: $key
                            已自动同意~
                        """.trimIndent())

                        return@subscribeAlways
                    }
                }
                
                sGroup.autoReject.forEach { key ->
                    if(msg.contains(key.lowercase(Locale.getDefault()))){
                        reject()
                        group.sendMsg("加群审批 - 自动拒绝", """
                            $fromNick ($fromId) 申请加入本群，申请信息:
                            $msg
                            
                            符合关键字: $key
                            已自动拒绝~
                        """.trimIndent())

                        return@subscribeAlways
                    }
                }
            }
            
        }
        
    }
}