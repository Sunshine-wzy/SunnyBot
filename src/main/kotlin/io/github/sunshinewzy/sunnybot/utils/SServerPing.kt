package io.github.sunshinewzy.sunnybot.utils

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.toPlainText
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import studio.trc.minecraft.serverpinglib.API.MCServerSocket
import studio.trc.minecraft.serverpinglib.Protocol.ProtocolVersion
import java.io.ByteArrayInputStream
import java.lang.Exception

object SServerPing {
    suspend fun Contact.pingServer(serverIp: String, isDisplayMod: Boolean = false): Message {
        val args = serverIp.split(":")
        if(args.isEmpty() || args.size > 2)
            return "".toPlainText()
        
        val ip = args[0]
        var port = 25565
        if(args.size == 2)
            port = args[1].toInt()
        
        val socket = MCServerSocket.getInstance(ip, port) ?: return "�޷����ӵ�������".toPlainText()
        
        var protocolVersion = ProtocolVersion.v1_8_X
        var status = socket.getStatus(protocolVersion) ?: return "�޷���ȡ������״̬".toPlainText()
        if(!status.isMCServer)
            return "�ⲻ��һ��MineCraft������".toPlainText()
        
        val version = status.version ?: return "��ȡ�������汾ʧ�ܣ�".toPlainText()
        protocolVersion = when {
            version.contains("1.11") -> ProtocolVersion.v1_11
            version.contains("1.12") -> ProtocolVersion.v1_12_2
            version.contains("1.13") || version.contains("1.14") || version.contains("1.15") || version.contains("1.16") -> ProtocolVersion.v1_13
            else -> ProtocolVersion.v1_8_X
        }
        
        val exactStatus = socket.getStatus(protocolVersion)
        if(exactStatus != null && exactStatus.isMCServer && exactStatus.version != null) status = exactStatus
        
        var res =  """
            
            ������IP: $serverIp
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
                        res += "[ " + it.modId + " ] v" + it.version + "\n"
                    }
                }
                else{
                    res += "\n>> ${info.modList.size} Mods\n" +
                        "(Tip: ���� /zt m ����ʾ��ϸmod��Ϣ)"
                }
            }
        }
        
        val text = res.toPlainText()
        
        try {
            val icon = status.icon ?: return text
            val imageBytes = icon.imageBytes ?: return text
            val inputStream = ByteArrayInputStream(imageBytes)
            val image = inputStream.uploadAsImage(this)

            return image + text
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        
        return text
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