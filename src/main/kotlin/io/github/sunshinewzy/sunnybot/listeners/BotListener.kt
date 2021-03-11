package io.github.sunshinewzy.sunnybot.listeners

import io.github.sunshinewzy.sunnybot.objects.getSGroup
import io.github.sunshinewzy.sunnybot.sendMsg
import io.github.sunshinewzy.sunnybot.sunnyBot
import io.github.sunshinewzy.sunnybot.sunnyChannel
import io.github.sunshinewzy.sunnybot.sunnyInit
import net.mamoe.mirai.console.command.descriptor.ExperimentalCommandDescriptors
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.contact.remarkOrNameCardOrNick
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.At

object BotListener {
    var cnt = 0

    @ExperimentalCommandDescriptors
    @ConsoleExperimentalApi
    fun listenBot() {
        
        sunnyChannel.apply {
            
            subscribeAlways<BotOnlineEvent> {
                sunnyBot = bot
                cnt++

                if (cnt == 1)
                    sunnyInit()
            }

            subscribeAlways<BotInvitedJoinGroupRequestEvent> {
                accept()
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
                    group.sendMessage("${member.remarkOrNameCardOrNick} (${member.id}) $msg")
            }
            
            subscribeAlways<MemberJoinRequestEvent> { 
                val group = group ?: return@subscribeAlways
                val sGroup = group.getSGroup()
                if(sGroup.autoApplyAcc.isEmpty() && sGroup.autoApplyFuz.isEmpty()) return@subscribeAlways
                val msg = message.toLowerCase()
                
                if(sGroup.autoApplyAcc.contains(msg.substringAfter("答案："))){
                    accept()
                    group.sendMsg("加群审批", """
                        $fromNick ($fromId) 申请加入本群，申请信息:
                        $message
                        
                        符合精确关键字
                        已自动同意~
                    """.trimIndent())
                    
                    return@subscribeAlways
                }
                
                sGroup.autoApplyFuz.forEach { fuz ->
                    if(msg.contains(fuz.toLowerCase())){
                        accept()
                        group.sendMsg("加群审批", """
                            $fromNick ($fromId) 申请加入本群，申请信息:
                            $message
                            
                            符合模糊关键字: $fuz
                            已自动同意~
                        """.trimIndent())

                        return@subscribeAlways
                    }
                }
            }
            
        }
        
    }
}