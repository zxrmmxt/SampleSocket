package com.xt.samplesocket;

import com.blankj.utilcode.util.ConvertUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author xt on 2020/4/3 14:31
 */
public class ServerSocketManager {
    private        ServerSocket        mServerSocket;
    private        Socket              mSocket;
    private        InputStream         mInputStream;
    private        OutputStream        mOutputStream;
    private static ServerSocketManager sServerSocketManager;
    private        ClientCallback      mClientCallback;

    /**
     * @steps bind();绑定端口号
     */
    private ServerSocketManager() {
    }

    public static ServerSocketManager getInstance() {
        if (sServerSocketManager == null) {
            sServerSocketManager = new ServerSocketManager();
        }
        return sServerSocketManager;
    }


    /**
     * @param port 端口号
     * @steps listen();
     * @effect socket监听数据
     */
    public void beginListen(final int port) {
        MyThreadUtils.doBackgroundWork(new Runnable() {
            @Override
            public void run() {
                try {
                    close();

                    mServerSocket = new ServerSocket(port);

                    /**
                     * accept();
                     * 接受请求
                     * */
                    mSocket = mServerSocket.accept();
                    /**得到输入流*/
                    mInputStream = mSocket.getInputStream();
                    mOutputStream = mSocket.getOutputStream();
                    {
                        if (mClientCallback != null) {
                            mClientCallback.onClientConnected();
                        }
                    }
                    {
                        /**
                         * 实现数据循环接收
                         */
                        while (isConnected()) {
                            byte[] buffer = new byte[1024];
                            //获取接收到的字节和字节数
                            int length = mInputStream.read(buffer);
                            if (length != -1) {
                                //获取正确的字节
                                byte[] receiveData = new byte[length];
                                System.arraycopy(buffer, 0, receiveData, 0, length);
                                if (mClientCallback != null) {
                                    mClientCallback.onReceiveData(receiveData);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    close();
                    if (mClientCallback != null) {
                        mClientCallback.onClientDisconneted();
                    }
                }
            }
        });
    }

    /**
     * @steps write();
     * @effect socket服务端发送信息
     */
    public void sendMessage(final String hexStr) {
        MyThreadUtils.doBackgroundWork(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isConnected()) {
                        mOutputStream.write(ConvertUtils.hexString2Bytes(hexStr));
                        mOutputStream.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    close();
                    if (mClientCallback != null) {
                        mClientCallback.onClientDisconneted();
                    }
                }
            }
        });
    }

    /**
     * 判断是否连接
     */
    public boolean isConnected() {
        return (mSocket != null) && mSocket.isConnected() && (!mSocket.isClosed()) && (mServerSocket != null) && (!mServerSocket.isClosed());
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
        try {
            if (mServerSocket != null) {
                mServerSocket.close();
                mSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setClientCallback(ClientCallback clientCallback) {
        mClientCallback = clientCallback;
    }

    interface ClientCallback {
        void onClientConnected();

        void onClientDisconneted();

        void onReceiveData(byte[] receiveData);
    }
}
