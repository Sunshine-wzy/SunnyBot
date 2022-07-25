package io.github.sunshinewzy.sunnybot.utils

import io.github.sunshinewzy.sunnybot.objects.RconData
import nl.vv32.rcon.Rcon
import java.util.concurrent.ConcurrentHashMap

object RconManager {
    private val rconMap = ConcurrentHashMap<String, Rcon>()
    
    
    fun open(hostname: String, port: Int, password: String): Rcon? {
        val ip = "$hostname:$port"
        rconMap[ip]?.let { return it }
        
        try {
            val rcon = Rcon.open(hostname, port)
            if(rcon.authenticate(password)) {
                rconMap[ip] = rcon
                return rcon
            }
        } catch (_: Exception) {}
        
        return null
    }
    
    fun open(ip: String, password: String): Rcon? {
        rconMap[ip]?.let { return it }
        
        val strList = ip.split(':')
        if(strList.size != 2) return null

        try {
            val rcon = Rcon.open(strList[0], strList[1].toInt())
            if(rcon.authenticate(password)) {
                rconMap[ip] = rcon
                return rcon
            }
        } catch (_: Exception) {}
        
        return null
    }
    
    fun open(data: RconData): Rcon? =
        open(data.ip, data.password)
    
}