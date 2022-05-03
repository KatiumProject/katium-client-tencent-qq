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
package katium.client.qq

import katium.client.qq.network.QQClient
import katium.core.Bot
import katium.core.BotPlatform
import katium.core.chat.LocalChatID
import katium.core.user.Contact
import katium.core.user.User
import kotlinx.coroutines.Job
import javax.swing.GroupLayout

class QQBot(uid: Long) : Bot(QQBotPlatform, QQLocalChatID(uid)) {

    val client: QQClient = QQClient(this)

    override val loopJob: Job
        get() = TODO("Not yet implemented")

    override val allContacts: Set<Contact>
        get() = TODO("Not yet implemented")

    override val isConnected: Boolean
        get() = TODO("Not yet implemented")

    override val isOnline: Boolean
        get() = TODO("Not yet implemented")

    override fun getGroup(id: LocalChatID): GroupLayout.Group {
        TODO("Not yet implemented")
    }

    override fun getUser(id: LocalChatID): User {
        TODO("Not yet implemented")
    }

}
