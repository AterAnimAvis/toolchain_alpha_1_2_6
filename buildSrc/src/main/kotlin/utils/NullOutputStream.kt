package utils

import java.io.IOException
import java.io.OutputStream

object NullOutputStream : OutputStream() {

    @Throws(IOException::class)
    override fun write(b: Int) {

    }

}