package de.jaq416.svr

import org.slf4j.LoggerFactory
import java.net.InetAddress

fun main(args: Array<String>) {
    val server = Jaq416Server(InetAddress.getByName("192.168.178.25"), 2233) {
        TestSinglePinSeqProtocol() {
            TestSinglePinSeqProtocol.PinSequence(
                    pinA = 3,
                    pinB = 4,
                    durationPinA = 20,
                    durationBreakA = 1000,
                    durationPinB = 500,
                    durationBreakB = 2000
            )
        }
    }
    server.start()
}
