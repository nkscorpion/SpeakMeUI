/*package com.speakme.speakme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterBranch implements Branch {
    private class TwitterHandler {
        public static final String CONSUMER_KEY = "n3xPeJgycK3BeBGdjGfSw";
        public static final String CONSUMER_SECRET = "St9OVuvlL8hKNcJlgL1feUE1b8DNj84uNCvzsUfpA";

        public String m_consumerKey;
        public String m_consumerSecret;
        public String m_accessToken;
        public String m_accessSecret;
        public RequestToken m_requestToken;
        Twitter m_twitter;

        TwitterHandler() {
            m_consumerKey = CONSUMER_KEY;
            m_consumerSecret = CONSUMER_SECRET;
            m_accessToken = "";
            m_accessSecret = "";

            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true)
                    .setOAuthConsumerKey(m_consumerKey)
                    .setOAuthConsumerSecret(m_consumerSecret);

            TwitterFactory tf = new TwitterFactory(cb.build());
            m_twitter  = tf.getInstance();
        }

        TwitterHandler(String accessToken, String accessSecret) {
            m_consumerKey = CONSUMER_KEY;
            m_consumerSecret = CONSUMER_SECRET;

            authenticate(accessToken, accessSecret);
        }

        public void authenticate(String token, String secret) {
            m_accessToken = token;
            m_accessSecret = secret;

            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true)
                    .setOAuthConsumerKey(m_consumerKey)
                    .setOAuthConsumerSecret(m_consumerSecret)
                    .setOAuthAccessToken(m_accessToken)
                    .setOAuthAccessTokenSecret(m_accessSecret);

            TwitterFactory tf = new TwitterFactory(cb.build());
            m_twitter  = tf.getInstance();
        }

        public void authenticate(AccessToken at) {
            authenticate(at.getToken(), at.getTokenSecret());
        }

        public String getAuthorizationUrl() {
            String url = "";
            try {
                // get request token.
                // this will throw IllegalStateException if access token is already available
                m_requestToken = m_twitter.getOAuthRequestToken();
                url = m_requestToken.getAuthorizationURL();
            } catch (TwitterException te) {
                te.printStackTrace();
                System.out.println("Failed to get timeline: " + te.getMessage());
                System.exit(-1);
            }

            return url;
        }

        //Returns null if user did not authorize app or entered incorrect pin
        public AccessToken getAccessTokenUsingPin(String pin) {
            AccessToken accessToken = null;

            try {
                if (pin.length() > 0) {
                    accessToken = m_twitter.getOAuthAccessToken(m_requestToken, pin);
                }
            } catch (TwitterException te) {
                if (401 == te.getStatusCode()) {
                    Log.d("TwitterHandler", "Unable to get the access token.");
                } else {
                    //ERROR
                }

                return null;
            }

            return accessToken;
        }

        public void tweet(String text) {
            try {
                Status status = m_twitter.updateStatus(text);
            } catch(TwitterException te) {
                te.printStackTrace();
                System.out.println("Failed to get timeline: " + te.getMessage());
                System.exit(-1);
            }
        }

        public boolean parseCommand(String command) {
            //Check if the user wants to tweet something
            if(command.toLowerCase().startsWith("tweet")) {
                tweet(command.substring(6));
            }

            return true;
        }
    }

    public static final String PREFS_FILE = "options";
    private TwitterHandler m_th;

    TwitterBranch() {
        TwitterHandler th = new TwitterHandler();
    }

    public void authenticate(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS_FILE, 0);
        if(!sp.getString("access_token", "").equals("") && !sp.getString("access_secret", "").equals("")) {
            String accessToken = sp.getString("access_token", "");
            String accessSecret = sp.getString("access_secret", "");
            m_th.authenticate(accessToken, accessSecret);    //Now TwitterHandler is authorized to access user
        } else {
            String authorizationUrl = m_th.getAuthorizationUrl();
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(authorizationUrl));
            ctx.startActivity(i);

            String pin = "123456";  //TODO: ASK USER FOR PIN
            AccessToken accessToken = m_th.getAccessTokenUsingPin(pin);
            if(accessToken != null) {
                sp.edit().putString("access_token", accessToken.getToken())
                        .putString("access_secret", accessToken.getTokenSecret())
                        .commit();
                m_th.authenticate(accessToken);   //Now TwitterHandler is authorized to access user
            }
        }
    }

    @Override
    public String[] getCommands() {
        String[] res = {"Tweet", "View feed"};
        return res;
    }

    @Override
    public boolean parseCommand(String command) {
        //Check if the user wants to tweet something
        if(command.toLowerCase().startsWith("tweet")) {
            m_th.tweet(command.substring(6));
        }

        return true;
    }
}
*/