package com.zjfgh.bluedhook.simple;

import android.util.Log;

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

    /**
     * 启动WebSocket服务器
     */
    public void startServer(int port) {
        this.mPort = port;
        if (mIsRunning) {
            Log.w(TAG, "Server is already running");
            if (mListener != null) {
                mListener.onServerError("Server is already running");
            }
            return;
        }
        // 空实现，不启动服务器
    }

    /**
     * 停止WebSocket服务器
     */
    public void stopServer() {
        if (!mIsRunning) {
            Log.w(TAG, "Server is not running");
            if (mListener != null) {
                mListener.onServerError("Server is not running");
            }
            return;
        }
        // 空实现，不执行任何操作
    }

    /**
     * 向所有连接的客户端广播消息
     */
    public void broadcastMessage(String message) {
        // 空实现，不广播消息
    }

    /**
     * 向特定客户端发送消息
     */
    public void sendMessage(WebSocket conn, String message) {
        // 空实现，不发送消息
    }

    /**
     * 获取当前连接的客户端数量
     */
    public int getConnectedClientsCount() {
        return 0; // 返回0，表示没有连接的客户端
    }

    /**
     * 检查服务器是否正在运行
     */
    public boolean isServerRunning() {
        return false; // 返回false，表示服务器未运行
    }

    /**
     * 获取服务器端口
     */
    public int getPort() {
        return 0; // 返回0，表示没有端口
    }

    /**
     * 获取所有连接的客户端
     */
    public List<WebSocket> getConnections() {
        return new ArrayList<>(); // 返回空列表
    }
}
