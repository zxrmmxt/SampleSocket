package com.xt.samplesocket;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author xt on 2019/4/29 15:14
 */
public final class SocketManager {
    private static final String       TAG = "SocketManager";
    /**
     * Socket
     */
    private              Socket       mSocket;
    /**
     * Socket输出流
     */
    private volatile     OutputStream mOutputStream;
    /**
     * Socket输入流
     */
    private volatile     InputStream  mInputStream;

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
        MyThreadUtils.doBackgroundWork(new Runnable() {
            @Override
            public void run() {
                close();
                try {
                    mSocket = new Socket(ipAddress, port);
                    Log.e(TAG, "连接成功");

                    mOutputStream = mSocket.getOutputStream();
                    mInputStream = mSocket.getInputStream();

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
        });
    }

    /**
     * 判断是否连接
     */
    public boolean isConnected() {
        return mSocket != null && mSocket.isConnected() && (!mSocket.isClosed());
    }

    /**
     * 断开连接
     */
    public void close() {
        try {
            if (mOutputStream != null) {
                mOutputStream.close();
                mOutputStream = null;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (mInputStream != null) {
                mInputStream.close();
                mInputStream = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (mSocket != null) {
                if (!mSocket.isClosed()) {
                    mSocket.close();
                }
                mSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 接收数据
     */
    public void receive() throws IOException {
        while (isConnected()) {
            byte[] buffer = new byte[1024];
            //获取接收到的字节和字节数
            int length = mInputStream.read(buffer);
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
    public void send(final byte[] data) {
        MyThreadUtils.doBackgroundWork(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isConnected()) {
                        mOutputStream.write(data);
                        mOutputStream.flush();
                        Log.i(TAG, "发送成功");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    close();
                    if (mServerCallback != null) {
                        mServerCallback.onServerDisconnected(e);
                    }
                }
            }
        });
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
