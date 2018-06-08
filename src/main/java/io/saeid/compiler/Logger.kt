package io.saeid.compiler

/**
 * @author Saeed Masoumi (s-masoumi@live.com)
 */

object Logger {

    var debug = true

    fun log(msg: String) {
        if (debug)
            println(msg)
    }
}