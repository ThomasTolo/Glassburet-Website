package com.glassburet.realtime;

import org.springframework.stereotype.Service;

@Service
public class SiteUpdateBroadcaster {

    private final SiteUpdateWebSocketHandler handler;

    public SiteUpdateBroadcaster(SiteUpdateWebSocketHandler handler) {
        this.handler = handler;
    }

    public void publish(String type) {
        handler.broadcast("{\"type\":\"" + type + "\"}");
    }
}
