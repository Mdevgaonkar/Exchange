package flo.org.exchange.app.utils;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import flo.org.exchange.app.Login.Person;

import static android.content.ContentValues.TAG;


/**
 * Created by Mayur on 31/10/16.
 */

public class campusExchangeApp extends Application {

    private RequestQueue mRequestQueue;



    private Person universalPerson;



    private static final String application_id = "application-id";
    private static final String secret_key= "secret-key";
    private String APP_ID=null;
    private String SEC_KEY=null;

    private Gson gson;

    private static campusExchangeApp mInstance;
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
        setupPerson();
    }

    private void setupPerson() {
        universalPerson = new Person(mInstance.getApplicationContext());
    }

    public Person getUniversalPerson() {
        return universalPerson;
    }


    public static synchronized campusExchangeApp getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }



    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }



    public void getCredentialsIn(){
        String credentials = null;
        credentials c = new credentials(mInstance.getApplicationContext());
        credentials = c.getCredentials();

        try {
            JSONObject JSONcredentials = new JSONObject(credentials);
            String app_id = JSONcredentials.getString(application_id);
            String sec_key = JSONcredentials.getString(secret_key);
            setAPP_ID(app_id);
            setSEC_KEY(sec_key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public String getApplication_id() {
        return application_id;
    }

    public String getSecret_key() {
        return secret_key;
    }



    public String getAPP_ID() {
        return APP_ID;
    }

    public void setAPP_ID(String APP_ID) {
        this.APP_ID = APP_ID;
    }

    public String getSEC_KEY() {
        return SEC_KEY;
    }

    public void setSEC_KEY(String SEC_KEY) {
        this.SEC_KEY = SEC_KEY;
    }

    public HashMap<String,String> getCredentialsHashMap(){
        getCredentialsIn();
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put(getApplication_id(), getAPP_ID());
        headers.put(getSecret_key(), getSEC_KEY());
        return headers;
    }

    public Gson getGson() {

        return gson;
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }
}
