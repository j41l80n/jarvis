package com.example.jailson.jarvis;


import android.media.AudioManager;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Message;
import android.os.Messenger;

import android.speech.RecognizerResultsIntent;
import android.text.style.LocaleSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.os.Handler;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;


public class MainActivity extends AppCompatActivity
{
    private static final int REQUEST_CODE = 1234;
    private ImageView imagem;
    private TextView textViewWords;
    Thread runner;

    private MediaRecorder mRecorder = null;
    private double mEMA = 0.0;
    static final private double EMA_FILTER = 0.6;

    final Runnable updater = new Runnable()
    {
        public void run()
        {
            updateTv();
        }
    };
    final Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewWords = (TextView) findViewById(R.id.textViewPalavras);
        imagem = (ImageView) findViewById(R.id.imagem);
        //mRecorder = new MediaRecorder();

        if (runner == null)
        {
            runner = new Thread()
            {
                public void run()
                {
                    while (runner != null)
                    {
                        try
                        {
                            Thread.sleep(1000);
                            Log.i("Noise", "Tock");
                        }
                        catch (InterruptedException e)
                        {

                        }
                        mHandler.post(updater);
                    }
                }
            };
            runner.start();
            Log.d("Noise", "start runner()");
        }

        showMessageIfThereIsNoVoiceRecognition();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        textViewWords.setText("");

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK)
        {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            for(String match : matches) {
                String currentWord = textViewWords.getText().toString();
                if(match.equals("apagar lâmpada") || match.equals("desligar lâmpada"))
                {
                    imagem.setImageResource(R.drawable.idea_96_b);
                    break;
                }
                if(match.equals("ascender lâmpada") || match.equals("acender lâmpada")
                        || match.equals("ligar lâmpada")
                        || match.equals("abrir lâmpada"))
                {
                    imagem.setImageResource(R.drawable.idea_96);
                    break;
                }

            }
        }
        else if (requestCode == 1111 && resultCode == RESULT_OK)
        {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            Toast.makeText(getApplicationContext(), "jarbas!", Toast.LENGTH_SHORT).show();
            String currentWord = textViewWords.getText().toString();
            for(String match : matches)
            {
                //String currentWord = textViewWords.getText().toString();
                textViewWords.setText(currentWord.concat(match) + "\t");
                if (match.equals("jarbas") || match.equals("jabas")
                        || match.equals("jarbes") || match.equals("jarbes")
                        || match.equals("teste") || match.equals("jubas"))
                {
                    Toast.makeText(getApplicationContext(), "entrou!", Toast.LENGTH_SHORT).show();

                    break;
                }
            }
        }
    }

    public void down()
    {
        //
        this.runOnUiThread(Update);
    }

    private Runnable Update = new Runnable()
    {
        @Override
        public void run()
        {
            // TODO Auto-generated method stub
            double a = getAmplitude();
            textViewWords.setText(String.valueOf(a));
        }
    };

    public double getAmplitude()
    {
        if (mRecorder != null)
        {
            return (mRecorder.getMaxAmplitude());
        }
        else
        {
            return 0;
        }
    }

    private void showMessageIfThereIsNoVoiceRecognition()
    {
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() == 0) {
            Toast.makeText(this, "O dispositivo não suporta reconhecimento de voz!", Toast.LENGTH_LONG).show();
        }
    }

    public void onClickVoice(View view)
    {
        //
        startVoiceRecognitionActivity();
    }

    private void startVoiceRecognitionActivity()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Olá, o que deseja normal?");
        startActivityForResult(intent, REQUEST_CODE);
    }

    private void pegarJarbas()
    {
        //Intent intent = new Intent(RecognizerIntent.ACTION_WEB_SEARCH );
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH.intern());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());

        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getAvailableLocales());
        //intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Olá, o que deseja jarbes?");
        startActivityForResult(intent, 1111);
    }

    public void onResume()
    {
        super.onResume();
        startRecorder();
    }

    public void onPause()
    {
        super.onPause();
        stopRecorder();
    }

    public void startRecorder()
    {
        if(mRecorder == null)
        {
            mRecorder = new MediaRecorder();
            mRecorder.reset();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");

            try
            {
                mRecorder.prepare();
            }
            catch (java.io.IOException ioe)
            {
                android.util.Log.e("[Monkey]", "IOException: " +
                        android.util.Log.getStackTraceString(ioe));

            }
            catch (java.lang.SecurityException e)
            {
                android.util.Log.e("[Monkey]", "SecurityException: " +
                        android.util.Log.getStackTraceString(e));
            }
            try
            {
                mRecorder.start();
            }
            catch (java.lang.SecurityException e)
            {
                android.util.Log.e("[Monkey]", "SecurityException: " +
                        android.util.Log.getStackTraceString(e));
            }
            mEMA = 0.0;
        }
    }

    public void stopRecorder()
    {
        if (mRecorder != null)
        {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    public void updateTv()
    {
        textViewWords.setText(Double.toString((getAmplitudeEMA())) + " dB" + "\n\t");

        if(getAmplitudeEMA() > 5000.0)
        {
            Toast.makeText(getApplicationContext(), "equals!", Toast.LENGTH_SHORT).show();
           // Toast.makeText(getApplicationContext(), RecognizerIntent.ACTION_RECOGNIZE_SPEECH.compareTo("teste")
               //     , Toast.LENGTH_SHORT).show();

            //pegarJarbas();
        }
    }

    public double soundDb(double ampl)
    {
        //
        return  20 * Math.log10(getAmplitudeEMA() / ampl);
    }

    public double getAmplitudeEMA()
    {
        double amp =  getAmplitude();
        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
        return mEMA;
    }


}

