package io.github.sunshinewzy.sunnybot.games

import io.github.sunshinewzy.sunnybot.objects.SGroup
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member

interface SGame {
    suspend fun run(
        member: Member,
        group: Group,
        groupId: Long,
        sGroup: SGroup,
        msg: String
    )
}