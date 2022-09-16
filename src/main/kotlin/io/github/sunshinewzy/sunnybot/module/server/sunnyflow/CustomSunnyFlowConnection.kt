package io.github.sunshinewzy.sunnybot.module.server.sunnyflow

import io.github.sunshinewzy.sunnybot.commands.SCMinecraftTransmit
import io.github.sunshinewzy.sunnybot.putElementInSet
import io.github.sunshinewzy.sunnybot.removeElementInSet
import io.github.sunshinewzy.sunnybot.sendMsg
import io.github.sunshinewzy.sunnybot.sunnyScope
import io.github.sunshinewzy.sunnyflow.packet.SunnyFlowConnection
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Group
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

class CustomSunnyFlowConnection : SunnyFlowConnection {
    private val groups: MutableSet<Group> = hashSetOf()
    @Volatile
    var running: Boolean = false
        private set
    
    
    constructor(host: String, port: Int, password: ByteArray) : super(host, port, password)
    
    constructor(host: String, port: Int, password: String) : super(host, port, password)
    
    
    fun addGroup(group: Group) {
        groups += group
        groupToConnectionMap.putElementInSet(group, this)
    }
    
    fun removeGroup(group: Group) {
        groups -= group
        groupToConnectionMap.removeElementInSet(group, this)
    }
    
    
    suspend fun start(): Boolean {
        if(running) return true

        if(socket.isClosed) {
            try {
                connect()
            } catch (_: Exception) {
                return false
            }
        }

        running = true

        thread {
            try {
                while(true) {
                    if(!running) return@thread

                    val packet = read()
                    val text = packet.text
                    sunnyScope.launch {
                        groups.forEach { group ->
                            group.sendMsg(SCMinecraftTransmit.description, text)
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                stop()
            }
        }
        
        return true
    }
    
    fun stop() {
        try {
            disconnect()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        running = false
    }
    
    
    
    companion object {
        val groupToConnectionMap: MutableMap<Group, MutableSet<CustomSunnyFlowConnection>> = ConcurrentHashMap()
    }
    
}