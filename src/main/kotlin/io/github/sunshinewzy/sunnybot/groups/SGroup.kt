package io.github.sunshinewzy.sunnybot.groups

import net.mamoe.mirai.contact.Group

val groups = HashMap<Long, SGroup>()

class SGroup(group: Group) {
    private val group: Group
    private val id: Long
    
    var runningState = ""
    var hour24 = IntArray(5) { -1 }

    init {
        this.group = group
        this.id = group.id


    }
    
}