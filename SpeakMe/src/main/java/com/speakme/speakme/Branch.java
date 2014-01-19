package com.speakme.speakme;

import android.content.Context;

/**
 * Created by saleh on 1/18/14.
 */
public interface Branch {
    //Returns a list of commands that are available
    public String[] getCommands();

    //Parses a command and performs action
    public boolean parseCommand(String command, MainActivity activity);
}
