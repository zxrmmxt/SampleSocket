package com.xt.samplesocket;

import android.os.Bundle;
import android.text.TextUtils;
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

import java.io.IOException;

/**
 * @author xt on 2020/4/3 14:42
 */
public class ClientActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        final EditText editText1 = findViewById(R.id.editText1);
        Button         button2   = findViewById(R.id.button2);
        final EditText editText2 = findViewById(R.id.editText2);
        TextView       button3   = findViewById(R.id.button3);
        final TextView textView2 = findViewById(R.id.textView2);


        editText1.setText(SPUtils.getInstance().getString("ipAddress", "192.168.168.145:5363"));
        //连接
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String address = editText1.getText().toString().trim();
                    SPUtils.getInstance().put("ipAddress", address);
                    String[]     split     = address.split(":");
                    final String ipAddress = split[0];
                    final int    port      = Integer.parseInt(split[1]);
                    SocketManager.getInstance().connect(ipAddress, port);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //发送
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String text = editText2.getText().toString().trim();
                if (TextUtils.isEmpty(text)) {
                    return;
                }
                SocketManager.getInstance().send(ConvertUtils.hexString2Bytes(text));
            }
        });

        SocketManager.getInstance().setServerCallback(new SocketManager.ServerCallback() {
            @Override
            public void onServerConnected() {
                ToastUtils.showShort("连接成功");
            }

            @Override
            public void onServerDisconnected(IOException e) {
                ToastUtils.showShort(e.getMessage());
            }

            @Override
            public void onOnReceiveData(final byte[] receiveData) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView2.setText(ConvertUtils.bytes2HexString(receiveData));
                    }
                });
            }
        });
    }
}
