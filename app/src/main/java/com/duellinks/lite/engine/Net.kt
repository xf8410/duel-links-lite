package com.duellinks.lite.engine

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets

@Serializable
sealed interface NetMessage

@Serializable
data class StateMsg(val state: GameState) : NetMessage

@Serializable
data class ActionMsg(val action: Action) : NetMessage

class LanConnection {

    var onState: ((GameState) -> Unit)? = null
    var onAction: ((Action) -> Unit)? = null
    var onConnected: (() -> Unit)? = null
    var onError: ((String) -> Unit)? = null

    private var socket: Socket? = null
    private var out: DataOutputStream? = null
    private val json = Json { ignoreUnknownKeys = true }

    fun startHost(port: Int = 8765) {
        Thread {
            try {
                val server = ServerSocket(port)
                val s = server.accept()
                server.close()
                setup(s)
            } catch (e: Exception) {
                onError?.invoke(e.message ?: "host error")
            }
        }.start()
    }

    fun connect(host: String, port: Int = 8765) {
        Thread {
            try {
                val s = Socket(host, port)
                setup(s)
            } catch (e: Exception) {
                onError?.invoke(e.message ?: "connect error")
            }
        }.start()
    }

    private fun setup(s: Socket) {
        socket = s
        out = DataOutputStream(s.getOutputStream())
        onConnected?.invoke()
        Thread {
            try {
                val `in` = DataInputStream(s.getInputStream())
                while (true) {
                    val len = `in`.readInt()
                    if (len <= 0) break
                    val bytes = ByteArray(len)
                    `in`.readFully(bytes)
                    val text = bytes.toString(StandardCharsets.UTF_8)
                    val msg = json.decodeFromString<NetMessage>(text)
                    when (msg) {
                        is StateMsg -> onState?.invoke(msg.state)
                        is ActionMsg -> onAction?.invoke(msg.action)
                    }
                }
            } catch (e: Exception) {
                onError?.invoke(e.message ?: "read error")
            }
        }.start()
    }

    fun sendState(state: GameState) = send(StateMsg(state))
    fun sendAction(action: Action) = send(ActionMsg(action))

    private fun send(msg: NetMessage) {
        try {
            val text = json.encodeToString(msg)
            val bytes = text.toByteArray(StandardCharsets.UTF_8)
            synchronized(this) {
                out?.writeInt(bytes.size)
                out?.write(bytes)
                out?.flush()
            }
        } catch (e: Exception) {
            onError?.invoke(e.message ?: "send error")
        }
    }
}
