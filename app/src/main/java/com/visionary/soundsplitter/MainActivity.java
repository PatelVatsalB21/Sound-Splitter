package com.visionary.soundsplitter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_AUDIO_REQUEST = 101;
    Button change_btn, dual, swapper, bluetooth;
    TextView state;
    private Boolean shouldEnableExternalSpeaker= true;
    private Boolean isBlueToothConnected = false;
    private AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer;
    private Uri m1,m2;
    private Boolean swap = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        change_btn = findViewById(R.id.change_state);
        state = findViewById(R.id.audio_state);
        dual = findViewById(R.id.dual_state);
        swapper = findViewById(R.id.dual_swap);
        bluetooth = findViewById(R.id.bluetooth);

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mMediaPlayer = new MediaPlayer();

        bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaPlayer mediaPlayer1 = MediaPlayer.create(MainActivity.this, m1);
                MediaPlayer mediaPlayer2 = MediaPlayer.create(MainActivity.this, m1);
                mediaPlayer1.setAudioStreamType(AudioManager.STREAM_RING);
                mediaPlayer2.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                mAudioManager.startBluetoothSco();
                mAudioManager.setBluetoothScoOn(true);
                mAudioManager.setSpeakerphoneOn(false);
            }
        });

        swapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m1 != null && m2 !=null){
                    swap = true;
                    dual(m1,m2);
                }
            }
        });

        dual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent audioIntent = new Intent();
                audioIntent.setType("audio/*");
                audioIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(audioIntent,PICK_AUDIO_REQUEST);
            }
        });


        change_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaPlayer.pause();
                if(shouldEnableExternalSpeaker) {
                    shouldEnableExternalSpeaker = false;
                    if(isBlueToothConnected) {
                        // 1. case - bluetooth device
                        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                        mAudioManager.startBluetoothSco();
                        mAudioManager.setBluetoothScoOn(true);
                    } else {
                        // 2. case - wired device
                        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                        mAudioManager.stopBluetoothSco();
                        mAudioManager.setBluetoothScoOn(false);
                        mAudioManager.setSpeakerphoneOn(false);
                        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    }
                    state.setText("Ear");
                } else {
                    // 3. case - phone speaker
                    mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    mAudioManager.stopBluetoothSco();
                    mAudioManager.setBluetoothScoOn(false);
                    mAudioManager.setSpeakerphoneOn(true);
                    mAudioManager.setWiredHeadsetOn(false);
                    shouldEnableExternalSpeaker = true;
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
                    state.setText("Speaker");
                }
                mMediaPlayer.start();
            }
        });
    }

    public void dual(Uri music1, Uri music2){
        MediaPlayer mediaPlayer1 = MediaPlayer.create(this, music1);
        MediaPlayer mediaPlayer2 = MediaPlayer.create(this, music2);
        if (swap){
            mediaPlayer1.pause();
            mediaPlayer2.pause();
            swap(mediaPlayer1, mediaPlayer2);
        }
        else {
            swap = false;
            final int sessionIdA = mediaPlayer1.getAudioSessionId();
            final int sessionIdB = mediaPlayer2.getAudioSessionId();
            mediaPlayer1.setLooping(true);
            mediaPlayer2.setLooping(true);
            mediaPlayer1.setVolume(0, 1);
            mediaPlayer2.setVolume(1, 0);
            mediaPlayer1.start();
            mediaPlayer2.start();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        swap = false;
        if (resultCode != RESULT_OK || data == null || data.getData() == null) {
            // error
            return;
        }
        if (requestCode == PICK_AUDIO_REQUEST) {
            try {
                Uri uri= data.getData();
                if (uri!=null){
                    if (m1 == null) m1 = uri;
                    else m2 = uri;
                }
                if (m1!=null && m2!=null) dual(m1,m2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void swap(MediaPlayer m1, MediaPlayer m2) {
        MediaPlayer temp = m2;
        m2 = m1;
        m1 = temp;
        m1.start();
        m2.start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        mAudioManager.setSpeakerphoneOn(true);
    }
}