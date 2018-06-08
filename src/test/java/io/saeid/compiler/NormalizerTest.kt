package io.saeid.compiler

import junit.framework.TestCase.assertEquals
import org.junit.Test

/**
 * @author Saeed Masoumi (s-masoumi@live.com)
 */
class NormalizerTest {

    @Test
    fun `remove whitespace`() {
        assertEquals("sa ee d", normalize("sa    ee  d "))
    }

    @Test
    fun `remove comments`() {
        assertEquals("", normalize("/* */"))
        assertEquals("", normalize("/**/"))
        //Multi line comment is not supported
//        assertEquals("", normalize("/*\n*/"))
//        assertEquals("sa eed", normalize("sa/*  sa \n*/ eed"))
        assertEquals("", normalize("/* foo bar */"))
        assertEquals("saeed", normalize("saee/* */d"))
    }

    @Test
    fun `remove new line`() {
        assertEquals("code code", normalize("code\ncode"))
    }
}