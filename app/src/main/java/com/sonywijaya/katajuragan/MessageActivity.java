package com.sonywijaya.katajuragan;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
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

import static android.text.Html.fromHtml;

public class MessageActivity extends AppCompatActivity {

    public static final int FROM_HTML_MODE_LEGACY = 0;
    public static final String TAG = MessageActivity.class.getSimpleName();
    private LinearLayoutManager layoutManager;
    private Message message;
    private RecyclerView.Adapter adapter;
    private String userId, token;
    List<Message> messageList;
    @BindView(R.id.messageRecycler) RecyclerView messageRecycler;
    @BindView(R.id.buttonReply) Button buttonReply;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        userId = extras.getString("userId");
        token = extras.getString("token");

        messageList = new ArrayList<>();
        messageRecycler.hasFixedSize();
        layoutManager = new LinearLayoutManager(this);

        buttonReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replyAll();
            }
        });
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

    @Override
    protected void onStart() {
        super.onStart();
        if (isNetworkAvailable()) {
            OkHttpClient client = new OkHttpClient();
            String credential = Credentials.basic(userId, token);
            Request request = new Request.Builder()
                    .url("https://api.bukalapak.com/v2/messages.json?filter=unread")
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
                            getMessage(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (messageList.size() > 0) {
                                        adapter = new MessageAdapter(MessageActivity.this, messageList);
                                        messageRecycler.setAdapter(adapter);
                                        messageRecycler.setLayoutManager(layoutManager);
                                    } else {
                                        Toast.makeText(getApplicationContext(), "No data yet!", Toast.LENGTH_SHORT).show();
                                    }
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
        } else {
            Toast.makeText(getApplicationContext(), R.string.network_unavailable, Toast.LENGTH_LONG).show();
        }
    }

    private void getMessage(String jsonData) throws JSONException {
        JSONObject inbox = new JSONObject(jsonData);
        JSONArray messages = inbox.getJSONArray("inbox");
        for(int i = 0; i < messages.length(); i++) {
            JSONObject jsonobject = messages.getJSONObject(i);
            Message message = new Message();
            message.setId(jsonobject.getString("id"));
            message.setUpdated_at(jsonobject.getString("updated_at"));
            message.setPartner_id(jsonobject.getString("partner_id"));
            message.setPartner_name(jsonobject.getString("partner_name"));
            message.setPartner_avatar(jsonobject.getString("partner_avatar"));
            message.setLast_message(jsonobject.getString("last_message"));
            message.setLast_message_sent(jsonobject.getString("last_message_sent"));
            message.setLast_message_read(jsonobject.getBoolean("last_message_read"));
            messageList.add(message);
        }
    }

    private void replyAll() {
        for(int i = 0; i < messageList.size(); i++) {
            message = messageList.get(i);
            String userMessage = message.getLast_message();
            final String partnerId = message.getPartner_id();
            if (isNetworkAvailable()) {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://api.api.ai/v1/query?v=20150910&query=" + userMessage + "&lang=en&sessionId=" + userId)
                        .get()
                        .addHeader("authorization", "Bearer 77bb893031f9470f986fc7765760d2d0")
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
                                try {
                                    JSONObject apiAiResponse = new JSONObject(jsonData);
                                    JSONObject result = apiAiResponse.getJSONObject("result");
                                    JSONObject fulfillment = result.getJSONObject("fulfillment");
                                    String reply = fulfillment.getString("speech");
                                    generateJSON(reply, partnerId);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
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
            } else {
                Toast.makeText(getApplicationContext(), R.string.network_unavailable, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void replyToUser(String messageJson) {
        if (isNetworkAvailable()) {
            OkHttpClient client = new OkHttpClient();
            String credential = Credentials.basic(userId, token);
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, messageJson);
            Request request = new Request.Builder()
                    .url("https://api.bukalapak.com/v2/messages.json")
                    .post(body)
                    .addHeader("authorization", credential)
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

    private void generateJSON(String fulfillment, String receiver) {
        JSONObject obj = new JSONObject();
        JSONObject instant_message = new JSONObject();

        try {
            obj.put("instant_message", instant_message);
            instant_message.put("receiver_id", receiver);
            instant_message.put("category", "5");
            instant_message.put("body_bb", fulfillment.replaceAll("<br/>","\n"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String jsonString = obj.toString();
        replyToUser(jsonString);
    }
}
