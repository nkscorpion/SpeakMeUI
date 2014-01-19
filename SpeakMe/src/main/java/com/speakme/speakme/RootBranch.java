package com.speakme.speakme;


import android.content.Context;
import android.widget.Toast;

public class RootBranch implements Branch {
    @Override
    public String[] getCommands() {
        String[] ret = {"Twitter", "SendGrid"};
        return ret;
    }

    @Override
    public boolean parseCommand(String command, MainActivity activity) {
        if(command.toLowerCase().contains("twitter")) {
            TwitterBranch tb = new TwitterBranch();
            activity.setBranch(tb);
            if(command.toLowerCase().contains("tweet")) {
                String subcommand = command.substring(command.toLowerCase().indexOf("tweet"));
                tb.parseCommand(subcommand, activity);
            }
            return true;
        } else if(command.toLowerCase().contains("grid") || command.toLowerCase().contains("sounds good")) {
            activity.setBranch(new SendGridBranch());
            return true;
        } else if(command.toLowerCase().contains("exit")) {
            activity.finish();
            return true;
        }

        return false;
    }
}
