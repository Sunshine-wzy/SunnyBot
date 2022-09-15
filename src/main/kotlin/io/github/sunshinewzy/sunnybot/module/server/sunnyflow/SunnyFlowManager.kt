package io.github.sunshinewzy.sunnybot.module.server.sunnyflow

import io.github.sunshinewzy.sunnybot.commands.SCMinecraftTransmit
import io.github.sunshinewzy.sunnybot.objects.SSavePlayer
import io.github.sunshinewzy.sunnybot.putElement
import io.github.sunshinewzy.sunnybot.sendMsg
import io.github.sunshinewzy.sunnybot.sunnyBot
import io.github.sunshinewzy.sunnybot.sunnyScope
import io.github.sunshinewzy.sunnyflow.packet.SunnyFlowConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Group
import java.util.concurrent.ConcurrentHashMap

object SunnyFlowManager {
    private val cacheMap: MutableMap<String, SunnyFlowConnection> = ConcurrentHashMap()
    private val connectionToGroupMap: MutableMap<SunnyFlowConnection, MutableList<Group>> = ConcurrentHashMap()
    private val groupToConnectionMap: MutableMap<Group, MutableList<SunnyFlowConnection>> = ConcurrentHashMap()
    

    fun open(hostname: String, port: Int, password: String): SunnyFlowConnection? {
        val ip = "$hostname:$port"
        cacheMap[ip]?.let { return it }

        try {
            val connection = SunnyFlowConnection(hostname, port, password)
            cacheMap[ip] = connection
            return connection
        } catch (_: Exception) {}

        return null
    }

    fun open(ip: String, password: String): SunnyFlowConnection? {
        cacheMap[ip]?.let { return it }

        val strList = ip.split(':')
        if(strList.size != 2) return null

        try {
            val connection = SunnyFlowConnection(strList[0], strList[1].toInt(), password)
            cacheMap[ip] = connection
            return connection
        } catch (_: Exception) {}

        return null
    }


    fun init() {
        SSavePlayer.sPlayerMap.forEach { (userId, sPlayer) ->
            sPlayer.minecraftTransmit.serverMap.forEach { (symbol, serverData) ->
                sunnyScope.launch(Dispatchers.IO) {
                    open(serverData.ip, serverData.password)?.let { connection ->
                        serverData.groupMap.forEach { (groupId, groupData) -> 
                            sunnyBot.getGroup(groupId)?.let {
                                connectionToGroupMap.putElement(connection, it)
                                groupToConnectionMap.putElement(it, connection)
                            }
                        }
                        
                        connection.startListen()
                    }
                }
            }
        }
    }
    
    fun transmit(group: Group, message: String) {
        sunnyScope.launch(Dispatchers.IO) {
            groupToConnectionMap[group]?.forEach { connection ->
                connection.message(message)
            }
        }
    }
    
    
    private fun SunnyFlowConnection.startListen() {
        sunnyScope.launch(Dispatchers.IO) { 
            try {
                while(true) {
                    val packet = read()
                    val text = packet.text
                    connectionToGroupMap[this@startListen]?.forEach { group ->
                        group.sendMsg(SCMinecraftTransmit.description, text)
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
    
}