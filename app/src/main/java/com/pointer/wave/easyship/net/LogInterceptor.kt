package com.pointer.wave.easyship.net


import android.util.Log
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.io.IOException

class LogInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder().apply {
            addHeader("Content-Type", "application/json")
//            addHeader("Accept-Language", getLanguage())
//            addHeader("TimeZone", TimeZone.getDefault().id)
//            addHeader("Platform", "android")
//            addHeader("AppVer", appVersionName)
//            addHeader("DeviceInfo", BluetoothClass.Device.name)
//            addHeader("SysVer", BluetoothClass.Device.systemVersion)
        }
        val request = requestBuilder.build()

        // 请求日志打印
        Log.d("NET",
            "\n\t" +
                    """
            
            |────── Request ────────────────────────────────────────────────────────────────────────>
            | Method: @${request.method}
            |
            | Url: ${request.url}
            |
            | Headers----------------->
            | ${JsonUtils.format(request.headers.toString())}
            | <-----------------Headers
            |
            | Body----------------->
            | ${bodyToString(request)}
            | <-----------------Body
            |
            |────── Request ────────────────────────────────────────────────────────────────────────>
        """.trimMargin()
        )

        val startTime = System.currentTimeMillis()
        val response: Response = chain.proceed(request)
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        val mediaType = response.body!!.contentType()
        val content = response.body!!.string()
        val method: String = request.method
        if ("POST" == method) {
            val sb = StringBuilder()
            if (request.body is FormBody) {
                val body = request.body as FormBody
                for (i in 0 until body.size) {
                    sb.append(body.encodedName(i) + "=" + body.encodedValue(i) + ",")
                }
                sb.delete(sb.length - 1, sb.length)
                Log.d("NET","| RequestParams:{$sb}")
            }
        }

        // 响应日志打印
        Log.d("NET",
            """
            
            |<────── Response ────────────────────────────────────────────────────────────────────────
            | Status: ${response.code}
            |
            | Url: ${response.request.url}
            |
            | Headers----------------->
            | ${JsonUtils.format(response.headers.toString())}
            | <-----------------Headers
            |
            | Body----------------->
            | ${JsonUtils.format(content)}
            | <-----------------Body
            |
            | Time: ${duration}ms
            |
            |<────── Response ────────────────────────────────────────────────────────────────────────
        """.trimMargin()
        )
        return response.newBuilder()
            .body(content.toResponseBody(mediaType))
            .build()
    }

    private fun bodyToString(request: Request): String? {
        return try {
            val copy = request.newBuilder().build()
            val buffer = Buffer()
            if (copy.body == null) return ""
            copy.body!!.writeTo(buffer)
            JsonUtils.format(buffer.readUtf8())
        } catch (e: IOException) {
            "{\"err\": \"" + e.message + "\"}"
        }
    }
}

private object JsonUtils {


    fun format(json: String): String =
        if ((json.startsWith("{") && json.endsWith("}"))
            || (json.startsWith("[") && json.endsWith("]"))
        ) {
            formatJson(decodeUnicode(json))
        } else {
            json
        }

    /**
     * 格式化json字符串
     *
     * @param jsonStr 需要格式化的json串
     * @return 格式化后的json串
     */
    private fun formatJson(jsonStr: String?): String {
        if (null == jsonStr || "" == jsonStr) return ""
        val sb = StringBuilder()
        var last = '\u0000'
        var current = '\u0000'
        var indent = 0
        for (element in jsonStr) {
            last = current
            current = element
            //遇到{ [换行，且下一行缩进
            when (current) {
                '{', '[' -> {
                    sb.append(current)
                    sb.append('\n')
                    indent++
                    addIndentBlank(sb, indent)
                }
                //遇到} ]换行，当前行缩进
                '}', ']' -> {
                    sb.append('\n')
                    indent--
                    addIndentBlank(sb, indent)
                    sb.append(current)
                }
                //遇到,换行
                ',' -> {
                    sb.append(current)
                    if (last != '\\') {
                        sb.append('\n')
                        addIndentBlank(sb, indent)
                    }
                }
                else -> sb.append(current)
            }
        }
        return sb.toString()
    }

    /**
     * 添加space
     *
     * @param sb
     * @param indent
     */
    private fun addIndentBlank(sb: StringBuilder, indent: Int) {
        for (i in 0 until indent) {
            sb.append('\t')
        }
    }

    /**
     * http 请求数据返回 json 中中文字符为 unicode 编码转汉字转码
     *
     * @param theString
     * @return 转化后的结果.
     */
    private fun decodeUnicode(theString: String): String {
        var aChar: Char
        val len = theString.length
        val outBuffer = StringBuffer(len)
        var x = 0
        while (x < len) {
            aChar = theString[x++]
            if (aChar == '\\') {
                aChar = theString[x++]
                if (aChar == 'u') {
                    var value = 0
                    for (i in 0..3) {
                        aChar = theString[x++]
                        value = when (aChar) {
                            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> (value shl 4) + aChar.code - '0'.code
                            'a', 'b', 'c', 'd', 'e', 'f' -> (value shl 4) + 10 + aChar.code - 'a'.code
                            'A', 'B', 'C', 'D', 'E', 'F' -> (value shl 4) + 10 + aChar.code - 'A'.code
                            else -> throw IllegalArgumentException(
                                "Malformed   \\uxxxx   encoding."
                            )
                        }

                    }
                    outBuffer.append(value.toChar())
                } else {
                    when (aChar) {
                        't' -> aChar = '\t'
                        'r' -> aChar = '\r'
                        'n' -> aChar = '\n'
                        'f' -> aChar = '\u000C'
                    }
//                        aChar = '\f'
                    outBuffer.append(aChar)
                }
            } else
                outBuffer.append(aChar)
        }
        return outBuffer.toString()
    }
}