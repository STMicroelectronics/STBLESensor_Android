package com.st.BlueSTSDK.Utils

import android.util.Log
import java.io.ByteArrayOutputStream
import com.st.BlueSTSDK.Debug
import kotlin.math.max

class STL2TransportProtocol {
    private var currentMessage: ByteArrayOutputStream? = null
    private var maxPayLoadSize =20
    fun decapsulate(byteCommand: ByteArray): ByteArray? {
        if (byteCommand[0] == TP_START_PACKET) {
            currentMessage = ByteArrayOutputStream().apply {
                write(byteCommand, 1, byteCommand.size - 1)
            }
        } else if (byteCommand[0] == TP_START_END_PACKET) {
            currentMessage = ByteArrayOutputStream().apply {
                write(byteCommand, 1, byteCommand.size - 1)
            }
            //Log.e("STL2TransportProtocol","currentMessage: " + byteToString(currentMessage.toByteArray()));
            return currentMessage!!.toByteArray()
        } else if (byteCommand[0] == TP_MIDDLE_PACKET) {
            currentMessage?.write(byteCommand, 1, byteCommand.size - 1)
        } else if (byteCommand[0] == TP_END_PACKET) {
            if (currentMessage != null) {
                currentMessage!!.write(byteCommand, 1, byteCommand.size - 1)
                //Log.e("STL2TransportProtocol","currentMessage: " + byteToString(currentMessage.toByteArray()));
                return currentMessage!!.toByteArray()
            }
        }
        return null
    }

    private fun toBytes(s: Short): ByteArray {
        return byteArrayOf((s.toInt() and 0x00FF).toByte(), ((s.toInt() and 0xFF00) shr (8)).toByte())
    }

    fun setMaxPayLoadSize(maxPayLoad: Int) {maxPayLoadSize = maxPayLoad}

    fun getMaxPayLoadSize() = maxPayLoadSize

    fun encapsulate(string: String?): ByteArray {
        val byteCommand = Debug.stringToByte(string)
        var head = TP_START_PACKET
        val baos = ByteArrayOutputStream()
        var cnt = 0
        val codedDataLength = byteCommand.size
        val mtuSize = maxPayLoadSize
        while (cnt < codedDataLength) {
            var size = Math.min(mtuSize - 1, codedDataLength - cnt)
            if (codedDataLength - cnt <= mtuSize - 1) {
                head = if (cnt == 0) {
                    if(codedDataLength - cnt <= mtuSize - 3) {
                        TP_START_END_PACKET
                    } else {
                        TP_START_PACKET
                    }
                } else {
                    TP_END_PACKET
                }
            }
            when (head) {
                TP_START_PACKET -> {

                    /*First part of a packet*/baos.write(head.toInt())
                    baos.write(toBytes(codedDataLength.toShort()).reversedArray())
                    baos.write(byteCommand, 0, mtuSize - 3)
                    size = mtuSize - 3
                    head = TP_MIDDLE_PACKET
                }
                TP_START_END_PACKET -> {

                    /*First and last part of a packet*/baos.write(head.toInt())
                    baos.write(toBytes(codedDataLength.toShort()).reversedArray())
                    baos.write(byteCommand, 0, codedDataLength)
                    size = codedDataLength
                    head = TP_START_PACKET
                }
                TP_MIDDLE_PACKET -> {

                    /*Central part of a packet*/baos.write(head.toInt())
                    baos.write(byteCommand, cnt, mtuSize - 1)
                }
                TP_END_PACKET -> {

                    /*Last part of a packet*/baos.write(head.toInt())
                    baos.write(byteCommand, cnt, codedDataLength - cnt)
                    head = TP_START_PACKET
                }
            }
            /*length variables update*/cnt += size
        }
        //Log.i("STL2TransportProtocol","baos.size="+baos.size()+"cnt="+cnt)
        return baos.toByteArray()
    }

    fun encapsulate(byteCommand: ByteArray): ByteArray {
        var head = TP_START_PACKET
        val baos = ByteArrayOutputStream()
        var cnt = 0
        val codedDataLength = byteCommand.size
        val mtuSize = maxPayLoadSize
        while (cnt < codedDataLength) {
            var size = Math.min(mtuSize - 1, codedDataLength - cnt)
            if (codedDataLength - cnt <= mtuSize - 1) {
                head = if (cnt == 0) {
                    if(codedDataLength - cnt <= mtuSize - 3) {
                        TP_START_END_PACKET
                    } else {
                        TP_START_PACKET
                    }
                } else {
                    TP_END_PACKET
                }
            }
            when (head) {
                TP_START_PACKET -> {
                    //Log.i("STL2TransportProtocol","TP_START_PACKET")
                    /*First part of a packet*/baos.write(head.toInt())
                    baos.write(toBytes(codedDataLength.toShort()).reversedArray())
                    baos.write(byteCommand, 0, mtuSize - 3)
                    size = mtuSize - 3
                    head = TP_MIDDLE_PACKET
                }
                TP_START_END_PACKET -> {
                    //Log.i("STL2TransportProtocol","TP_START_END_PACKET")
                    /*First and last part of a packet*/baos.write(head.toInt())
                    baos.write(toBytes(codedDataLength.toShort()).reversedArray())
                    baos.write(byteCommand, 0, codedDataLength)
                    size = codedDataLength
                    head = TP_START_PACKET
                }
                TP_MIDDLE_PACKET -> {
                    //Log.i("STL2TransportProtocol","TP_MIDDLE_PACKET")
                    /*Central part of a packet*/baos.write(head.toInt())
                    baos.write(byteCommand, cnt, mtuSize - 1)
                }
                TP_END_PACKET -> {
                    //Log.i("STL2TransportProtocol","TP_END_PACKET")
                    /*Last part of a packet*/baos.write(head.toInt())
                    baos.write(byteCommand, cnt, codedDataLength - cnt)
                    head = TP_START_PACKET
                }
            }
            /*length variables update*/cnt += size
            //Log.i("STL2TransportProtocol","baos.size="+baos.size()+"cnt="+cnt)
        }
        //Log.i("STL2TransportProtocol","End baos.size="+baos.size()+"cnt="+cnt)
        return baos.toByteArray()
    }

    companion object {
        private const val TP_START_PACKET = 0x00.toByte()
        private const val TP_START_END_PACKET = 0x20.toByte()
        private const val TP_MIDDLE_PACKET = 0x40.toByte()
        private const val TP_END_PACKET = 0x80.toByte()
    }
}