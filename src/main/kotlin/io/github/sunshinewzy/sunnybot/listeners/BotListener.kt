package io.github.sunshinewzy.sunnybot.listeners

import io.github.sunshinewzy.sunnybot.miraiBot
import io.github.sunshinewzy.sunnybot.miraiScope
import io.github.sunshinewzy.sunnybot.sunnyInit
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.event.subscribeAlways

var cnt = 0

fun listenBot() {
    miraiScope.subscribeAlways<BotOnlineEvent> {
        miraiBot = bot
        cnt++

        if (cnt == 1)
            sunnyInit()
    }
    
    miraiScope.subscribeAlways<BotInvitedJoinGroupRequestEvent> {
        accept()
    }
    
    miraiScope.subscribeAlways<NewFriendRequestEvent> { 
        accept()
    }
}