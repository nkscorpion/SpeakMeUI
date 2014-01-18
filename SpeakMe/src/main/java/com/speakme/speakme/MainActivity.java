package com.speakme.speakme;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {
    public static final String ACTION_PICK_PLUGIN = "speak.me.action.PICK_PLUGIN";
    public static final int RESULT_SPEECH = 1;
    SpeakMeReader m_speakMeReader;
    boolean m_bound = false;
    Branch m_currentBranch;

    //Views
    TextView txtCommands;
    Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get the views
        btnStart = (Button) findViewById(R.id.btn_start);
        txtCommands = (TextView) findViewById(R.id.txt_commands);

        //Set the start button's OnClickListener
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(m_bound) {
                    Intent intent = new Intent(
                            RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");

                    try {
                        startActivityForResult(intent, RESULT_SPEECH);
                    } catch (ActivityNotFoundException a) {
                        Toast t = Toast.makeText(getApplicationContext(),
                                "Opps! Your device doesn't support Speech to Text",
                                Toast.LENGTH_SHORT);
                        t.show();
                    }
                }
            }
        });

        //Set the current branch to the root branch, which has the plugins.
        m_currentBranch = new RootBranch();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Bind to SpeakMeReader
        Intent intent = new Intent(this, SpeakMeReader.class);
        bindService(intent, m_connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (m_bound) {
            unbindService(m_connection);
            m_bound = false;
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection m_connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SpeakMeReader.ReaderBinder binder = (SpeakMeReader.ReaderBinder) service;
            m_speakMeReader = binder.getService();
            m_bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            m_bound = false;
        }
    };

    //This function is called after the user speaks
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SPEECH: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> text = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    String voice_command = Arrays.toString(text.toArray()).replace(", ", " ").replaceAll("[\\[\\]]", "");

                    if(voice_command.equalsIgnoreCase("speak me") || voice_command.equalsIgnoreCase("speakme"))
                        m_currentBranch = new RootBranch();
                    else
                        m_currentBranch.parseCommand(voice_command, this);

                    //Get the next commands from the current branch and put them on the screen
                    txtCommands.setText(Arrays.toString(m_currentBranch.getCommands()).replace(", ", "\r\n").replaceAll("[\\[\\]]", ""));

                    Toast t = Toast.makeText(this,
                            voice_command,
                            Toast.LENGTH_SHORT);
                    t.show();
                    //Speak the next commands out loud
                    m_speakMeReader.speakOut("You said: " + voice_command);
                    m_speakMeReader.speakOut("Possible commands: " + Arrays.toString(m_currentBranch.getCommands()).replace(", ", "\r\n").replaceAll("[\\[\\]]", ""));
                }
                break;
            }
        }
    }
}
