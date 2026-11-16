package dybarsky.blocker.store

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.IOException
import androidx.datastore.core.Serializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/** DataStore serializer that keeps a list of [T] as a JSON array. */
class JsonListSerializer<T>(element: KSerializer<T>) : Serializer<List<T>> {

    private val serializer = ListSerializer(element)

    override val defaultValue: List<T> = emptyList()

    override suspend fun readFrom(input: InputStream): List<T> =
        runCatching {
            Json.decodeFromString(serializer, input.readBytes().decodeToString())
        }.getOrElse {
            throw CorruptionException("Failed to read list from file", it)
        }

    override suspend fun writeTo(t: List<T>, output: OutputStream) {
        runCatching {
            output.write(Json.encodeToString(serializer, t).encodeToByteArray())
        }.onFailure {
            throw IOException("Failed to write list to file", it)
        }
    }
}
