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
                    "���Ⱥ423179929��@Ⱥ�� ˵�����Ի����˵�ʹ�����󣬵ȴ�Ⱥ���ֶ�ͬ��(����48Сʱ�ڸ���ظ�)"
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
                val msg = message.lowercase(Locale.getDefault()).substringAfter("�𰸣�")
                
                sGroup.autoApply.forEach { key ->
                    if(msg.contains(key.lowercase(Locale.getDefault()))){
                        accept()
                        group.sendMsg("��Ⱥ���� - �Զ�ͬ��", """
                            $fromNick ($fromId) ������뱾Ⱥ��������Ϣ:
                            $msg
                            
                            ���Ϲؼ���: $key
                            ���Զ�ͬ��~
                        """.trimIndent())

                        return@subscribeAlways
                    }
                }
                
                sGroup.autoReject.forEach { key ->
                    if(msg.contains(key.lowercase(Locale.getDefault()))){
                        reject()
                        group.sendMsg("��Ⱥ���� - �Զ��ܾ�", """
                            $fromNick ($fromId) ������뱾Ⱥ��������Ϣ:
                            $msg
                            
                            ���Ϲؼ���: $key
                            ���Զ��ܾ�~
                        """.trimIndent())

                        return@subscribeAlways
                    }
                }
            }
            
        }
        
    }
}