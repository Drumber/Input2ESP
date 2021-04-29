package com.github.drumber.input2esp.backend.network

import android.util.Log
import com.github.drumber.input2esp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Simple TCP client with error handling. Use the [Observable.addObserver] method to get notified about
 *
 *     - connecting and connected state</li>
 *     - failed connection
 *     - connection lost
 *     - disconnected state
 *     - server message receive events
 *
 * To send messages to the server use the [write] method.
 * This client can be re-used. You need to disconnect if you want to re-connect to the server.
 */
open class NetworkClient(var hostname: String, var port: Int, val timeout: Int = 5000): Observable() {
    private val TAG = "NetworkClient"

    private var socket: Socket? = null
    private var outWriter: PrintWriter? = null
    private var inReader: BufferedReader? = null

    private val run = AtomicBoolean(false)

    @Throws(NetworkException::class)
    suspend fun connect() {
        if(isConnected()) return

        val address = try {
            InetSocketAddress(hostname, port)
        } catch (e: Exception) {
            throw NetworkException(R.string.error_invalid_address_port)
        }

        try {
            fireEvent(NetworkEvent(NetworkClientState.CONNECTING))

            socket = Socket()
            socket?.connect(address, timeout)
            run.set(true)

            outWriter = PrintWriter(BufferedWriter(OutputStreamWriter(socket?.getOutputStream())), false)
            inReader = BufferedReader(InputStreamReader(socket?.getInputStream()))

            Log.d(TAG, "Client connected to ${socket?.remoteSocketAddress.toString()}")
            fireEvent(NetworkEvent(NetworkClientState.CONNECTED))

            // start listening for incoming messages
            listenForInput()

        } catch (e: Exception) {
            Log.e(TAG, "Error while connecting to server: ${address.hostName}:${address.port}", e)
            close()
            fireEvent(NetworkEvent(NetworkClientState.FAILED, e))
        }
    }

    protected suspend fun listenForInput() = withContext(Dispatchers.IO) {
        try {
            while (inReader != null && run.get()) {
                val serverMessage = inReader?.readLine()
                    ?: throw IOException("InputStream cannot read data from server.")

                Log.d(TAG, "Received message from server: $serverMessage")
                serverMessage?.let {
                    fireEvent(NetworkEvent(NetworkClientState.MESSAGE_RECEIVED, it))
                }
            }
        } catch (e: SocketException) {
            // do nothing, socket is already closed
        } catch (e: Exception) {
            Log.e(TAG, "Error while listening for data.", e)
            close()
            fireEvent(NetworkEvent(NetworkClientState.CONNECTION_LOST, e))
        }
    }

    protected fun close() {
        run.set(false) // make sure the listening loop is stopped
        try {
            outWriter?.let {
                it.flush()
                it.close()
            }
            outWriter = null
            inReader?.close()
            inReader = null
            socket?.close()
            socket = null
        } catch (e: Exception) {
            Log.e(TAG, "Error while closing connection.", e)
        }
    }

    fun write(data: String) {
        outWriter?.let {
            it.println(data)
            it.flush()
        }
    }

    fun disconnect() {
        if(isConnected()) {
            run.set(false)
            close()
            Log.d(TAG, "Client disconnected.")
            fireEvent(NetworkEvent(NetworkClientState.DISCONNECTED))
        }
    }

    fun isConnected() = socket != null

    protected fun fireEvent(event: NetworkEvent) {
        setChanged()
        notifyObservers(event)
        clearChanged()
    }

}
