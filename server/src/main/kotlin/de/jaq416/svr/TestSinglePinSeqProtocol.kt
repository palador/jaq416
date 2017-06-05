package de.jaq416.svr

import org.slf4j.LoggerFactory

class TestSinglePinSeqProtocol(
        val sequenceProvider: () -> PinSequence
) : AbstractProtocol() {
    override fun processDecodedMessage(
            request: Message,
            response: Message) {
        when (request.opCode) {
            BaseOp.init -> {
                log.info("Hello :)")
            }
            Op.getPinSeq -> {
                response.opCode = Op.getPinSeq
                val seq = sequenceProvider()
                response.data.put(seq.pinA.encUChar())
                response.data.put(seq.pinB.encUChar())
                response.data.putInt(seq.durationPinA.encSInt())
                response.data.putInt(seq.durationBreakA.encSInt())
                response.data.putInt(seq.durationPinB.encSInt())
                response.data.putInt(seq.durationBreakB.encSInt())
            }
        }
    }

    data class PinSequence(
            val pinA: Int,
            val pinB: Int,
            val durationPinA: Long,
            val durationBreakA: Long,
            val durationPinB: Long,
            val durationBreakB: Long)

    object Op {
        /**
         * Response:
         *
         * pinA                     | B
         * pinB                     | B
         * duration Pin A (millis)  | uint
         * duration break (millis)  | uint
         * duration Pin B (millis)  | uint
         * duration break (millis)  | uint
         *
         */
        val getPinSeq = 1
    }

    companion object {
        private val log = LoggerFactory.getLogger(TestSinglePinSeqProtocol::class.java)
    }
}