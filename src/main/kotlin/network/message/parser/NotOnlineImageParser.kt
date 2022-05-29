package katium.client.qq.network.message.parser

import katium.client.qq.message.content.QQImage
import katium.client.qq.network.QQClient
import katium.client.qq.network.pb.PbMessageElements
import katium.client.qq.network.pb.PbMessages

object NotOnlineImageParser : MessageParser {

    override suspend fun parse(
        client: QQClient,
        message: PbMessages.Message,
        element: PbMessageElements.Element
    ) = element.notOnlineImage.run {
        QQImage(
            resourceKey = resourceID,
            originUrl = origUrl,
            md5 = pictureMd5,
            width = if (hasPictureWidth()) pictureWidth else null,
            height = if (hasPictureHeight()) pictureHeight else null
        )
    }

}