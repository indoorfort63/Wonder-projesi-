package com.example.wonderclient;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

public class PlayerActivity extends Activity {
    private PowerManager.WakeLock wl;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_player);
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "wonder:player");
        wl.acquire();

        String id = getIntent().getStringExtra("id");
        VideoView vv = (VideoView) findViewById(R.id.video);
        MediaController mc = new MediaController(this);
        mc.setAnchorView(vv);
        vv.setMediaController(mc);
        vv.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(PlayerActivity.this, "Video oynatılamadı (" + what + ")",
                        Toast.LENGTH_LONG).show();
                return true;
            }
        });
        Toast.makeText(this, "Yükleniyor...", Toast.LENGTH_SHORT).show();
        vv.setVideoURI(Uri.parse(Api.base(this) + "/video?id=" + id));
        vv.start();
    }

    @Override
    protected void onDestroy() {
        if (wl != null && wl.isHeld()) wl.release();
        super.onDestroy();
    }
}
