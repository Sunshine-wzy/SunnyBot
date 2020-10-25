package io.github.sunshinewzy.sunnybot.listeners

import io.github.sunshinewzy.sunnybot.antiRecall
import io.github.sunshinewzy.sunnybot.sunnyScope
import net.mamoe.mirai.event.events.MessageRecallEvent
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.message.GroupMessageEvent


fun listenMessage() {
    sunnyScope.subscribeAlways<GroupMessageEvent> {
        antiRecall!!.saveMessage(group.id, message)
    }
    
    sunnyScope.subscribeAlways<MessageRecallEvent.GroupRecall> {
        antiRecall!!.antiRecallByGroupEvent(this)
    }
}