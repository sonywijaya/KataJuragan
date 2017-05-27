package com.sonywijaya.katajuragan;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = LoginActivity.class.getSimpleName();
    private EditText editUsername, editPassword;
    private Button buttonSubmit;
    private Authentication authentication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editUsername = (EditText) findViewById(R.id.editUsername);
        editPassword = (EditText) findViewById(R.id.editPassword);
        buttonSubmit = (Button) findViewById(R.id.buttonLogin);
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String username = editUsername.getText().toString();
                final String password = editPassword.getText().toString();
                if (isNetworkAvailable()) {
                    OkHttpClient client = new OkHttpClient();
                    String credential = Credentials.basic(username, password);
                    Request request = new Request.Builder()
                            .url("https://api.bukalapak.com/v2/authenticate.json")
                            .post(RequestBody.create(null, new byte[]{}))
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
                                    authentication = getAuthentication(jsonData);
                                    final String userId = authentication.getUserId();
                                    final String token = authentication.getToken();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            goToMain(userId, token);
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
        });

    }

    private void goToMain(String userId, String token) {
        Intent intent = new Intent(this, MainActivity.class);
        Bundle extras = new Bundle();
        extras.putString("userId", userId);
        extras.putString("token", token);
        intent.putExtras(extras);
        startActivity(intent);
    }

    private Authentication getAuthentication(String jsonData) throws JSONException {
        JSONObject product = new JSONObject(jsonData);
        String uid = product.getString("user_id");
        String token = product.getString("token");
        //Log.i(TAG, "UID: " + uid);
        //Log.i(TAG, "TOKEN: " + token);

        Authentication authentication = new Authentication();
        authentication.setUserId(uid);
        authentication.setToken(token);

        /*JSONArray name = product.getJSONArray("products");
        for(int i = 0; i < name.length(); i++) {
            JSONObject jsonobject = name.getJSONObject(i);
            Log.i(TAG, "Name JSON: " + jsonobject.getString("name"));
            Log.i(TAG, "Name JSON: " + jsonobject.getString("city"));
        }*/
        return authentication;
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
