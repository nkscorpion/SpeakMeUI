package com.speakme.speakme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.net.URL;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import twitter4j.Query;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterBranch implements Branch {
    private class Tweet {
        String m_user, m_status;

        Tweet(String user, String status) {
            m_user = user;
            m_status = status;
        }

        String getUser() {
            return m_user;
        }

        String getStatus() {
            return m_status;
        }
    }

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
            cb.setOAuthConsumerKey(m_consumerKey)
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
            class TweetTask extends AsyncTask<String, Void, Boolean> {

                private Exception exception;

                protected Boolean doInBackground(String... tweet) {
                    try {
                        twitter4j.Status status = m_twitter.updateStatus(tweet[0]);
                    } catch (Exception e) {
                        this.exception = e;
                        Log.e("SPEAK ME", "ERROR: " + e);
                        return false;
                    }
                    return true;
                }
            }

            new TweetTask().execute(text);
        }

        public Tweet[] getTimeline() {
            Tweet[] tweets = new Tweet[0];  //I only initialized to avoid the not initialized error
            class TweetTask extends AsyncTask<String, Void, Tweet[]> {

                private Exception exception;

                protected Tweet[] doInBackground(String... params) {
                    Tweet[] tweets = new Tweet[0];
                    try {
                        List<twitter4j.Status> statuses = m_twitter.getHomeTimeline();
                        tweets = new Tweet[statuses.size()];
                        for (int i = 0; i < statuses.size(); i++) {
                            twitter4j.Status status = statuses.get(i);
                            tweets[i] = new Tweet(status.getUser().getName(), status.getText());
                        }
                    } catch(TwitterException te) {
                        Log.e("SPEAK ME", "TwitterException: " + te);
                    } catch (Exception e) {
                        this.exception = e;
                        Log.e("SPEAK ME", "ERROR: " + e);
                    }

                    return tweets;
                }
            }

            try {
                TweetTask tt = new TweetTask();
                tt.execute();
                tweets = tt.get();
            } catch(Exception e) {
                Log.e("SPEAK ME", "ERROR getting feed: " + e);
            }

            return tweets;  //Should not be used
        }
    }

    public static final String PREFS_FILE = "options";
    private TwitterHandler m_th;

    TwitterBranch() {
        m_th = new TwitterHandler("2297275028-U79WUXG20LAquFwsrLS4HH6DhzHOBuzlW4GS47Y", "BF3RfPes7DabVMavYewoQELZkyXzvD202Z5tLJjhvU6oA");
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
    public boolean parseCommand(String command, MainActivity activity) {
        //Check if the user wants to tweet something
        if( (command.toLowerCase().startsWith("tweet") || command.toLowerCase().startsWith("sweet") || command.toLowerCase().startsWith("weed")) && command.length() > 6) {
            m_th.tweet(command.substring(6));
            activity.m_speakMeReader.speakOut("Tweet successful.");
        } else if(command.toLowerCase().contains("feed") || command.toLowerCase().contains("posts")) {
            Tweet[] tweets = m_th.getTimeline();
            int digitIndex = containsDigit(command);
            int postsToRead;
            if(digitIndex != -1) {
                try {
                    postsToRead = Math.min(tweets.length, Integer.parseInt(command.substring(digitIndex, command.indexOf(' ', digitIndex))));
                } catch(Exception e) {
                    Log.d("SPEAK ME", "ERROR: " + e);
                    return false;
                }
            } else {
                postsToRead = tweets.length;
            }

            for(int i = 0; i < postsToRead; i++) {
                activity.m_speakMeReader.speakOut(tweets[i].getUser() + " tweeted " + tweets[i].getStatus());
            }
        } else if(command.toLowerCase().contains("back")) {
            activity.setBranch(new RootBranch());
        }

        return true;
    }

    public final int containsDigit(String s){
        int digitIndex = -1;

        if(s != null){
            for(int i = 0; i < s.length(); i++) {
                if(Character.isDigit(s.charAt(i)))
                    return i;
            }
        }

        return digitIndex;
    }
}
