package com.xt.samplesocket;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;

/**
 * @author xt on 2020/4/3 14:41
 */
public class ServerActivity extends AppCompatActivity {

    private TextView mTextView1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        final Button button3 = findViewById(R.id.button3);
        //刷新本机ip
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ipAddress = NetworkUtils.getIPAddress(true);
                button3.setText(ipAddress);
            }
        });

        final EditText editText1 = findViewById(R.id.editText1);
        editText1.setText(String.valueOf(SPUtils.getInstance().getInt("port", 5363)));
        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    int port = Integer.parseInt(editText1.getText().toString().trim());
                    SPUtils.getInstance().put("port", port);
                    ServerSocketManager.getInstance().beginListen(port);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //接收数据
        mTextView1 = findViewById(R.id.textView1);
        final EditText editText2 = findViewById(R.id.editText2);
        //发送数据
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String hexStr = editText2.getText().toString().trim();
                    ServerSocketManager.getInstance().sendMessage(hexStr);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        ServerSocketManager.getInstance().setClientCallback(new ServerSocketManager.ClientCallback() {
            @Override
            public void onClientConnected() {
                ToastUtils.showShort("连接成功");
            }

            @Override
            public void onClientDisconneted() {
                ToastUtils.showShort("连接断开");
            }

            @Override
            public void onReceiveData(final byte[] receiveData) {
                MyThreadUtils.doMainWork(new Runnable() {
                    @Override
                    public void run() {
                        mTextView1.setText(ConvertUtils.bytes2HexString(receiveData));
                    }
                });
            }
        });
    }
}
