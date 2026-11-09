package dybarsky.blocker.store

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.IOException
import androidx.datastore.core.Serializer
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object RecordSerializer : Serializer<List<Record>> {

    override val defaultValue: List<Record> = emptyList()

    override suspend fun readFrom(input: InputStream): List<Record> =
        runCatching {
            val string = input.readBytes().decodeToString()
            Json.decodeFromString<List<Record>>(string)
        }.getOrElse {
            throw CorruptionException("Failed to read numbers from file", it)
        }

    override suspend fun writeTo(t: List<Record>, output: OutputStream) {
        runCatching {
            val string = Json.encodeToString(t).encodeToByteArray()
            output.write(string)
        }.onFailure {
            throw IOException("Failed to write numbers to file", it)
        }
    }
}
