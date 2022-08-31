package io.github.sunshinewzy.sunnybot.module.server.rcon

import io.github.sunshinewzy.sunnybot.objects.RconData
import java.util.concurrent.ConcurrentHashMap

object RconManager {
    private val rconMap = ConcurrentHashMap<String, CustomRcon>()
    
    
    fun open(hostname: String, port: Int, password: String): CustomRcon? {
        val ip = "$hostname:$port"
        rconMap[ip]?.let { return it }
        
        try {
            val rcon = CustomRcon(hostname, port, password)
            rconMap[ip] = rcon
            return rcon
        } catch (_: Exception) {}
        
        return null
    }
    
    fun open(ip: String, password: String): CustomRcon? {
        rconMap[ip]?.let { return it }
        
        val strList = ip.split(':')
        if(strList.size != 2) return null

        try {
            val rcon = CustomRcon(strList[0], strList[1].toInt(), password)
            rconMap[ip] = rcon
            return rcon
        } catch (_: Exception) {}
        
        return null
    }
    
    fun open(data: RconData): CustomRcon? =
        open(data.ip, data.password)
    
}