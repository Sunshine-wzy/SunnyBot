package io.github.sunshinewzy.sunnybot.events.game

import io.github.sunshinewzy.sunnybot.objects.SDataGroup
import io.github.sunshinewzy.sunnybot.objects.SGroup
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member

class SGroupGameEvent(
    val member: Member,
    val group: Group,
    val groupId: Long,
    val sGroup: SGroup,
    val sDataGroup: SDataGroup,
    val msg: String
): SGameEvent