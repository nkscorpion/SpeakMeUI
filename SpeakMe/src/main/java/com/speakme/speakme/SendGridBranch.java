package com.speakme.speakme;

import com.github.sendgrid.SendGrid;

/**
 * Created by saleh on 1/18/14.
 */
public class SendGridBranch implements Branch {
    private class AskForEmail implements Branch {
        private String m_toEmail, m_subject, m_messageBody;

        AskForEmail() {
            m_toEmail = "";
            m_subject = "";
            m_messageBody = "";
        }

        AskForEmail(String toEmail, String subject, String messageBody) {
            m_toEmail = toEmail;
            m_subject = subject;
            m_messageBody = messageBody;
        }

        @Override
        public String[] getCommands() {
            return new String[]{"To whom?"};
        }

        @Override
        public boolean parseCommand(String command, MainActivity activity) {
            if(command.toLowerCase().equals("back") || command.toLowerCase().equals("go back")) {
                activity.setBranch(new RootBranch());
            } else if(command.toLowerCase().equals("cancel")) {
                activity.setBranch(new RootBranch());
            } else {
                m_toEmail = command;
                activity.setBranch(new AskForSubject(m_toEmail, m_subject, m_messageBody));
            }
            return false;
        }
    }

    private class AskForSubject implements Branch {
        private String m_toEmail, m_subject, m_messageBody;

        AskForSubject() {
            m_toEmail = "";
            m_subject = "";
            m_messageBody = "";
        }

        AskForSubject(String toEmail, String subject, String messageBody) {
            m_toEmail = toEmail;
            m_subject = subject;
            m_messageBody = messageBody;
        }

        @Override
        public String[] getCommands() {
            return new String[]{"What's the subject?"};
        }

        @Override
        public boolean parseCommand(String command, MainActivity activity) {
            if(command.toLowerCase().equals("back") || command.toLowerCase().equals("go back")) {
                activity.setBranch(new AskForEmail());
            } else if(command.toLowerCase().equals("cancel")) {
                activity.setBranch(new RootBranch());
            } else {
                int indexOfAt = command.lastIndexOf("at");
                String email = new StringBuilder(command).replace(indexOfAt, indexOfAt+2, "@").toString();
                email = email.replace(" ", "");
                m_toEmail = email;
                activity.setBranch(new AskForMessageBody(m_toEmail, m_subject, m_messageBody));
            }
            return false;
        }
    }

    private class AskForMessageBody implements Branch {
        private String m_toEmail, m_subject, m_messageBody;

        AskForMessageBody() {
            m_toEmail = "";
            m_subject = "";
            m_messageBody = "";
        }

        AskForMessageBody(String toEmail, String subject, String messageBody) {
            m_toEmail = toEmail;
            m_subject = subject;
            m_messageBody = messageBody;
        }

        @Override
        public String[] getCommands() {
            return new String[]{"What's the message?"};
        }

        @Override
        public boolean parseCommand(String command, MainActivity activity) {
            if(command.toLowerCase().equals("back") || command.toLowerCase().equals("go back")) {
                activity.setBranch(new AskForSubject(m_toEmail, m_subject, m_messageBody));
            } else if(command.toLowerCase().equals("cancel")) {
                activity.setBranch(new RootBranch());
            } else {
                m_messageBody = command;

                //Log into the SendGrid API
                SendGrid sendgrid = new SendGrid("speakmeteam", "mhacksgt2014");

                //Set the message's details
                sendgrid.setFrom("speakmeteam@gmail.com");
                sendgrid.addTo(m_toEmail);
                sendgrid.setSubject(m_subject);
                sendgrid.setText(m_messageBody);

                //Send the message
                sendgrid.send();

                activity.setBranch(new SendGridBranch());
            }
            return false;
        }
    }
    @Override
    public String[] getCommands() {
        String[] ret = {"Send mail"};
        return ret;
    }

    @Override
    public boolean parseCommand(String command, MainActivity activity) {
        if(command.toLowerCase().contains("send")) {
            activity.setBranch(new AskForEmail());
        } else if(command.toLowerCase().contains("back")) {
            activity.setBranch(new RootBranch());
        }
        return false;
    }
}
