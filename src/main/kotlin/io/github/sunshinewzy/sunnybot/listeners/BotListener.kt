package io.github.sunshinewzy.sunnybot.listeners

import io.github.sunshinewzy.sunnybot.sunnyBot
import io.github.sunshinewzy.sunnybot.sunnyInit
import io.github.sunshinewzy.sunnybot.sunnyScope
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.event.subscribeAlways

var cnt = 0

fun listenBot() {
    sunnyScope.subscribeAlways<BotOnlineEvent> {
        sunnyBot = bot
        cnt++

        if (cnt == 1)
            sunnyInit()
    }
    
    sunnyScope.subscribeAlways<BotInvitedJoinGroupRequestEvent> {
        accept()
    }
    
    sunnyScope.subscribeAlways<NewFriendRequestEvent> { 
        accept()
    }
}