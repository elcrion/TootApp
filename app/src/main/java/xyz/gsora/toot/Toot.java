package xyz.gsora.toot;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by gsora on 4/20/17.
 * <p>
 * A class containing application-wide methods.
 */
public class Toot extends Application {

    private static Context context;

    public static Context getAppContext() {
        return Toot.context;
    }

    public static SharedPreferences prefsFactory() {
        return getAppContext().getSharedPreferences("TOOT_APP", Context.MODE_PRIVATE);
    }

    public static SharedPreferences.Editor editorFactory() {
        return prefsFactory().edit();
    }

    public static Boolean hasLoggedIn() {
        return prefsFactory().getBoolean("LOGGED_IN", false);
    }

    public static void markLoggedIn() {
        editorFactory().putBoolean("LOGGED_IN", true).apply();
    }

    public static String getUsername() {
        return prefsFactory().getString("USERNAME", null);
    }

    public static String getInstanceURI() {
        return prefsFactory().getString("INSTANCE", null);
    }

    public static String getInstanceURL() {
        return "https://" + getInstanceURI();
    }

    public static void saveUsernameInstanceTuple(UserTuple t) {
        SharedPreferences.Editor se = editorFactory();
        se.putString("USERNAME", t.getUser());
        se.putString("INSTANCE", t.getInstanceURI());
        se.apply();
    }

    public static void saveClientID(String clientID) {
        SharedPreferences.Editor se = editorFactory();
        se.putString("CLIENT_ID", clientID);
        se.apply();
    }

    public static String getClientID() {
        return prefsFactory().getString("CLIENT_ID", null);
    }

    public static void saveClientSecret(String clientSecret) {
        SharedPreferences.Editor se = editorFactory();
        se.putString("CLIENT_SECRET", clientSecret);
        se.apply();
    }

    public static String getClientSecret() {
        return prefsFactory().getString("CLIENT_SECRET", null);
    }

    public static void saveOAuthAccessToken(String accessToken) {
        SharedPreferences.Editor se = editorFactory();
        se.putString("ACCESS_TOKEN", accessToken);
        se.apply();
    }

    public static String getOAuthAccessToken() {
        return prefsFactory().getString("ACCESS_TOKEN", null);
    }

    public static void saveOAuthRefreshToken(String refreshToken) {
        SharedPreferences.Editor se = editorFactory();
        se.putString("REFRESH_TOKEN", refreshToken);
        se.apply();
    }

    public static String getOAuthRefreshToken() {
        return prefsFactory().getString("REFRESH_TOKEN", null);
    }

    public static String debugSettingsStorage() {
        StringBuilder s = new StringBuilder("Toot settings storage contents: ");
        s.append("\n\tLogged in: " + hasLoggedIn().toString());
        s.append("\n\tUsername: " + getUsername());
        s.append("\n\tInstance URL: " + getInstanceURL());
        s.append("\n\tClient ID: " + getClientID());
        s.append("\n\tClient secret: " + getClientSecret());
        s.append("\n\tAccess token:" + getOAuthAccessToken());
        s.append("\n\tRefresh token: " + getOAuthRefreshToken());

        return s.toString();
    }

    public static String buildBearer() {
        return "Bearer " + getOAuthAccessToken();
    }

    public static Realm getRealm() {
        return Realm.getInstance(
                new RealmConfiguration.Builder()
                        .deleteRealmIfMigrationNeeded()
                        .build());
    }

    public void onCreate() {
        super.onCreate();
        Toot.context = getApplicationContext();

        Realm.init(context);
    }

}
