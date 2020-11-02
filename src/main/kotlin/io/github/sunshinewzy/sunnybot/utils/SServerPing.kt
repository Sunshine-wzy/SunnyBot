package io.github.sunshinewzy.sunnybot.utils

import studio.trc.minecraft.serverpinglib.API.MCServerSocket
import studio.trc.minecraft.serverpinglib.Protocol.ProtocolVersion

object SServerPing {
    suspend fun pingServer(serverIp: String): String {
        val args = serverIp.split(":")
        if(args.isEmpty() || args.size > 2)
            return ""
        
        val ip = args[0]
        var port = 25565
        if(args.size == 2)
            port = args[1].toInt()
        
        val socket = MCServerSocket.getInstance(ip, port) ?: return "�޷����ӵ�������"

        val status = socket.getStatus(ProtocolVersion.v1_8_X) ?: return "�޷���ȡ������״̬"
        if(!status.isMCServer)
            return "�ⲻ��һ��MineCraft������"
        
        var res =  """
            ������IP: ${socket.ip}:${socket.port}
            �������汾: ${status.version}
            ��ǰ��������: ${status.onlinePlayers}
            �����������: ${status.maxPlayers}
            
            MOTD:
            ${status.motd.line1MotdText}
            ${status.motd.line2MotdText}
            
        """.trimIndent()

        if(status.modInfo.hasModInfo()){
            if(status.modInfo.hasMod()){
                res += "\n>>Mod:\n"
                status.modInfo.modList.forEach { 
                    res += "[ " + it.modId + " ] �汾: v" + it.version + "\n"
                }
            }
        }
        
        return res
    }
    
    suspend fun checkServer(serverIp: String): Boolean {
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