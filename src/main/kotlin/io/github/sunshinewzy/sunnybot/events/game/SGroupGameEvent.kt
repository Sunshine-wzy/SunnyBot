package io.github.sunshinewzy.sunnybot.events.game

import io.github.sunshinewzy.sunnybot.objects.getSData
import io.github.sunshinewzy.sunnybot.objects.getSGroup
import net.mamoe.mirai.contact.Member

class SGroupGameEvent(
    val member: Member,
    val msg: String
): SGameEvent {
    val group = member.group
    val sGroup = group.getSGroup()
    val sDataGroup = group.getSData()
}