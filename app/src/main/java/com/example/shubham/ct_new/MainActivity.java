package com.example.shubham.ct_new;

import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    Button buttonstart,buttonstop,buttonplaylastrecord,buttonstopplaying;

    TextView textView;

    Thread runner;
    private static double mEMA=0.0;
    static final private double EMA_FILTER = 0.6;
    public static final int RequestPermissionCode =1 ;
    String AudioSavePathInDevice = null;

    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;

    public double soundDb(double ampl){
        return 20*Math.log10(getAmplitudeEMA()/ampl);
    }

    public double getAmplitude(){
        if (mediaRecorder != null){
            return(mediaRecorder.getMaxAmplitude());
        }else
            return 0;

    }
    public double getAmplitudeEMA(){
double amp = getAmplitude();
        mEMA = EMA_FILTER*amp + (1.0-EMA_FILTER)*mEMA; // filtering y(n)= a*x + b*y(n-1)
        return (int) mEMA;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonstart = (Button) findViewById(R.id.button);
        buttonstop = (Button) findViewById(R.id.button1);
        buttonplaylastrecord = (Button) findViewById(R.id.button2);
        buttonstopplaying = (Button) findViewById(R.id.button3);

        buttonstop.setEnabled(false);
        buttonstopplaying.setEnabled(false);
        buttonplaylastrecord.setEnabled(false);

        textView = (TextView) findViewById(R.id.textview);
        if(runner == null){
            runner = new Thread(){
                 public void run(){
                     while(runner!=null){
                         try{
                             Thread.sleep(200);
                             Log.i(TAG,"run:Tock");
                         }catch (InterruptedException e){
                             e.printStackTrace();
                         }
                         runOnUiThread(new Runnable() {
                             @Override
                             public void run() {
                                 textView.setText(Double.toString(soundDb(1))+ "dB");
                             }
                         });

                     }
                 }
            };
            runner.start();
            Log.d(TAG,"onCreate: runner started");
        }


        buttonstart.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(CheckPermission()){
                    AudioSavePathInDevice = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+"AudioRecord.3gpp";
                    MediaRecorderReady();

                    try{
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    }catch(IllegalStateException e){
                        e.printStackTrace();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    buttonstart.setEnabled(false);
                    buttonstop.setEnabled(true);
                    buttonplaylastrecord.setEnabled(false);

                    Toast.makeText(MainActivity.this, "Recording Started", Toast.LENGTH_SHORT).show();
                }else {
                    RequestPermission();
                }
            }
        });


        buttonstop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                mediaRecorder.stop();
                buttonstop.setEnabled(false);
                buttonplaylastrecord.setEnabled(true);
                buttonstart.setEnabled(true);
                buttonstopplaying.setEnabled(false);

                Toast.makeText(MainActivity.this, "Recording Completed", Toast.LENGTH_SHORT).show();

            }
        });

        buttonplaylastrecord.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) throws IllegalArgumentException, SecurityException, IllegalStateException {
                buttonstop.setEnabled(false);
                buttonstart.setEnabled(false);
                buttonstopplaying.setEnabled(true);

                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(AudioSavePathInDevice);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.start();
                Toast.makeText(MainActivity.this, "Last Recording Playing", Toast.LENGTH_SHORT).show();
            }
            });
    buttonstopplaying.setOnClickListener(new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            buttonstop.setEnabled(false);
            buttonstart.setEnabled(true);
            buttonstopplaying.setEnabled(true);
            buttonplaylastrecord.setEnabled(true);

            if(mediaPlayer != null){
                mediaPlayer.start();
                mediaPlayer.release();
                MediaRecorderReady();
            }

            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(AudioSavePathInDevice);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.start();
            Toast.makeText(MainActivity.this, "Last Recording Playing", Toast.LENGTH_SHORT).show();
        }
    });
    }


    public void MediaRecorderReady(){
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(AudioSavePathInDevice);
    }
    private void RequestPermission(){
        ActivityCompat.requestPermissions(MainActivity.this,new String[]{WRITE_EXTERNAL_STORAGE,RECORD_AUDIO}, RequestPermissionCode);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case RequestPermissionCode:
                if(grantResults.length>0){
                    boolean StoragePermission = grantResults[0]== PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1]== PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        Toast.makeText(this, "Permission is Granted", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(this, "Permission is Denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }
    public boolean CheckPermission(){
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),WRITE_EXTERNAL_STORAGE);
        int result1= ContextCompat.checkSelfPermission(getApplicationContext(),RECORD_AUDIO);

        return result== PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }
}
