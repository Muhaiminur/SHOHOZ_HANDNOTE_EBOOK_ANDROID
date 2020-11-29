package com.gtech.shohozhandnote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.gtech.shohozhandnote.Model.Ebook;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import Http.ApiClient;
import Http.ApiInterface;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    List<Ebook> Blog_List_Response;
    Realm realm;

    ArrayList<Ebook> book_list = new ArrayList<>();
    String title, body;
    ApiInterface apiInterface = ApiClient.getBaseClient().create(ApiInterface.class);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //createNotificationChannel();
        FirebaseMessaging.getInstance().subscribeToTopic("general")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });

        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().name("EbookRealm.realm").schemaVersion(1)
                .deleteRealmIfMigrationNeeded().build();
        Realm.setDefaultConfiguration(config);
        realm = Realm.getDefaultInstance();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                Log.d(TAG, "Extras received at onCreate:  Key: " + key + " Value: " + value);
            }
            title = extras.getString("title");
            body = extras.getString("body");
            Log.d(TAG, "Extras received  " + title + " Value: " + body);
        }

        if (isNetworkAvailable()) {
            loadMenuData();
        } else {
            checkData();
        }


    }

    //During offline: Checks if any downloaded data available
    private void checkData() {

        realm.beginTransaction();

        ArrayList<Ebook> new_list = new ArrayList<>();
        RealmResults<Ebook> new_result = realm.where(Ebook.class).findAll();

        new_list.addAll(realm.copyFromRealm(new_result));

        realm.commitTransaction();

        if (new_list.size() > 0) {
            SharedPreferences EbookDatasharedPreferences = getSharedPreferences("Ebook_Data", MODE_PRIVATE);
            SharedPreferences.Editor editor = EbookDatasharedPreferences.edit();
            Gson gson1 = new Gson();
            String ajson = gson1.toJson(new_list);
            editor.putString("EbookList", ajson);
            editor.apply();

            Intent i = new Intent(getApplicationContext(), Menu_Activity.class);
            i.putExtra("status", "offline");
            startActivity(i);
            finish();
        } else {
            alertBox("No Internet Connection.");
        }

    }

    //alert box for no internet
    private void alertBox(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(
                this);
        builder.setTitle(Html.fromHtml("<font color='#00A6BC'>" + msg + "</font>"));
        builder.setMessage("").setCancelable(false)
                .setIcon(R.drawable.ic_app_logo)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });


        AlertDialog alert = builder.create();

        alert.show();

        Button okbtn = alert.getButton(DialogInterface.BUTTON_POSITIVE);

        if (okbtn != null) {
            okbtn.setTextColor(getResources().getColor(R.color.colorPrimary));
        }

    }


//Checking network connection

    private boolean isNetworkAvailable() {

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();

    }


//Loading menu data in splash screen and passing through intent

    private void loadMenuData() {

        Call<JsonElement> call = apiInterface.getbookmenuonly("Basic YWRtaW46R3RlY2hFYm9vaw==");
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                try {
                    if (response.isSuccessful() && response.code() == 200) {

                        Gson gson = new Gson();
                        Type listType = new TypeToken<List<Ebook>>() {
                        }.getType();
                        Blog_List_Response = gson.fromJson(response.body(), listType);
                        if (Blog_List_Response.size() > 0) {
                            book_list.addAll(Blog_List_Response);
                        }


                        SharedPreferences EbookDatasharedPreferences = getSharedPreferences("Ebook_Data", MODE_PRIVATE);
                        SharedPreferences.Editor editor = EbookDatasharedPreferences.edit();
                        Gson gson1 = new Gson();
                        String ajson = gson1.toJson(book_list);
                        editor.putString("EbookList", ajson);
                        editor.apply();

                        if (book_list.size() > 0) {
                            Intent i = new Intent(getApplicationContext(), Menu_Activity.class);
                            i.putExtra("status", "online");
                            if (getIntent().hasExtra("body")) {
                                i.putExtra("title", title);
                                i.putExtra("body", body);
                            }
                            startActivity(i);
                            finish();
                        }

                    }
                } catch (Exception e) {
                    Log.d("Failed to hit menu api", Log.getStackTraceString(e));
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.d("On Failure to hit api", t.toString());
            }
        });
    }


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onDestroy() {
        SharedPreferences EbookDatasharedpreferences = getSharedPreferences("Ebook_Data", MODE_PRIVATE);
        SharedPreferences.Editor editor = EbookDatasharedpreferences.edit();
        editor.clear();
        editor.apply();
        super.onDestroy();
    }
}


