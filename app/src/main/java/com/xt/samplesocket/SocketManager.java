package com.xt.samplesocket;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * created by XuTi on 2019/4/29 15:14
 */
public final class SocketManager {
    private static final String       TAG = "SocketManager";
    /**
     * Socket
     */
    private              Socket       socket;
    /**
     * IP地址
     */
    private              String       ipAddress;
    /**
     * 端口号
     */
    private              int          port;
    /**
     * Socket输出流
     */
    private volatile     OutputStream outputStream;
    /**
     * Socket输入流
     */
    private volatile     InputStream  inputStream;

    /**
     * 连接回调
     */
    private ServerCallback mServerCallback;

    private SocketManager() {
    }

    private static class Holder {
        private static SocketManager sInstance = new SocketManager();
    }

    public static SocketManager getInstance() {
        return Holder.sInstance;
    }

    /**
     * 通过IP地址(域名)和端口进行连接
     * 必须在子线程中调用
     *
     * @param ipAddress IP地址(域名)
     * @param port      端口
     */
    public void connect(final String ipAddress, final int port) {
        close();
        try {
            socket = new Socket(ipAddress, port);
            Log.e(TAG, "连接成功");
            this.ipAddress = ipAddress;
            this.port = port;

            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();

            if (mServerCallback != null) {
                mServerCallback.onServerConnected();
            }

            receive();
        } catch (IOException e) {
            Log.e(TAG, "连接异常：" + e.toString());
            close();
            if (mServerCallback != null) {
                mServerCallback.onServerDisconnected(e);
            }
        }
    }

    /**
     * 判断是否连接
     */
    public boolean isConnected() {
        return socket != null && socket.isConnected() && (!socket.isClosed());
    }

    /**
     * 断开连接
     */
    public void close() {
        try {
            if (outputStream != null) {
                outputStream.close();
                outputStream = null;
            }
            if (inputStream != null) {
                inputStream.close();
                inputStream = null;
            }

            if (socket != null) {
                if (!socket.isClosed()) {
                    socket.close();
                }
                socket = null;
            }
        } catch (IOException e) {
            Log.i(TAG, e.toString());
        }
    }

    /**
     * 接收数据
     */
    public void receive() throws IOException {
        while (isConnected()) {
            byte[] buffer = new byte[1024];
            //获取接收到的字节和字节数
            int length = inputStream.read(buffer);
            if (length != -1) {
                //获取正确的字节
                byte[] receiveData = new byte[length];
                System.arraycopy(buffer, 0, receiveData, 0, length);
                Log.i(TAG, "接收字节数组=====" + bytesToHexString(receiveData));
                if (mServerCallback != null) {
                    mServerCallback.onOnReceiveData(receiveData);
                }
            }
        }
    }

    /**
     * 发送数据
     *
     * @param data 数据
     */
    public void send(final byte[] data) throws IOException {
        if (isConnected()) {
            outputStream.write(data);
            outputStream.flush();
            Log.i(TAG, "发送成功");
        }
    }

    /**
     * 回调声明
     */
    public interface ServerCallback {
        void onServerConnected();

        void onServerDisconnected(IOException e);

        void onOnReceiveData(byte[] receiveData);
    }

    public void setServerCallback(ServerCallback serverCallback) {
        this.mServerCallback = serverCallback;
    }

    /**
     * 移除回调
     */
    private void removeCallback() {
        mServerCallback = null;
    }

    /**
     * 数组转换成十六进制字符串
     *
     * @param byteArray
     * @return
     */
    public static final String bytesToHexString(byte[] byteArray) {
        StringBuffer sb = new StringBuffer(byteArray.length);
        String       sTemp;
        for (int i = 0; i < byteArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & byteArray[i]);
            if (sTemp.length() < 2) {
                sb.append(0);
            }
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }
}
