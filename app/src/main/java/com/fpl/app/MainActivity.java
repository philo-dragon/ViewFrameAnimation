package com.fpl.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View view = findViewById(R.id.view);

        final FrameAnimatorUtils build = new FrameAnimatorUtils.Builder()
                .setFrames_number(20)
                .setFrames_rate(120)
                .setH_frames(4)
                .setW_frames(5)
                .setLoop_from(1)
                .setLoop_to(20)
                .setAutoAnimation(false)
                .setView(view)
                .setResId(R.drawable.pao)
                .build()
                .display();

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                build.toggle();
            }
        });
    }
}
