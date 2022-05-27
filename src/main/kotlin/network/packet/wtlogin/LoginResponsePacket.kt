/*
 * Copyright 2022 Katium Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package katium.client.qq.network.packet.wtlogin

import com.google.common.hash.Hashing
import io.netty.buffer.ByteBuf
import katium.client.qq.network.QQClient
import katium.client.qq.network.codec.base.readQQShortLengthString
import katium.client.qq.network.codec.oicq.OicqPacket
import katium.client.qq.network.codec.tlv.applyT119
import katium.client.qq.network.codec.tlv.readT119
import katium.client.qq.network.codec.tlv.readTlvMap
import katium.core.util.netty.readUByte
import katium.core.util.netty.toArray
import java.util.*
import kotlin.random.Random

class LoginResponsePacket(client: QQClient, uin: Int, command: Short) :
    OicqPacket.Response.Simple(client, uin, command) {

    var success: Boolean = false
        private set
    var captchaImage: ByteBuf? = null
        private set
    var captchaSignature: ByteBuf? = null
        private set
    var verifyUrl: String? = null
        private set
    var smsPhone: String? = null
        private set
    var errorMessage: String? = null
        private set

    override fun readBody(input: ByteBuf) {
        input.run {
            skipBytes(2) // sub command
            val type = readUByte().toUInt()
            skipBytes(2)
            val tlv = readTlvMap(2, release = false)
            if (0x402 in tlv) {
                client.sig.dpwd = Random.Default.nextBytes(16).toUByteArray()
                client.sig.t402 = tlv[0x402]!!.toArray(release = false).toUByteArray()
                @Suppress("DEPRECATION")
                client.sig.g =
                    Hashing.md5()
                        .hashBytes(client.deviceInfo.guid + client.sig.dpwd!!.toByteArray() + client.sig.t402!!.toByteArray())
                        .asBytes().toUByteArray()
            }
            when (type) {
                0x00u -> {
                    success = true
                    /*if(0x150 in tlv) {
                        client.sig.t150 = tlv[0x150]!!.toUByteArray()
                    }
                    if(0x161 in tlv) {
                        val tlv2 = alloc().buffer(tlv[0x161]!!).skipBytes(2).readTlvMap(2)
                        if(0x172 in tlv2) {
                            client.sig.rollbackSig = tlv2[0x172]!!.toUByteArray()
                        }
                    }*/
                    if (0x403 in tlv) {
                        client.sig.randomSeed = tlv[0x403]!!.toArray(release = false).toUByteArray()
                    }
                    tlv[0x119]!!.readT119(client.deviceInfo.tgtgtKey, release = false).use {
                        it.applyT119(client)
                    }
                    if (0x149 in tlv) {
                        success = false
                        tlv[0x149]!!.apply {
                            skipBytes(2)
                            errorMessage = "Other device login(0 with T149), ${readQQShortLengthString()}"
                        }
                    }
                    if (0x146 in tlv) {
                        success = false
                        tlv[0x146]!!.apply {
                            skipBytes(4)
                            errorMessage = "Other device login(0 with T146), ${readQQShortLengthString()}"
                        }
                    }
                }
                0x01u -> errorMessage = "Wrong password"
                0x02u -> {
                    errorMessage = "Need captcha(2)"
                    client.sig.t104 = tlv[0x104]!!.toArray(false).toUByteArray()
                    if (0x192 in tlv) {
                        errorMessage += ", url available"
                        verifyUrl = String(tlv[0x192]!!.toArray(false))
                    } else if (0x165 in tlv) {
                        errorMessage += ", image available"
                        tlv[0x165]!!.run {
                            val signLength = readShort().toInt()
                            skipBytes(2)
                            captchaSignature = readBytes(signLength)
                            captchaImage = readBytes(readableBytes())
                        }
                    } else {
                        errorMessage += ", unknown login(no available captcha detected)"
                    }
                }
                0x06u -> errorMessage = "Slider ticket error(6), network environment error, not allowed to login"
                0x09u -> errorMessage = "Protocol error(9)"
                0x28u -> errorMessage = "Account frozen(40)"
                0xA0u, 0xEFu -> {
                    errorMessage = "Unsafe device($type)"
                    if (0x174 in tlv) { // SMS Verify
                        errorMessage += ", with SMS verify(T174), server(T17E): ${String(tlv[0x17E]!!.toArray(false))}"
                        client.sig.t104 = tlv[0x104]!!.toArray(false).toUByteArray()
                        client.sig.t174 = tlv[0x174]!!.toArray(false).toUByteArray()
                        client.sig.randomSeed = tlv[0x403]!!.toArray(false).toUByteArray()
                        smsPhone = String(tlv[0x178]!!.toArray(false))
                        val index = smsPhone!!.indexOf(0x0B.toChar()) + 1
                        smsPhone = smsPhone!!.substring(index, index + 11)
                    }
                    if (0x17B in tlv) {
                        errorMessage += ", SMS needed error(T17B)"
                        client.sig.t104 = tlv[0x104]!!.toArray(false).toUByteArray()
                    }
                    if (0x204 in tlv) { // QR code
                        errorMessage += ", with QR code(T204)"
                        verifyUrl = String(tlv[0x204]!!.toArray(false))
                    }
                }
                0xA1u, 0xA2u -> errorMessage = "Too many SMS requests($type)"
                0xA3u -> errorMessage = "Wrong device lock verification code(163)"
                0xB4u -> errorMessage = "Fallback(180), ECDH error"
                0xCCu -> TODO("Device lock") // @TODO: https://cs.github.com/Mrs4s/MiraiGo/blob/master/client/decoders.go#L146
                0xEDu -> errorMessage = "Account not enabled(237)"
                else -> throw IllegalStateException("Unknown login response type: $type")
            }
            tlv.release()
        }
    }

    override fun close() {
        captchaImage?.release()
        captchaSignature?.release()
    }

    override fun toString() =
        "LoginResponsePacket(success=$success, captchaImage=$captchaImage, captchaSignature=$captchaSignature, verifyUrl=$verifyUrl, smsPhone=$smsPhone, errorMessage=$errorMessage)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LoginResponsePacket) return false
        if (success != other.success) return false
        if (captchaImage != other.captchaImage) return false
        if (captchaSignature != other.captchaSignature) return false
        if (verifyUrl != other.verifyUrl) return false
        if (smsPhone != other.smsPhone) return false
        if (errorMessage != other.errorMessage) return false
        return true
    }

    override fun hashCode(): Int {
        var result = success.hashCode()
        result = 31 * result + (captchaImage?.hashCode() ?: 0)
        result = 31 * result + (captchaSignature?.hashCode() ?: 0)
        result = 31 * result + (verifyUrl?.hashCode() ?: 0)
        result = 31 * result + (smsPhone?.hashCode() ?: 0)
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        return result
    }

}
