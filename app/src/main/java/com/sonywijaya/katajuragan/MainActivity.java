package com.sonywijaya.katajuragan;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity{

    public static final String TAG = MainActivity.class.getSimpleName();
    private LapakInfo lapakInfo;
    private Produk produk;
    List<Produk> produkList;
    @BindView(R.id.textUserName) TextView textUserName;
    @BindView(R.id.textLapakName) TextView textLapakName;
    @BindView(R.id.textLapakDesc) TextView textLapakDesc;
    @BindView(R.id.textLapakLevel) TextView textLapakLevel;
    @BindView(R.id.textLapakOpen) TextView textLapakOpen;
    @BindView(R.id.buttonLogout) Button buttonLogout;
    SessionManager session;
    @BindView(R.id.buttonInbox) Button buttonInbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        produkList = new ArrayList<>();
        Bundle extras = getIntent().getExtras();
        final String userId = extras.getString("userId");
        final String token = extras.getString("token");
        myLapak(userId, token);
        buttonInbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openInbox(userId, token);
            }
        });

        session = new SessionManager(getApplicationContext());
        Toast.makeText(getApplicationContext(), "User Login Status: " + session.isLoggedIn(), Toast.LENGTH_LONG).show();
        session.checkLogin();
        HashMap<String, String> user = session.getUserDetails();

        // name
        String name = user.get(SessionManager.username);

        // email
//        String email = user.get(SessionManager.pass);
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                session.logoutUser();
//                logout();
            }
        });

        Log.i(TAG, "USER ID: " + userId);

        if (isNetworkAvailable()) {
            OkHttpClient client = new OkHttpClient();
            //String credential = Credentials.basic(userId, token);
            Request request = new Request.Builder()
                    .url("https://api.bukalapak.com/v2/users/" + userId + "/profile.json")
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                            lapakInfo = getLapakInfo(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });
                        }
                        else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    alertError();
                                }
                            });
                        }
                    }
                    catch (IOException e){
                        Log.d(TAG, "Exception caught: ", e);
                    }
                    catch (JSONException e) {
                        Log.d(TAG, "Exception caught: ", e);
                    }
                }
            });
        }
        else {
            Toast.makeText(getApplicationContext(), R.string.network_unavailable, Toast.LENGTH_LONG).show();
        }

    }

    public void onBackPressed(){
        moveTaskToBack(true);
    }

    private void logout(){
//        SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.MyPreferences, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.clear();
//        editor.commit();
    }

    private void updateDisplay() {
        textUserName.setText("Hi, " + lapakInfo.getUserName() + "!");
        textLapakName.setText(lapakInfo.getLapakName());
        textLapakDesc.setText(lapakInfo.getLapakDescription());
    }

    private void goToMain(String userId, String token) {
        Intent intent = new Intent(this, MainActivity.class);
        Bundle extras = new Bundle();
        extras.putString("userId", userId);
        extras.putString("token", token);
        intent.putExtras(extras);
        startActivity(intent);
    }

    private LapakInfo getLapakInfo(String jsonData) throws JSONException {
        JSONObject profile = new JSONObject(jsonData);
        JSONObject user = profile.getJSONObject("user");
        LapakInfo lapakInfo = new LapakInfo();
        if (user.getString("lapak_name").equals("null")) {
            lapakInfo.setUserName(user.getString("name"));
            lapakInfo.setLapakName("Kamu belum setting nama lapak.");
        }
        else {
            lapakInfo.setUserName(user.getString("name"));
            lapakInfo.setLapakName(user.getString("lapak_name"));
            lapakInfo.setLapakDescription(user.getString("lapak_desc"));
            lapakInfo.setLapakLevel(user.getString("level"));
            lapakInfo.setLapakOpen(user.getString("store_closed"));
        }
        return lapakInfo;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

    private void alertError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.error_title)
                .setMessage(R.string.error_message)
                .setPositiveButton(R.string.error_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        builder.show();
    }

    private void openInbox(String userId, String token) {
        Intent intent = new Intent(this, MessageActivity.class);
        Bundle extras = new Bundle();
        extras.putString("userId", userId);
        extras.putString("token", token);
        intent.putExtras(extras);
        startActivity(intent);
    }

    private String generateJSON(String userId) {
        JSONObject obj = new JSONObject();
        JSONArray entities = new JSONArray();
        JSONObject entity = new JSONObject();
        JSONArray entries = new JSONArray();
        entities.put(entity);
        try {
            obj.put("sessionId", userId);
            obj.put("entities", entities);
            entity.put("name", "produk");
            entity.put("entries", entries);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (int i = 0; i<produkList.size(); i++) {
            produk = produkList.get(i);
            JSONObject entryValue = new JSONObject();
            JSONArray synonyms = new JSONArray();
            try {
                entryValue.put("value", produk.getName());
                synonyms.put(produk.getName());
                entries.put(entryValue);
                entryValue.put("synonyms", synonyms);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        String jsonString = obj.toString();
        sendEntity(jsonString);
        return jsonString;
    }

    private void getProdukList(String jsonData) throws JSONException {
        produkList.clear();
        JSONObject myLapak = new JSONObject(jsonData);
        JSONArray products = myLapak.getJSONArray("products");
        for(int i = 0; i < products.length(); i++) {
            JSONObject jsonobject = products.getJSONObject(i);
            Produk produk = new Produk();
            produk.setName(jsonobject.getString("name"));
            produkList.add(produk);
        }
    }

    private void myLapak(final String userId, String token) {
        if (isNetworkAvailable()) {
            OkHttpClient client = new OkHttpClient();
            String credential = Credentials.basic(userId, token);
            Request request = new Request.Builder()
                    .url("https://api.bukalapak.com/v2/products/mylapak.json")
                    .get()
                    .addHeader("authorization", credential)
                    .addHeader("cache-control", "no-cache")
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                            getProdukList(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.i(TAG, "JSON NYA: " + generateJSON(userId));
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    alertError();
                                }
                            });
                        }
                    } catch (IOException e) {
                        Log.d(TAG, "Exception caught: ", e);
                    } catch (JSONException e) {
                        Log.d(TAG, "Exception caught: ", e);
                    }
                }
            });
        }
        else {
            Toast.makeText(getApplicationContext(), R.string.network_unavailable, Toast.LENGTH_LONG).show();
        }
    }

    private void sendEntity(String productJson){
        if (isNetworkAvailable()) {
            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, productJson);
            Request request = new Request.Builder()
                    .url("https://api.api.ai/v1/userEntities?v=20150910&sessionId=12345")
                    .post(body)
                    .addHeader("authorization", "Bearer 1113a7cb4e2c4ecb96def948d4f14169")
                    .addHeader("content-type", "application/json")
                    .addHeader("cache-control", "no-cache")
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        final String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.i(TAG, "RESPON: " + jsonData);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    alertError();
                                }
                            });
                        }
                    } catch (IOException e) {
                        Log.d(TAG, "Exception caught: ", e);
                    }
                }
            });
        }
        else {
            Toast.makeText(getApplicationContext(), R.string.network_unavailable, Toast.LENGTH_LONG).show();
        }
    }
}
