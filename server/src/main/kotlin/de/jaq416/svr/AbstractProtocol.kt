package de.jaq416.svr

abstract class AbstractProtocol {

    private val request = Message()
    private val response = Message()

    fun processRequest(
            dataIn: ByteArray,
            dataOut: ByteArray) {

        // build request
        val opCode = dataIn[Offset.opCode].decUChar()
        request.opCode = opCode
        request.data.clear()
        request.data.put(dataIn, Offset.dataStart, msgLength - Offset.dataStart)
        request.data.flip()

        // prepare response
        response.opCode = 0
        response.data.clear()

        processDecodedMessage(request, response)

        // move response into dataOut
        dataOut.fill(0.encUChar())
        response.data.flip()
        dataOut[Offset.opCode] = response.opCode.encUChar()
        System.arraycopy(response.data.array(), 0, dataOut, Offset.dataStart, response.data.limit())
    }

    abstract protected fun processDecodedMessage(
            request: Message,
            response: Message)

    data class Message(
            var opCode: Int = 0,
            val data: java.nio.ByteBuffer = java.nio.ByteBuffer.allocate(
                    de.jaq416.svr.AbstractProtocol.Companion.msgLength - de.jaq416.svr.AbstractProtocol.Offset.dataStart)
    )

    companion object {
        val msgLength = 65
    }

    object Offset {
        val opCode = 0
        val dataStart = 1
    }

    object BaseOp {
        val init = 255
    }
}