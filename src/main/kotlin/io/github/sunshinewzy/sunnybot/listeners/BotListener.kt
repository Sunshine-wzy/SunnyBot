package io.github.sunshinewzy.sunnybot.listeners

import io.github.sunshinewzy.sunnybot.sunnyBot
import io.github.sunshinewzy.sunnybot.sunnyChannel
import io.github.sunshinewzy.sunnybot.sunnyInit
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent

var cnt = 0

fun listenBot() {
    sunnyChannel.subscribeAlways<BotOnlineEvent> {
        sunnyBot = bot
        cnt++

        if (cnt == 1)
            sunnyInit()
    }
    
    sunnyChannel.subscribeAlways<BotInvitedJoinGroupRequestEvent> {
        accept()
    }
    
    sunnyChannel.subscribeAlways<NewFriendRequestEvent> { 
        accept()
    }
}