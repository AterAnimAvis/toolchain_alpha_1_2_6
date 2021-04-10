package utils

import java.io.FilterOutputStream
import java.io.OutputStream

class UnclosingOutputStream(stream: OutputStream) : FilterOutputStream(stream) {

    override fun close() {

    }

}