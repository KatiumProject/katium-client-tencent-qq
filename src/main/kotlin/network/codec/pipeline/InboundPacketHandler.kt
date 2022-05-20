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
package katium.client.qq.network.codec.pipeline

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import katium.client.qq.network.QQClient
import katium.client.qq.network.codec.packet.TransportPacket
import kotlin.coroutines.resume

class InboundPacketHandler(val client: QQClient) : ChannelInboundHandlerAdapter() {

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        super.channelRead(ctx, msg)
        msg as TransportPacket.Response
        val handler = client.packetHandlers[msg.sequenceID]
        if (handler != null) {
            handler.resume(msg)
            client.packetHandlers.remove(msg.sequenceID)
        }
    }

}