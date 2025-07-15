package com.zjfgh.bluedhook.simple;

import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class WSServerManager {
    private static final String TAG = "WSServerManager";

    private WebSocketServer mWebSocketServer;
    private int mPort;
    private final WSServerListener mListener;
    private final List<WebSocket> mConnections = new ArrayList<>();
    private volatile boolean mIsRunning = false;

    public interface WSServerListener {
        void onServerStarted(int port);
        void onServerStopped();
        void onServerError(String error);
        void onClientConnected(String address);
        void onClientDisconnected(String address);
        void onMessageReceived(WebSocket conn, String message);
    }

    public WSServerManager(WSServerListener listener) {
        this.mListener = listener;
    }

    public void startServer(int port) {
        this.mPort = port;
        Log.d(TAG, "Server start requested on port " + port);
    }

    public void stopServer() {
        Log.d(TAG, "Server stop requested");
    }

    public void broadcastMessage(String message) {
        Log.d(TAG, "Broadcast message requested: " + message);
    }

    public void sendMessage(WebSocket conn, String message) {
        Log.d(TAG, "Send message requested: " + message);
    }

    public int getConnectedClientsCount() {
        return 0;
    }

    public boolean isServerRunning() {
        return mIsRunning;
    }

    public int getPort() {
        return mPort;
    }

    public List<WebSocket> getConnections() {
        return new ArrayList<>(mConnections);
    }
}
