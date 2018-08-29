package com.dryseed.dslayout;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.zhangyue.we.x2c.X2C;
import com.zhangyue.we.x2c.ano.Xml;

//@Xml(layouts = {R.layout.activity_main})
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        X2C.setContentView(this, R.layout.activity_main);
    }
}
