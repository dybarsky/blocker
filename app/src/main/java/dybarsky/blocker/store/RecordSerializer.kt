package dybarsky.blocker.store

import androidx.datastore.core.Serializer

object RecordSerializer : Serializer<List<Record>> by JsonListSerializer(Record.serializer())
