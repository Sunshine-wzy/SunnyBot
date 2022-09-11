package io.github.sunshinewzy.sunnybot.objects.data

import io.github.sunshinewzy.sunnybot.sunnyBot
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.ForwardMessageBuilder
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.findIsInstance

@Serializable
class TransmitGroupData(val id: Long) {
    var period: Long = 15 * 60 * 1000
    var number: Int = 10
    var prefix: String = ":"

    val group: Group by lazy { sunnyBot.getGroupOrFail(id) }
    @Transient
    var time: Long = System.currentTimeMillis()
    @Transient
    lateinit var cache: ForwardMessageBuilder
    
    
    suspend fun transmit(group: Group) {
        if(cache.size <= 1) return
        
        time = System.currentTimeMillis()
        this.group.sendMessage(cache.build())
        cache = ForwardMessageBuilder(this.group).apply { 
            sunnyBot says "> 群 ${group.name} (${group.id})"
        }
    }
    
    suspend fun update(group: Group, event: MessageEvent) {
        if(!this::cache.isInitialized) {
            cache = ForwardMessageBuilder(this.group).apply {
                sunnyBot says "> 群 ${group.name} (${group.id})"
            }
        }
        cache.add(event)

        event.message.findIsInstance<PlainText>()?.let {
            if(it.content.startsWith(prefix)) {
                transmit(group)
                return
            }
        }
        
        if(cache.size > number || System.currentTimeMillis() - time > period) {
            transmit(group)
        }
    }
}