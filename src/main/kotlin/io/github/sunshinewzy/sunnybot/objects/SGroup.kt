package io.github.sunshinewzy.sunnybot.objects

import net.mamoe.mirai.contact.Group

val groups = HashMap<Long, SGroup>()

class SGroup(private val group: Group) {
    private val id: Long = group.id

    var runningState = ""
    var hour24 = IntArray(5) { -1 }

}
