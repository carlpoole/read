package codes.carl.read;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubredditPaginator;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.UUID;

/**
 * Created by carl on 10/31/15.
 */
public class Application extends android.app.Application {

    public static RedditClient reddit;
    public static Credentials credentials;
    public static SubredditPaginator subredditPaginator;
    public static ArrayList<Submission> currentPage;

    public static void authenticate() {
        new Initiate().execute();
    }

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    private static class Initiate extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            OAuthData authData;
            try {
                Properties props = new Properties();
                props.load(mContext.getAssets().open("reddit.properties"));

                reddit = new RedditClient(UserAgent.of(
                        props.getProperty("platform"),
                        props.getProperty("appID"),
                        props.getProperty("version"),
                        props.getProperty("name")));

                credentials = Credentials.userlessApp(
                        props.getProperty("clientID"),
                        UUID.randomUUID());

                authData = reddit.getOAuthHelper().easyAuth(credentials);
                reddit.authenticate(authData);
                return "";
            } catch (OAuthException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String account) {
            if (account == null)
                Log.e("READ", "AUTHENTICATION ERROR");
        }
    }

}
