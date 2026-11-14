/*
 * ViewMD — Enhanced Markdown preview for Android Studio and IntelliJ-based IDEs
 * Copyright (C) 2026 Mina
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.mina.viewmd.util

import org.junit.Assert.assertEquals
import org.junit.Test

class HtmlUtilsTest {

    @Test
    fun testEscapeAmpersand() {
        assertEquals("A &amp; B", HtmlUtils.escapeHtml("A & B"))
    }

    @Test
    fun testEscapeLessThan() {
        assertEquals("&lt;script&gt;", HtmlUtils.escapeHtml("<script>"))
    }

    @Test
    fun testEscapeDoubleQuote() {
        assertEquals("say &quot;hello&quot;", HtmlUtils.escapeHtml("say \"hello\""))
    }

    @Test
    fun testEscapeSingleQuote() {
        assertEquals("it&#39;s", HtmlUtils.escapeHtml("it's"))
    }

    @Test
    fun testEscapeAllSpecialChars() {
        assertEquals("&lt;a href=&quot;x&quot;&gt;&amp;&#39;", HtmlUtils.escapeHtml("<a href=\"x\">&'"))
    }

    @Test
    fun testEmptyString() {
        assertEquals("", HtmlUtils.escapeHtml(""))
    }

    @Test
    fun testNoSpecialChars() {
        assertEquals("Hello World 123!", HtmlUtils.escapeHtml("Hello World 123!"))
    }

    @Test
    fun testUnicodePassthrough() {
        assertEquals("日本語 &amp; émojis 🎉", HtmlUtils.escapeHtml("日本語 & émojis 🎉"))
    }
}
