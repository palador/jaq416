package de.jaq416.svr

import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.ByteBuffer.allocate
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.atomic.AtomicBoolean

class Jaq416Server(
        val serverAddr: InetAddress,
        val serverPort: Int,
        private val protocolFactory: () -> AbstractProtocol
) {

    private val serverThread = Thread({ run() }, "Server $serverAddr:$serverPort")
    private var stopServer = AtomicBoolean(false)

    fun start() {
        serverThread.start()
    }

    fun stop() {
        log.info("Shut down server $serverAddr:$serverPort...")
        stopServer.set(true)
        serverThread.interrupt()
        serverThread.join(60 * 1000)

    }

    private fun run() {
        log.info("Start server @ $serverAddr:$serverPort")
        val sessions = arrayListOf<Session>()
        val outBuf = allocate(AbstractProtocol.msgLength)

        val selector = Selector.open()
        val ssChannel = ServerSocketChannel.open()
        ssChannel.socket().bind(InetSocketAddress(serverAddr, serverPort))
        ssChannel.configureBlocking(false)
        ssChannel.register(selector, SelectionKey.OP_ACCEPT)

        while (!stopServer.get()) {
            if (selector.select() > 0) {
                val selKeys = selector.selectedKeys()
                val iter = selKeys.iterator()
                iter.forEach { selKey ->
                    iter.remove()

                    when {
                        selKey.isAcceptable -> {
                            val ssChannel = selKey.channel() as ServerSocketChannel
                            val sChannel = ssChannel.accept()
                            val clientAddr = (sChannel.remoteAddress as InetSocketAddress).address!!
                            sChannel.configureBlocking(false)
                            sChannel.register(selKey.selector(), SelectionKey.OP_READ)
                            val existingSession = sessions.firstOrNull { it.clientAddr == clientAddr }
                            if (existingSession == null) {
                                log.info("Accepted connection to $clientAddr")
                                sessions += Session(clientAddr, protocolFactory())
                            } else {
                                log.info("Renewed connection to $clientAddr")
                                existingSession.inBuf.clear()
                            }
                        }

                        selKey.isReadable -> {
                            val sc = selKey.channel() as SocketChannel
                            val clientAddr = (sc.remoteAddress as InetSocketAddress).address!!
                            val session = sessions.firstOrNull { it.clientAddr == clientAddr }
                            if (session == null) {
                                log.error("Bad connection to $clientAddr")
                            } else {
                                var conClosed = false

                                val nRead = try {
                                    sc.read(session.inBuf)
                                } catch (e: java.io.IOException) {
                                    // The remote forcebibly closed the connection...
                                    log.error("Remote forcebily closed the connection: $clientAddr")
                                    -1
                                }

                                if (nRead == -1) {
                                    conClosed = true
                                } else if (!session.inBuf.hasRemaining()) {
                                    // check if data is fully read
                                    session.inBuf.clear()
                                    outBuf.clear()
                                    try {
                                        session.protocol.processRequest(session.inBuf.array(), outBuf.array())
                                        sc.write(outBuf)
                                    } catch (e: Exception) {
                                        log.error("Failed to write response...", e)
                                        conClosed = true
                                    }
                                }

                                if (conClosed) {
                                    sc.close()
                                    selKey.cancel()
                                    log.info("Close connection to $clientAddr")
                                }
                            }
                        }
                    }
                }
            }
        }

        log.info("Server $serverAddr:$serverPort is shut down")
    }

    private data class Session(
            val clientAddr: InetAddress,
            val protocol: AbstractProtocol
    ) {
        val inBuf = allocate(AbstractProtocol.msgLength)
    }

    companion object {
        private val log = LoggerFactory.getLogger(Jaq416Server::class.java)
    }
}