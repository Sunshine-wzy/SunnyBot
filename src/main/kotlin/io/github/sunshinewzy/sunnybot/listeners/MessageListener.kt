package io.github.sunshinewzy.sunnybot.listeners

import io.github.sunshinewzy.sunnybot.antiRecall
import io.github.sunshinewzy.sunnybot.miraiScope
import net.mamoe.mirai.event.events.MessageRecallEvent
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.GroupMessageEvent


fun listenMessage() {
    miraiScope.subscribeAlways<GroupMessageEvent> {
        antiRecall!!.saveMessage(group.id, message)
    }
    
    miraiScope.subscribeAlways<MessageRecallEvent.GroupRecall> {
        antiRecall!!.antiRecallByGroupEvent(this)
    }
}