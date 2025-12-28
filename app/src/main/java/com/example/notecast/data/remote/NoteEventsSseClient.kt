package com.example.notecast.data.remote

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources

/**
 * SSE client lắng sự kiện /notes/{id}/events.
 * Đây là client thuần OkHttp, parse payload JSON ở ViewModel/UseCase phía trên.
 */
class NoteEventsSseClient(
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient = OkHttpClient(),
) {
    fun subscribeNoteEvents(noteId: String): Flow<String> = callbackFlow {
        val scope = CoroutineScope(Dispatchers.IO)
        val request = Request.Builder()
            .url("$baseUrl/notes/$noteId/events")
            .build()

        val listener = object : EventSourceListener() {
            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                scope.launch { trySend(data).isSuccess }
            }

            override fun onFailure(
                eventSource: EventSource,
                t: Throwable?,
                response: okhttp3.Response?
            ) {
                close(t)
            }
        }

        val eventSource = EventSources.createFactory(okHttpClient)
            .newEventSource(request, listener)

        awaitClose { eventSource.cancel() }
    }
}

