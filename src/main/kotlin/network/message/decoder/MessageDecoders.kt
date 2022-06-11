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
package katium.client.qq.network.message.decoder

import katium.client.qq.chat.QQChat
import katium.client.qq.network.QQClient
import katium.client.qq.network.event.QQMessageDecodersInitializeEvent
import katium.client.qq.network.pb.PbMessageElements
import katium.client.qq.network.pb.PbMessages
import katium.client.qq.util.CoroutineLazy
import katium.core.message.content.MessageChain
import katium.core.util.event.post

class MessageDecoders(val client: QQClient) {

    val decoders = CoroutineLazy(client) {
        val decoders = mutableMapOf<Int, MessageDecoder>()
        registerBuiltinParsers(decoders)
        client.bot.post(QQMessageDecodersInitializeEvent(client, decoders))
        decoders.toMap()
    }

    private fun registerBuiltinParsers(decoders: MutableMap<Int, MessageDecoder>) {
        decoders[1] = PlainTextDecoder
        decoders[4] = NotOnlineImageDecoder
        decoders[8] = CustomFaceDecoder
        decoders[12] = QQServiceDecoder
        decoders[45] = RefMessageDecoder
    }

    operator fun get(type: Int) = decoders.getSync()[type]

    operator fun get(element: PbMessageElements.Element): MessageDecoder? {
        val fieldKeys = element.allFields.keys
        if (fieldKeys.isEmpty()) return null
        if (fieldKeys.size != 1) throw UnsupportedOperationException("Too many fields: $fieldKeys in $element")
        return get(fieldKeys.first().number) ?: FallbackDecoder
            //?: throw UnsupportedOperationException("Unknown element type: ${fieldKeys.first()}(${fieldKeys.first().number})"))
    }

    suspend fun decode(context: QQChat, message: PbMessages.Message) = MessageChain(*message.body.richText.elementsList.mapNotNull {
        get(it)?.decode(client, context, message, it)
    }.toTypedArray()).simplest

}