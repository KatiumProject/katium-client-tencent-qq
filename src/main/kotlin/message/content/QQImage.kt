package katium.client.qq.message.content

import com.google.protobuf.ByteString
import katium.core.message.content.Image
import katium.core.util.okhttp.GlobalHttpClient
import katium.core.util.okhttp.await
import katium.core.util.okhttp.expected
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import java.util.*

class QQImage(
    val resourceKey: String,
    originUrl: String,
    val md5: ByteString,
    val size: Int? = null,
    val filePath: String = "${HexFormat.of().formatHex(md5.toByteArray()).uppercase()}.jpg",
    width: Int? = null,
    height: Int? = null,
) : Image(width, height) {

    override val contentBytes: ByteArray by lazy {
        runBlocking(CoroutineName("Download QQ Chat Image")) {
            GlobalHttpClient.newCall(
                Request.Builder()
                    .url(contentUrl)
                    .get()
                    .build()
            )
                .await()
                .expected(200)
                .body
                .bytes()
        }
    }

    override val contentUrl = "https://c2cpicdw.qpic.cn/$originUrl"

    override fun toString() = "[QQImage($resourceKey, $contentUrl)]"

}