package com.sict.mobile.car;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class MainActivity extends AppCompatActivity{
    private Socket socket;
    private SeekBar seekBarX, seekBarY;
    View mainView;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            socket = IO.socket("http://40.74.112.141");
            socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        seekBarX = findViewById(R.id.seek_bar_x);
        seekBarX.setMax(0);
        seekBarX.setMax(100);
        seekBarX.setProgress(50);
        seekBarY = findViewById(R.id.seek_bar_y);
        seekBarY.setMax(0);
        seekBarY.setMax(100);
        seekBarY.setProgress(50);
        mainView = findViewById(R.id.main_view);

        seekBarX.setOnTouchListener(new View.OnTouchListener() {
            int value = 90;
            JSONObject data = new JSONObject();
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    data.put("type", "servo");
                    data.put("value", value);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                switch(event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        seekBarX.setProgress(seekBarX.getProgress());
                        int tmp = seekBarX.getProgress()+40;
                        if(tmp%10 == 0 && tmp != value) {
                            value = tmp;
                            try {
                                data.put("value", value);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            socket.emit("control", data);
                        }
                        return false;

                    case MotionEvent.ACTION_UP:
                        seekBarX.setProgress(50);
                        try {
                            data.put("value", 90);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        socket.emit("control", data);
                        break;
                }
                return true;
            }
        });

        seekBarY.setOnTouchListener(new View.OnTouchListener() {
            int value = 0;
            int oldValue = 1;
            JSONObject data = new JSONObject();
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {

                    case MotionEvent.ACTION_MOVE:
                        seekBarY.setProgress(seekBarY.getProgress());
                        int tmp = seekBarY.getProgress();
                        if(tmp%10 == 0 && oldValue != tmp){
                            String type = "";
                            if(tmp < 50) {
                                value = (int) ((50-tmp)*21.2);
                                type = "back";
                            }
                            else if(tmp > 50){
                                value = (int) ((tmp-50)*21.2);
                                type = "ahead";
                            }
                            else {
                                value = 0;
                                type = "stop";
                            }
                            oldValue = tmp;
                            try {
                                data.put("type", type);
                                data.put("value", value);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            socket.emit("control", data);
                        }

                        return false;

                    case MotionEvent.ACTION_UP:
                        seekBarY.setProgress(50);
                        try {
                            data.put("type", "stop");
                            data.put("value", 0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        socket.emit("control", data);
                        break;
                }
                return true;
            }
        });

    }
}
