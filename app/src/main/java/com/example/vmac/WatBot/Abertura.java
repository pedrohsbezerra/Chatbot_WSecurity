package com.example.vmac.WatBot;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;


public class Abertura extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abertura);
        Thread timer = new Thread(){
            public void run(){
                try{
                    sleep(4000);   // set the duration of splash screen
                }
                catch(InterruptedException e){
                    e.printStackTrace();
                } finally {
                    Intent intent = new Intent(Abertura.this, MainActivity.class);
                    startActivity(intent);
                }
            }
        };
        timer.start();


    }
    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
