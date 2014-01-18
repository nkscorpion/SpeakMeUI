package com.speakme.speakme;


import android.content.Context;
import android.widget.Toast;

public class RootBranch implements Branch {
    @Override
    public String[] getCommands() {
        String[] ret = {"Twitter", "SendGrid", "Domino's Pizza"};
        return ret;
    }

    @Override
    public boolean parseCommand(String command, Context ctx) {
        return false;
    }
}
