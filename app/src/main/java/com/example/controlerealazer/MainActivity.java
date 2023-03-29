package com.example.controlerealazer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Looper;

import java.util.logging.Handler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
      /*  Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this, FormLogin::class.java)
        startActivity(intent)
        finish()
                },3000)*/
    }
}