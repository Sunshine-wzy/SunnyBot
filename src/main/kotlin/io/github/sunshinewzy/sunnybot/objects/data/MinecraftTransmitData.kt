package io.github.sunshinewzy.sunnybot.objects.data

import kotlinx.serialization.Serializable

@Serializable
class MinecraftTransmitData {
    val serverMap: MutableMap<String, MinecraftTransmitServerData> = hashMapOf()
    var select: String = ""
    
    
    fun getSelectedServerData(): MinecraftTransmitServerData? {
        if(select.isEmpty()) return null
        return serverMap[select]
    }
    
    fun addServer(symbol: String, ip: String, password: String) {
        serverMap[symbol] = MinecraftTransmitServerData(ip, password)
        select = symbol
    }
}

@Serializable
class MinecraftTransmitServerData(
    val ip: String,
    val password: String
) {
    val groupMap: MutableMap<Long, MinecraftTransmitServerGroupData> = hashMapOf()
}

@Serializable
class MinecraftTransmitServerGroupData(
    val id: Long
) {
    var period: Long = 15 * 60 * 1000
    var number: Int = 10
    var prefix: String = ":"
    
}