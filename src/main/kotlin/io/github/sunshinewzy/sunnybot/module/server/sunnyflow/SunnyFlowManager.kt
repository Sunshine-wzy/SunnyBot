package io.github.sunshinewzy.sunnybot.module.server.sunnyflow

import io.github.sunshinewzy.sunnybot.commands.SCMinecraftTransmit
import io.github.sunshinewzy.sunnybot.objects.SSavePlayer
import io.github.sunshinewzy.sunnybot.sendMsg
import io.github.sunshinewzy.sunnybot.sunnyBot
import io.github.sunshinewzy.sunnybot.sunnyScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Group
import java.net.SocketException
import java.util.concurrent.ConcurrentHashMap

object SunnyFlowManager {
    private val cacheMap: MutableMap<String, CustomSunnyFlowConnection> = ConcurrentHashMap()
    

    fun open(hostname: String, port: Int, password: String): CustomSunnyFlowConnection? {
        val ip = "$hostname:$port"
        cacheMap[ip]?.let { return it }

        try {
            val connection = CustomSunnyFlowConnection(hostname, port, password)
            cacheMap[ip] = connection
            return connection
        } catch (_: Exception) {}

        return null
    }

    fun open(ip: String, password: String): CustomSunnyFlowConnection? {
        cacheMap[ip]?.let { return it }

        val strList = ip.split(':')
        if(strList.size != 2) return null

        try {
            val connection = CustomSunnyFlowConnection(strList[0], strList[1].toInt(), password)
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
                                connection.addGroup(it)
                            }
                        }
                        
                        connection.start()
                    }
                }
            }
        }
    }
    
    fun transmit(group: Group, message: String) {
        sunnyScope.launch(Dispatchers.IO) {
            CustomSunnyFlowConnection.groupToConnectionMap[group]?.forEach { connection ->
                if(connection.running) {
                    try {
                        connection.message(message)
                    } catch (ex: SocketException) {
                        connection.stop()
                        group.sendMsg(
                            SCMinecraftTransmit.description,
                            "服务器 ${connection.socket.remoteSocketAddress} 连接失败\n" +
                                "请输入 /mct connect 以重新连接"
                        )
                    } catch (_: Exception) {}
                }
            }
        }
    }
    
}