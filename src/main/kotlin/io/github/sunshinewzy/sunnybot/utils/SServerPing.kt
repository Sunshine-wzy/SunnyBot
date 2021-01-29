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
        
        var protocolVersion = ProtocolVersion.v1_8_X
        var status = socket.getStatus(protocolVersion) ?: return "�޷���ȡ������״̬"
        if(!status.isMCServer)
            return "�ⲻ��һ��MineCraft������"
        
        val version = status.version.split(".")
        if(version.size >= 2){
            val ver = version[1]

            when {
                ver.contains("11") -> protocolVersion = ProtocolVersion.v1_11
                ver.contains("12") -> protocolVersion = ProtocolVersion.v1_12_2
                ver.contains("13") -> protocolVersion = ProtocolVersion.v1_13
                ver.contains("14") -> protocolVersion = ProtocolVersion.v1_14
                ver.contains("15") -> protocolVersion = ProtocolVersion.v1_15
                ver.contains("16") -> protocolVersion = ProtocolVersion.v1_15_2
            }
        }
        
        val exactStatus = socket.getStatus(protocolVersion)
        if(exactStatus != null && exactStatus.isMCServer && exactStatus.version != null) status = exactStatus
        
        var res =  """
            ������IP: ${socket.ip}:${socket.port}
            �������汾: ${status.version}
            ��ǰ��������: ${status.onlinePlayers}
            �����������: ${status.maxPlayers}
            Э��汾: ${status.protocolVersion}
            
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