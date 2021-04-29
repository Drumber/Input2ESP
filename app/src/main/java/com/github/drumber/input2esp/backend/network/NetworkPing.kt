package com.github.drumber.input2esp.backend.network

import java.net.InetSocketAddress
import java.net.Socket

object NetworkPing {

    fun isReachable(hostname: String, port: Int, timeout: Int = 2000): Boolean {
        val socket = Socket()
        return try {
            val address = InetSocketAddress(hostname, port)
            socket.connect(address, timeout)
            //InetAddress.getByName(hostname).isReachable(timeout)
            true
        }  catch (e: Exception) {
            //e.printStackTrace()
            false
        } finally {
            socket.close()
        }
    }

}