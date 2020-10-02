package io.github.sunshinewzy.sunnybot.listeners

import io.github.sunshinewzy.sunnybot.miraiScope
import net.mamoe.mirai.event.events.MessageRecallEvent
import net.mamoe.mirai.event.events.author
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.source

val groupMsgs = HashMap<Int, Message>()

fun listenMessage() {
    miraiScope.subscribeAlways<GroupMessageEvent> {
        groupMsgs[message.source.id] = message
    }
    
    miraiScope.subscribeAlways<MessageRecallEvent.GroupRecall> {
        val startTime = System.currentTimeMillis()
        
        if (groupMsgs[messageId] != null) 
            group.sendMessage(PlainText("${author.nick} 撤回了:\n").plus(groupMsgs[messageId]!!))
        else group.sendMessage("${author.nick} 撤回了一条消息")
        
        group.sendMessage("[防撤回系统]搜索用时: ${System.currentTimeMillis() - startTime}ms")
    }
}