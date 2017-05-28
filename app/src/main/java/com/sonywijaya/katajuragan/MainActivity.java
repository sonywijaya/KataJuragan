package com.sonywijaya.katajuragan;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity{

    public static final String TAG = MainActivity.class.getSimpleName();
    private LapakInfo lapakInfo;
    @BindView(R.id.textLapakName) TextView textLapakName;
    @BindView(R.id.textLapakDesc) TextView textLapakDesc;
    @BindView(R.id.textLapakLevel) TextView textLapakLevel;
    @BindView(R.id.textLapakOpen) TextView textLapakOpen;
    @BindView(R.id.buttonLogout) Button buttonLogout;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Bundle extras = getIntent().getExtras();
        String userId = extras.getString("userId");
        String token = extras.getString("token");

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
        textLapakName.setText(lapakInfo.getLapakName());

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
        if (user.getString("lapak_name").equals(null)) {
            lapakInfo.setLapakName("Kamu belum bikin lapak.");
        }
        else {
            lapakInfo.setLapakName(user.getString("lapak_name"));
            lapakInfo.setLapakDescription(user.getString("lapak_desc"));
            lapakInfo.setLapakLevel(user.getString("level"));
            lapakInfo.setLapakOpen(user.getString("store_closed"));
        }
        /*JSONArray name = product.getJSONArray("products");
        for(int i = 0; i < name.length(); i++) {
            JSONObject jsonobject = name.getJSONObject(i);
            Log.i(TAG, "Name JSON: " + jsonobject.getString("name"));
            Log.i(TAG, "Name JSON: " + jsonobject.getString("city"));
        }*/
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
}
