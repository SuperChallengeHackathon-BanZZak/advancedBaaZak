package org.tensorflow.lite.examples.posenet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("state", "launch");

        try{
            Thread.sleep(3000);

        }catch (InterruptedException e){
            e.printStackTrace();
        }

        startActivity(intent);
        finish();
    }
}
