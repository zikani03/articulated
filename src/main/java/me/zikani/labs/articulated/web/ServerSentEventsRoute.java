package me.zikani.labs.articulated.web;

import io.javalin.http.sse.SseClient;

import java.util.Queue;
import java.util.function.Consumer;

/**
 * Basic Server Sent Events stream route with Spark.
 * Probably has a lot to be improved like getting data
 * from
 */
public class ServerSentEventsRoute implements Consumer<SseClient> {
    private final long intervalMillis = 1_000;
    private final Queue<String> dataQueue;

    public ServerSentEventsRoute(Queue<String> dataQueue) {
        this.dataQueue = dataQueue;
    }


    @Override
    public void accept(SseClient sseClient) {
        while (true) {
            try {
                String data = dataQueue.poll();
                String event = ":keepalive";
                if (data != null) {
                    event = String.format("event:%s\ndata:%s\n\n", "data", data);
                }

                sseClient.sendEvent(event, data);
                Thread.sleep(intervalMillis);
            } catch (Exception e) {
            }
        }
    }

    public static class SSEException extends RuntimeException {
        public SSEException(Throwable cause) {
            super(cause);
        }
    }
}
