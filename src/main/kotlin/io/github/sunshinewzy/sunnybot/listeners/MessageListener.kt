package io.github.sunshinewzy.sunnybot.listeners

import io.github.sunshinewzy.sunnybot.antiRecall
import io.github.sunshinewzy.sunnybot.sunnyChannel
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageRecallEvent

object MessageListener {
    fun listenMessage() {
        sunnyChannel.subscribeAlways<GroupMessageEvent> {
            antiRecall?.saveMessage(group.id, message)

        }

        sunnyChannel.subscribeAlways<MessageRecallEvent.GroupRecall> {
            antiRecall?.antiRecallByGroupEvent(this)
        }

    }
}