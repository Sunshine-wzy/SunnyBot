package io.github.sunshinewzy.sunnybot.objects

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder


/**
 * @param url �ӿڵ�ַ
 */

class SRequest(private val url: String) {
    fun result(serverAddr: String, showFavicon: Int): RosellemcServerInfo {
        //params���ڴ洢Ҫ����Ĳ���
        val params = java.util.HashMap<String, Any>()
        params["server_addr"] = serverAddr
        params["show_favicon"] = showFavicon
        //����httpRequest���������������Ҫ���������ַ���������������
        val strRequest = httpRequest(params)
        //�����ص�JSON���ݲ�����
//        val resultType = object : TypeToken<RosellemcServerInfo>() {}.type
        return Gson().fromJson(strRequest, RosellemcServerInfo::class.java)
    }
    
    private fun httpRequest(params: Map<String, Any>): String {
        //buffer ���ڽ��շ��ص��ַ�
        val buffer = StringBuffer()
        try {
            //����URL���������ַ����ȫ������urlEncode()�������ڰ�params��Ĳ�����ȡ����
            val theURL = URL(url + "?" + urlEncode(params))
            //��http����
            val httpUrlConn: HttpURLConnection = theURL.openConnection() as HttpURLConnection
            httpUrlConn.doInput = true
            httpUrlConn.requestMethod = "GET"
            httpUrlConn.connect()

            //�������
            val inputStream: InputStream? = httpUrlConn.inputStream
            val inputStreamReader = InputStreamReader(inputStream!!, "utf-8")
            val bufferedReader = BufferedReader(inputStreamReader)

            //��bufferReader��ֵ���ŵ�buffer��
            var str: String?
            while ((bufferedReader.readLine().also { str = it }) != null){
                buffer.append(str)
            }
            //�ر�bufferReader��������
            bufferedReader.close()
            inputStreamReader.close()
            inputStream.close()
            //�Ͽ�����
            httpUrlConn.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //�����ַ���
        return buffer.toString()
    }
    
    private fun urlEncode(data: Map<String, Any>): String {
        //��map��Ĳ�������� showapi_appid=###&showapi_sign=###&������
        val sb = StringBuilder()
        for ((key, value) in data.entries) {
            try {
                sb.append(key).append("=").append(URLEncoder.encode(value.toString() + "", "UTF-8")).append("&")
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
        }
        return sb.toString()
    }
}