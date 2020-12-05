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

        val info = status.modInfo
        if(info.hasModInfo()){
            if(info.hasMod()){
                if(isDisplayMod){
                    res += "\n>> ${info.modList.size} Mods:\n"
                    info.modList.forEach {
                        res += "[ " + it.modId + " ] �汾: v" + it.version + "\n"
                    }
                }
                else{
                    res += "\n>> ${info.modList.size} Mods\n" +
                        "(Tip: ���� /zt m ����ʾ��ϸmod��Ϣ)"
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