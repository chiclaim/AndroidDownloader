package com.chiclaim.android.updater.util

import java.io.InputStream
import java.security.MessageDigest

/**
 *
 * @author by chiclaim@google.com
 */
internal object MD5 {
    private const val LO_BYTE: Int = 0x0f
    private const val MOVE_BIT: Int = 4
    private const val HI_BYTE: Int = 0xf0
    private val HEX_DIGITS = arrayOf(
        "0", "1", "2", "3", "4", "5",
        "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"
    )

    /**
     * 转换字节数组为16进制字串
     *
     * @param b 字节数组
     * @return 16进制字串
     */
    private fun byteArrayToHexString(b: ByteArray): String {
        val buf = StringBuilder()
        for (value in b) {
            buf.append(byteToHexString(value))
            // 也可以使用下面的方式。 X 表示大小字母，x 表示小写字母，对应的是 HEX_DIGITS 中字母
            // buf.append(String.format("%02X", value));
        }
        return buf.toString()
    }

    /**
     * 字节转成字符.
     *
     * @param b 原始字节.
     * @return 转换后的字符.
     */
    private fun byteToHexString(b: Byte): String {
        return HEX_DIGITS[(b.toInt() and HI_BYTE) shr MOVE_BIT] + HEX_DIGITS[b.toInt() and LO_BYTE]
    }

    /**
     * 进行加密.
     *
     * @return 加密后的结果.
     */
    fun md5(origin: ByteArray): String {
        val md = MessageDigest.getInstance("MD5")
        return byteArrayToHexString(md.digest(origin))
    }

    fun md5(str: String): String {
        return md5(str.toByteArray())
    }

    /**
     * 对输入流生成校验码.
     * @param in 输入流.
     * @return 生成的校验码.
     */
    fun md5(input: InputStream): String {
        val md = MessageDigest.getInstance("MD5")
        val buffer = ByteArray(1024 * 1024)
        var len: Int
        while (input.read(buffer).also { len = it } > 0) {
            md.update(buffer, 0, len)
        }
        return byteArrayToHexString(md.digest())
    }
}