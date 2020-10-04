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
 * @param url 接口地址
 */

class SRequest(private val url: String) {
    fun result(serverAddr: String, showFavicon: Int): RosellemcServerInfo {
        //params用于存储要请求的参数
        val params = java.util.HashMap<String, Any>()
        params["server_addr"] = serverAddr
        params["show_favicon"] = showFavicon
        //调用httpRequest方法，这个方法主要用于请求地址，并加上请求参数
        val strRequest = httpRequest(params)
        //处理返回的JSON数据并返回
//        val resultType = object : TypeToken<RosellemcServerInfo>() {}.type
        return Gson().fromJson(strRequest, RosellemcServerInfo::class.java)
    }
    
    private fun httpRequest(params: Map<String, Any>): String {
        //buffer 用于接收返回的字符
        val buffer = StringBuffer()
        try {
            //建立URL，把请求地址给补全，其中urlEncode()方法用于把params里的参数给取出来
            val theURL = URL(url + "?" + urlEncode(params))
            //打开http连接
            val httpUrlConn: HttpURLConnection = theURL.openConnection() as HttpURLConnection
            httpUrlConn.doInput = true
            httpUrlConn.requestMethod = "GET"
            httpUrlConn.connect()

            //获得输入
            val inputStream: InputStream? = httpUrlConn.inputStream
            val inputStreamReader = InputStreamReader(inputStream!!, "utf-8")
            val bufferedReader = BufferedReader(inputStreamReader)

            //将bufferReader的值给放到buffer里
            var str: String?
            while ((bufferedReader.readLine().also { str = it }) != null){
                buffer.append(str)
            }
            //关闭bufferReader和输入流
            bufferedReader.close()
            inputStreamReader.close()
            inputStream.close()
            //断开连接
            httpUrlConn.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //返回字符串
        return buffer.toString()
    }
    
    private fun urlEncode(data: Map<String, Any>): String {
        //将map里的参数变成像 showapi_appid=###&showapi_sign=###&的样子
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