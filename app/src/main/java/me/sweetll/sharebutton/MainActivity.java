package me.sweetll.sharebutton;

import android.animation.ValueAnimator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    ImageView share;
    SeekBar seekBar;
    ShareDrawable shareDrawable;
    Button button;
    GooeyMenu mGooeyMenu;

    boolean flag = false;
    ValueAnimator valueAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        share = (ImageView)findViewById(R.id.share_button);
        seekBar = (SeekBar)findViewById(R.id.seek_bar);
        button = (Button)findViewById(R.id.button);

        shareDrawable = new ShareDrawable(getResources());
        share.setImageDrawable(shareDrawable);

        valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(250);
        valueAnimator.setInterpolator(new AccelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float)animation.getAnimatedValue();
                if (!flag) {
                    progress = 1 - progress;
                }
                shareDrawable.setProgress(progress);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag = !flag;
                valueAnimator.start();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Toast.makeText(seekBar.getContext(), "" + progress, Toast.LENGTH_LONG).show();

                shareDrawable.setProgress(((float) progress) / 100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

}
