package com.speakme.speakme;

import java.io.FileDescriptor;
import java.util.Locale;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class SpeakMeReader extends Service {
    public class ReaderBinder extends Binder {
        SpeakMeReader getService() {
            // Return this instance of LocalService so clients can call public methods
            return SpeakMeReader.this;
        }
    }

    private final IBinder m_binder = new ReaderBinder();
    private SpeakMeReader m_service;
    private TextToSpeech tts;

    public void stopSpeaking() {
        tts.speak("", TextToSpeech.QUEUE_FLUSH, null);
    }

    public void speakOut(String text) {
        tts.speak(text, TextToSpeech.QUEUE_ADD, null);
    }

    //Reminder: This is only called on the first bind request. Our app uses only one anyway.
    @Override
    public IBinder onBind(Intent intent) {
        m_service = this;

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    }
                } else {
                    Log.e("TTS", "Initilization Failed!");
                }
            }
        });

        return m_binder;
    }

}
