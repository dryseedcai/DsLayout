package com.dryseed.dslayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.zhangyue.we.x2c.X2C;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        X2C.setContentView(this, R.layout.activity_main);
    }
}
