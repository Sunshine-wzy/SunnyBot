package io.github.sunshinewzy.sunnybot.utils

import studio.trc.minecraft.serverpinglib.API.MCServerSocket
import studio.trc.minecraft.serverpinglib.Protocol.ProtocolVersion

object SServerPing {
    fun pingServer(serverIp: String, isDisplayMod: Boolean = false): String {
        val args = serverIp.split(":")
        if(args.isEmpty() || args.size > 2)
            return ""
        
        val ip = args[0]
        var port = 25565
        if(args.size == 2)
            port = args[1].toInt()
        
        val socket = MCServerSocket.getInstance(ip, port) ?: return "无法连接到服务器"

        val status = socket.getStatus(ProtocolVersion.v1_8_X) ?: return "无法获取服务器状态"
        if(!status.isMCServer)
            return "这不是一个MineCraft服务器"
        
        var res =  """
            服务器IP: ${socket.ip}:${socket.port}
            服务器版本: ${status.version}
            当前在线人数: ${status.onlinePlayers}
            最大在线人数: ${status.maxPlayers}
            
            MOTD:
            ${status.motd.line1MotdText}
            ${status.motd.line2MotdText}
            
        """.trimIndent()

        val info = status.modInfo
        if(info.hasModInfo()){
            if(info.hasMod()){
                if(isDisplayMod){
                    res += "\n>> ${info.modList.size} Mods:\n"
                    info.modList.forEach {
                        res += "[ " + it.modId + " ] 版本: v" + it.version + "\n"
                    }
                }
                else{
                    res += "\n>> ${info.modList.size} Mods\n" +
                        "(Tip: 输入 /zt m 以显示详细mod信息)"
                }
            }
        }
        
        return res
    }
    
    fun checkServer(serverIp: String): Boolean {
        val args = serverIp.split(":")
        if(args.isEmpty() || args.size > 2)
            return false

        val ip = args[0]
        var port = 25565
        if(args.size == 2)
            port = args[1].toInt()

        val socket = MCServerSocket.getInstance(ip, port) ?: return false

        val status = socket.getStatus(ProtocolVersion.v1_8_X) ?: return false
        if(!status.isMCServer)
            return false
        
        return true
    }
}