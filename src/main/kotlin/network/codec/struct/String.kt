/*
 * Katium Client Tencent QQ: Tencent QQ protocol implementation for Katium
 * Copyright (C) 2022  Katium Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package katium.client.qq.network.codec.struct

import io.netty.buffer.ByteBuf

fun ByteBuf.writeQQIntLengthString(string: String): ByteBuf {
    val bytes = string.toByteArray()
    writeInt(bytes.size + 4)
    writeBytes(bytes)
    return this
}

fun ByteBuf.readQQIntLengthString(): String {
    val buffer = ByteArray(readInt() - 4)
    readBytes(buffer)
    return String(buffer)
}

fun ByteBuf.writeQQShortLengthString(string: String): ByteBuf {
    val bytes = string.toByteArray()
    writeShort(bytes.size)
    writeBytes(bytes)
    return this
}
