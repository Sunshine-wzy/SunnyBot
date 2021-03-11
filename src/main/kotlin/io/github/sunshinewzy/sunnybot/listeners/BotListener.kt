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
                
                if(sGroup.autoApplyAcc.contains(msg.substringAfter("�𰸣�"))){
                    accept()
                    group.sendMsg("��Ⱥ����", """
                        $fromNick ($fromId) ������뱾Ⱥ��������Ϣ:
                        $message
                        
                        ���Ͼ�ȷ�ؼ���
                        ���Զ�ͬ��~
                    """.trimIndent())
                    
                    return@subscribeAlways
                }
                
                sGroup.autoApplyFuz.forEach { fuz ->
                    if(msg.contains(fuz.toLowerCase())){
                        accept()
                        group.sendMsg("��Ⱥ����", """
                            $fromNick ($fromId) ������뱾Ⱥ��������Ϣ:
                            $message
                            
                            ����ģ���ؼ���: $fuz
                            ���Զ�ͬ��~
                        """.trimIndent())

                        return@subscribeAlways
                    }
                }
            }
            
        }
        
    }
}