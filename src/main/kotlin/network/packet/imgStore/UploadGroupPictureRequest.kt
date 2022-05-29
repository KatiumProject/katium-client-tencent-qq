package katium.client.qq.network.packet.imgStore

import com.google.common.hash.HashCode
import com.google.protobuf.ByteString
import io.netty.buffer.ByteBufAllocator
import katium.client.qq.network.QQClient
import katium.client.qq.network.codec.packet.TransportPacket
import katium.client.qq.network.pb.PbCmd0x388
import katium.core.util.netty.heapBuffer

object UploadGroupPictureRequest {

    fun create(
        client: QQClient,
        sequenceID: Int = client.allocPacketSequenceID(),
        groupCode: Long,
        md5: HashCode,
        fileSize: Int,
    ) =
        TransportPacket.Request.Buffered(
            client = client,
            type = TransportPacket.Type.SIMPLE,
            encryptType = TransportPacket.EncryptType.D2_KEY,
            sequenceID = sequenceID,
            command = "ImgStore.GroupPicUp",
            body = ByteBufAllocator.DEFAULT.heapBuffer(createRequest(client, groupCode, md5, fileSize).toByteArray())
        )

    fun createRequest(
        client: QQClient,
        groupCode: Long,
        md5: HashCode,
        fileSize: Int,
    ): PbCmd0x388.C388Request = PbCmd0x388.C388Request.newBuilder().apply {
        networkType = 3 // wifi
        subCommand = 1
        addUploadRequest(PbCmd0x388.C388UploadRequest.newBuilder().apply {
            this.groupCode = groupCode
            fromUin = client.uin
            fileID = 0
            fileMd5 = ByteString.copyFrom(md5.asBytes())
            this.fileSize = fileSize.toLong()
            fileName = ByteString.copyFrom("${md5.toString().uppercase()}.jpg".toByteArray())
            sourceTerm = 5
            platformType = 9
            buType = 1
            pictureWidth = 0
            pictureHeight = 0
            pictureType = 1000
            buildVersion = ByteString.copyFrom(client.clientVersion.version.toByteArray())
            appPictureType = 1052
            originalPicture = 0
        })
    }.build()

}