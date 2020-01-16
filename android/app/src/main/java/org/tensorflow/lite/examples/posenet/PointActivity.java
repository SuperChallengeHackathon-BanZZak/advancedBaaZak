package org.tensorflow.lite.examples.posenet;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

public class PointActivity extends AppCompatActivity {

    SharedPreferences pref;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point);

        pref = getSharedPreferences("point", MODE_PRIVATE);
        int score = pref.getInt("cnt",0);

        String full="반짝이는 아침 "+score+"일째";

        String message = "";

        ImageView rabbit = (ImageView) findViewById(R.id.gif_image);
        TextView textview = (TextView) findViewById(R.id.showpoint);
        textview.setText(full);

        GlideDrawableImageViewTarget gifImage = new GlideDrawableImageViewTarget(rabbit);
        Glide.with(this).load(R.drawable.giffile).into(gifImage);




    }
}
