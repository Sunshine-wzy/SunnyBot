package io.github.sunshinewzy.sunnybot.listeners

import io.github.sunshinewzy.sunnybot.*
import io.github.sunshinewzy.sunnybot.objects.getSGroup
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
                    """
已经收到了您的请求，请稍等，我们将在24小时内处理。
当处理完成，我将通知您。
                        """.trimIndent()
                )
                sunnyBot.getGroup(rootgroup.toLong())?.sendMessage("""
收到了一个入群申请:
群号为: $groupId
申请人为: $invitorId

如需同意,请输入下方命令:
/accept $invitId
            """.trimMargin())
                invitList[invitId] = this
                invitId++

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