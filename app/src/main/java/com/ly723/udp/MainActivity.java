package com.ly723.udp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private TextView mTvLength;
    private TextView mTvLog;
    private EditText mEtContent;
    private EditText mEtIP;

    private int mPort;
    private int mMaxStrNum;

    public MyHandler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {

        private final WeakReference<MainActivity> mActivity;

        MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                activity.mTvLog.append(msg.obj + "\n");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        initConfigs();
        initUI();
        new Server().start();
    }

    private void initConfigs() {
        mPort = getResources().getInteger(R.integer.port);
        mMaxStrNum = getResources().getInteger(R.integer.input_max_length);
    }

    private void initUI() {
        mTvLog = (TextView) findViewById(R.id.tv_log);
        mTvLength = (TextView) findViewById(R.id.tv_length);
        String content = "0/" + mMaxStrNum;
        mTvLength.setText(content);
        mEtContent = (EditText) findViewById(R.id.et_content);
        mEtIP = (EditText) findViewById(R.id.et_ip);
        mEtContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mTvLength.setText(s.length() + "/" + mMaxStrNum);
            }
        });
    }

    public void onSend(View v) {
        new Sender().start();
    }

    public class Sender extends Thread {
        @Override
        public void run() {
            try {
                byte[] bytes = mEtContent.getText().toString().getBytes();
                String ip = mEtIP.getText().toString();
                DatagramPacket packet = new DatagramPacket(
                        bytes,
                        bytes.length,
                        TextUtils.isEmpty(ip) ? InetAddress.getLocalHost() : InetAddress.getByName(ip),
                        mPort);
                new DatagramSocket().send(packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class Server extends Thread {
        private boolean mClose;

        @Override
        public void run() {
            try {
                DatagramSocket socket = new DatagramSocket(mPort);
                showState("Server开启");
                byte[] bytes;
                DatagramPacket packet;
                while (!mClose) {
                    bytes = new byte[mMaxStrNum * 3];
                    packet = new DatagramPacket(bytes, bytes.length);
                    socket.receive(packet);
                    Log.d("aaa", Arrays.toString(bytes));
                    showState("接收到：" + new String(packet.getData()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void close() {
            mClose = true;
        }

    }

    public void showState(String s) {
        Message msg = Message.obtain();
        msg.obj = s;
        mHandler.sendMessage(msg);
    }
}
