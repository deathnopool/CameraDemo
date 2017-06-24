package com.fuckoff.camerademo;

import android.content.Intent;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import static com.fuckoff.camerademo.CameraActivity.checkCameraHardware;

public class MainActivity extends AppCompatActivity {

    Button takePictureBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!checkCameraHardware(this)){
            Toast.makeText(this, "这个手机没有相机", Toast.LENGTH_SHORT).show();
            return;
        }

        takePictureBtn = (Button) findViewById(R.id.take_picture_btn);
        takePictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });


    }

}
