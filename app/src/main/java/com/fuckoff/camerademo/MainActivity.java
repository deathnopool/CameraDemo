package com.fuckoff.camerademo;

import android.Manifest;
import android.content.Intent;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import pub.devrel.easypermissions.EasyPermissions;

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

        // asking permission
        requestCameraPermission();

        takePictureBtn = (Button) findViewById(R.id.take_picture_btn);
        takePictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkCameraPermission()){
                    Toast.makeText(MainActivity.this, "没有权限启动相机！完蛋去吧！", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });


    }

    public void requestCameraPermission(){
        String[] permissions = {Manifest.permission.CAMERA};
        if (this.checkCameraPermission()){
            // then what?
        } else {
            EasyPermissions.requestPermissions(this, "想拍照就必须给我照相机的权限，懂？", 10086, permissions);
        }
    }

    public boolean checkCameraPermission(){
        String[] permissions = {Manifest.permission.CAMERA};
        return EasyPermissions.hasPermissions(this, permissions);
    }
}
