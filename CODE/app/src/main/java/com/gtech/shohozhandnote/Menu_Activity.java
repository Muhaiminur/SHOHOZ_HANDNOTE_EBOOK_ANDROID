package com.gtech.shohozhandnote;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.gtech.shohozhandnote.Model.Ebook;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import Http.ApiClient;
import Http.ApiInterface;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Menu_Activity extends AppCompatActivity implements View.OnTouchListener, ViewTreeObserver.OnScrollChangedListener {

    MediaPlayer mediaPlayer;
    ApiInterface apiInterface = ApiClient.getBaseClient().create(ApiInterface.class);
    int pos;

    @BindView(R.id.toolbar_tvTitle)
    TextView toolbarTvTitle;
    @BindView(R.id.toolbar_ivNavigation)
    ImageView toolbarIvNavigation;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.btn_down)
    Button btn_down;
    @BindView(R.id.btn_up)
    Button btn_up;
    @BindView(R.id.btn_play)
    Button btn_play;
    @BindView(R.id.btn_stop)
    Button btn_stop;
    @BindView(R.id.flContainerFragment)
    LinearLayout flContainerFragment;
    @BindView(R.id.flContainerNavigationMenu)
    FrameLayout flContainerNavigationMenu;
    @BindView(R.id.drawerLayout)
    DrawerLayout drawerLayout;

    @BindView(R.id.scrollweb)
    ScrollView scrollView;

    @BindView(R.id.webView)
    WebView webView;

    @BindView(R.id.swipeRefreshID)
    SwipeRefreshLayout swipeRefresh;
    Unbinder unbinder;


    ArrayList<Ebook> book_list_all = new ArrayList<>();
    ArrayList<Ebook> menu_list;
    Realm realm;
    static String status,titleNotify,bodyNotify;
    SearchView searchView;
    int next_button = 0;
    String audioFile = null;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_);
        unbinder = ButterKnife.bind(this);
        realm = Realm.getDefaultInstance();

        Intent i = getIntent();
        status = i.getStringExtra("status");

        btn_play.setVisibility(View.INVISIBLE);
        btn_stop.setVisibility(View.INVISIBLE);
        webView.setVerticalScrollBarEnabled(true);


        SharedPreferences EbookDatasharedPreferences = getSharedPreferences("Ebook_Data", MODE_PRIVATE);
        Gson gson2 = new Gson();
        String bjson = EbookDatasharedPreferences.getString("EbookList", null);
        Type type = new TypeToken<ArrayList<Ebook>>() {
        }.getType();
        menu_list = gson2.fromJson(bjson, type);

        if (status.equals("online")) {

            if (menu_list != null && menu_list.size() > 0) {
                replaceNavigationFragment();
                getById(1);
            }
            if(getIntent().hasExtra("body")) //Enters if Any Notification has been clicked
            {
                titleNotify = i.getStringExtra("title");
                bodyNotify = i.getStringExtra("body");

                alertboxNotifications(titleNotify,bodyNotify);
            }

        } else {
            if (menu_list != null && menu_list.size() > 0) {
                replaceNavigationFragment();
                loadofflinedata();
            }
        }

        setToolbarTitle("");
        toolbar.setNavigationIcon(null);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toolbar.setNavigationIcon(null);
        setSupportActionBar(toolbar);

        /*
         // Anik Roy

          Go to previous page using swipe refresh

         */
        swipeRefresh.setOnRefreshListener(() -> {
            if (menu_list.size() > 0 && next_button < menu_list.size() && next_button > 0) {
                if (status.equals("online")) {
                    next_button = next_button - 1;
                    replaceFragment_button(next_button);
                    Navigation_Fragment.adapter.setSelected(next_button);
                    swipeRefresh.setRefreshing(false);
                } else {
                    next_button = next_button - 1;
                    replaceFragmentOffline(next_button);
                    Navigation_Fragment.adapter.setSelected(next_button);
                    swipeRefresh.setRefreshing(false);
                }
            }else {
                swipeRefresh.setRefreshing(false);
            }
        });

        webView.setOnTouchListener(new OnSwipeTouchListener(Menu_Activity.this) {
            @Override
            public void onSwipeTop() {
                //Swipe to next page
                if (scrollView.getScrollY() == 0) {
                    if (menu_list.size() > 0 && next_button + 1 < menu_list.size() && next_button >= 0) {
                        if (status.equals("online")) {
                            next_button = next_button + 1;
                            replaceFragment_button(next_button);
                            Navigation_Fragment.adapter.setSelected(next_button);
                        } else {
                            next_button = next_button + 1;
                            replaceFragmentOffline(next_button);
                            Navigation_Fragment.adapter.setSelected(next_button);
                        }
                    }
                }
            }

        });

        scrollView.setOnTouchListener(Menu_Activity.this);
        webView.getViewTreeObserver().addOnScrollChangedListener(Menu_Activity.this);

        SharedPreferences settings = this.getSharedPreferences("Ebook_Data", MODE_PRIVATE);
        settings.edit().clear().apply();
    }

    //Inflates Search menu in Action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        searchView = (SearchView) searchItem.getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        if (searchView != null)
        {
            /*searchView.setSubmitButtonEnabled(true);
            searchView.setImeOptions(EditorInfo.IME_ACTION_GO);*/
            searchView.setOnQueryTextListener(new QueryTextListener());
            searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean queryTextFocused) {
                    if(!queryTextFocused) {
                        searchItem.collapseActionView();
                        searchView.setQuery("", false);
                        searchView.setIconified(true);
                    }
                }
            });
        }

        return true;
    }

    //Sub class to query on SearchView
    private class QueryTextListener
            implements SearchView.OnQueryTextListener
    {

        // onQueryTextChange
        @Override
        public boolean onQueryTextChange (String newText)
        {
          /*  resultMenu.findAll(newText);*/
            webView.findAllAsync(newText);

            return false;
        }

        // onQueryTextSubmit
        @Override
        public boolean onQueryTextSubmit (String query)
        {
            if(!query.isEmpty()) {
                try {
                    Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(webView, true);
                } catch (Throwable ignored) {
                }
                webView.findNext(true);
            }
            return false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (mediaPlayer!=null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        } catch (Exception we) {
            we.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //Initialise Audio
    private void initialiseAudio(String audiofile) {

        if (audiofile != null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mediaPlayer.setDataSource(audiofile);
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {

                    }
                });
                mediaPlayer.prepareAsync();
            } catch (Exception e) {
                Log.d("Audio error", Log.getStackTraceString(e));
            }
        }

    }


    // Audio url file play
    public void btn_play(View v) {
        try {
            if (isNetworkAvailable()) {
                if (!mediaPlayer.isPlaying()) {
                    // mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
                    mediaPlayer.start();
                    btn_play.setBackgroundResource(R.drawable.ic_pause);
                    Toast.makeText(getApplicationContext(), "Audio Playing...please wait", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("Audio", "works");
                    mediaPlayer.pause();
                    btn_play.setBackgroundResource(R.drawable.ic_play_button);
                    Toast.makeText(getApplicationContext(), "Audio Paused", Toast.LENGTH_SHORT).show();
                }
            } else {
                alertbox("No Internet Connection");
            }
        } catch (Exception e) {
            Log.d("Audio error", Log.getStackTraceString(e));
        }

    }

    //stop audio
    public void btn_stop(View v) {
        if (isNetworkAvailable()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            Toast.makeText(getApplicationContext(), "Audio Stopped", Toast.LENGTH_SHORT).show();
            initialiseAudio(audioFile);
            btn_play.setBackgroundResource(R.drawable.ic_play_button);
        } else {
            alertbox("No Internet Connection");
        }

    }


    //loads first data for offline mode
    private void loadofflinedata() {
        if (menu_list.size() > 0) {
            try {
                if (menu_list.get(0).getContent().contains("===০===")){
                    webView.loadDataWithBaseURL(null, "<style type=\"text/css\">@font-face {font-family: solaimanlipinormal; src: url(\"file:///android_asset/solaimanlipinormal.ttf\")} p{font-family: 'solaimanlipinormal';}</style>   <style>img{display: inline;height: auto;max-width: 100%;}</style>" + menu_list.get(0).getContent().replace("===০===","").concat("<center>===০===</center>") + "<br><br>", "text/html", "utf-8", null);
                }else {
                    webView.loadDataWithBaseURL(null, "<style type=\"text/css\">@font-face {font-family: solaimanlipinormal; src: url(\"file:///android_asset/solaimanlipinormal.ttf\")} p{font-family: 'solaimanlipinormal';}</style>   <style>img{display: inline;height: auto;max-width: 100%;}</style>" + menu_list.get(0).getContent()+ "<br><br>", "text/html", "utf-8", null);
                }
                //resultMenu.loadDataWithBaseURL(null, "<style type=\"text/css\">@font-face {font-family: solaimanlipinormal; src: url(\"file:///android_asset/solaimanlipinormal.ttf\")} p{font-family: 'solaimanlipinormal';}</style>   <style>img{display: inline;height: auto;max-width: 100%;}</style>" + menu_list.get(0).getContent() + "<br><br>", "text/html", "utf-8", null);
                webView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                toolbarTvTitle.setText(menu_list.get(0).getMenu());
                scrollView.fullScroll(ScrollView.FOCUS_UP);
            } catch (Exception e) {
                webView.loadDataWithBaseURL(null, "<style type=\"text/css\">@font-face {font-family: solaimanlipinormal; src: url(\"file:///android_asset/solaimanlipinormal.ttf\")} p{font-family: 'solaimanlipinormal';}</style>   <style>img{display: inline;height: auto;max-width: 100%;}</style> IMAGE NOT AVAILABLE DUE TO LACK OF INTERNET CONNECTION", "text/html", "utf-8", null);
                webView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                toolbarTvTitle.setText(R.string.No_data);
                scrollView.fullScroll(ScrollView.FOCUS_UP);
                Log.d("failed offline data", Log.getStackTraceString(e));
            }
        }
    }

    //Realm offline download button for saving data
    public void download_btn(View v) {
        if (isNetworkAvailable()) {

            saveData();
        } else {

            alertbox("No Internet Connection");

        }
    }


    // No internet connection alert box
    private void alertbox(String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                this);
        builder.setTitle(Html.fromHtml("<font color='#00A6BC'>" + s + "</font>"));
        builder.setMessage("").setCancelable(false)
                .setIcon(R.drawable.ic_app_logo)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
        Button b = alert.getButton(DialogInterface.BUTTON_POSITIVE);

        if (b != null) {
            b.setTextColor(getResources().getColor(R.color.colorPrimary));
        }

    }

    private void alertboxNotifications(String title,String body)
    {
        ViewGroup viewGroup = findViewById(android.R.id.content);

        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(this).inflate(R.layout.notification_alertdialog, viewGroup, false);

        //setting the view of the builder to our custom view that we already inflated
        AlertDialog.Builder builder = new AlertDialog.Builder(
                this);

        TextView notificationAlertTitle=dialogView.findViewById(R.id.NotificationAlertTitle);
        TextView notificationAlertBody=dialogView.findViewById(R.id.NotificationAlertBody);
        Button notificationAlertButton=dialogView.findViewById(R.id.okNotification);

        notificationAlertTitle.setText(title);
        notificationAlertBody.setText(body);
        builder.setView(dialogView);

        AlertDialog alert = builder.create();
        notificationAlertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    alert.dismiss();
            }
        });
        alert.show();
    }

    //Previous page navigation button

    public void btn_up_click(View v) {

        if (menu_list.size() > 0 && next_button < menu_list.size() && next_button > 0) {
            if (status.equals("online")) {
                next_button = next_button - 1;
                replaceFragment_button(next_button);
                Navigation_Fragment.adapter.setSelected(next_button);
            } else {
                next_button = next_button - 1;
                replaceFragmentOffline(next_button);
                Navigation_Fragment.adapter.setSelected(next_button);
            }
        }

    }

    //Next page navigation button

    public void btn_down_click(View v) {

        if (menu_list.size() > 0 && next_button + 1 < menu_list.size() && next_button >= 0) {
            if (status.equals("online")) {
                next_button = next_button + 1;
                replaceFragment_button(next_button);
                Navigation_Fragment.adapter.setSelected(next_button);
            } else {
                next_button = next_button + 1;
                replaceFragmentOffline(next_button);
                Navigation_Fragment.adapter.setSelected(next_button);
            }
        }
    }

    //Checking network connection

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    @OnClick({R.id.toolbar_tvTitle, R.id.toolbar_ivNavigation, R.id.drawerLayout})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.toolbar_tvTitle:
                break;
            case R.id.toolbar_ivNavigation:
                openCloseDrawer();
                break;
            case R.id.drawerLayout:
                break;
        }
    }

    //Offline mode: Getting content data
    public void getContentOffline(Ebook id) {
        if (menu_list.size() > 0) {
            try {
                if (id.getContent().length() < 700) {
                    if (id.getContent().contains("===০===")){
                        webView.loadDataWithBaseURL(null, "<style type=\"text/css\">@font-face {font-family: solaimanlipinormal; src: url(\"file:///android_asset/solaimanlipinormal.ttf\")} p{font-family: 'solaimanlipinormal';}</style>   <style>img{display: inline;height: auto;max-width: 100%;}</style>" + id.getContent().replace("===০===","").concat("<center>===০===</center>") + "<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>", "text/html", "utf-8", null);
                    }else {
                        webView.loadDataWithBaseURL(null, "<style type=\"text/css\">@font-face {font-family: solaimanlipinormal; src: url(\"file:///android_asset/solaimanlipinormal.ttf\")} p{font-family: 'solaimanlipinormal';}</style>   <style>img{display: inline;height: auto;max-width: 100%;}</style>" + id.getContent()+ "<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>", "text/html", "utf-8", null);
                    }
                    //resultMenu.loadDataWithBaseURL(null, "<style type=\"text/css\">@font-face {font-family: solaimanlipinormal; src: url(\"file:///android_asset/solaimanlipinormal.ttf\")} p{font-family: 'solaimanlipinormal';}</style>   <style>img{display: inline;height: auto;max-width: 100%;}</style>" + id.getContent() + "<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>", "text/html", "utf-8", null);
                    webView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    toolbarTvTitle.setText(id.getMenu());
                } else {
                    if (id.getContent().contains("===০===")){
                        webView.loadDataWithBaseURL(null, "<style type=\"text/css\">@font-face {font-family: solaimanlipinormal; src: url(\"file:///android_asset/solaimanlipinormal.ttf\")} p{font-family: 'solaimanlipinormal';}</style>   <style>img{display: inline;height: auto;max-width: 100%;}</style>" + id.getContent().replace("===০===","").concat("<center>===০===</center>") + "<br><br>", "text/html", "utf-8", null);
                    }else {
                        webView.loadDataWithBaseURL(null, "<style type=\"text/css\">@font-face {font-family: solaimanlipinormal; src: url(\"file:///android_asset/solaimanlipinormal.ttf\")} p{font-family: 'solaimanlipinormal';}</style>   <style>img{display: inline;height: auto;max-width: 100%;}</style>" + id.getContent()+ "<br><br>", "text/html", "utf-8", null);
                    }
                    //resultMenu.loadDataWithBaseURL(null, "<style type=\"text/css\">@font-face {font-family: solaimanlipinormal; src: url(\"file:///android_asset/solaimanlipinormal.ttf\")} p{font-family: 'solaimanlipinormal';}</style>   <style>img{display: inline;height: auto;max-width: 100%;}</style>" + id.getContent() + "<br><br>", "text/html", "utf-8", null);
                    webView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    toolbarTvTitle.setText(id.getMenu());
                }
                scrollView.fullScroll(ScrollView.FOCUS_UP);
            } catch (Exception e) {
                webView.loadDataWithBaseURL(null, "<style type=\"text/css\">@font-face {font-family: solaimanlipinormal; src: url(\"file:///android_asset/solaimanlipinormal.ttf\")} p{font-family: 'solaimanlipinormal';}</style>   <style>img{display: inline;height: auto;max-width: 100%;}</style> IMAGE NOT AVAILABLE DUE TO LACK OF INTERNET CONNECTION", "text/html", "utf-8", null);
                toolbarTvTitle.setText(R.string.No_data);
                scrollView.fullScroll(ScrollView.FOCUS_UP);
                Log.d("failed offline data", Log.getStackTraceString(e));
            }
        }


    }

    //Offline mode: Put data in navigation bar
    public void replaceFragmentOffline(int position) {
        if (status.equals("offline")) {
            pos = position;
            if (position < menu_list.size()) {
                Ebook ebook = null;
                for (Ebook e : menu_list) {
                    if (e.getId().equalsIgnoreCase(menu_list.get(position).getId())) {
                        ebook = e;
                    }
                }
                if (ebook != null) {
                    next_button = position;
                    getContentOffline(ebook);
                }
            }

            closeNavigationDrawer();
        }
    }


    //Fetch api content of ebook by ID Online

    private void getById(int id) {

        Call<JsonElement> call = apiInterface.getbookmenubyid("Basic YWRtaW46R3RlY2hFYm9vaw==", String.valueOf(id));
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                try {

                    if (response.isSuccessful() && response.code() == 200) {

                        Gson gson = new Gson();
                        Type listType = new TypeToken<List<Ebook>>() {
                        }.getType();
                        List<Ebook> Blog_List_Response = gson.fromJson(response.body(), listType);
                        if (Blog_List_Response.size() > 0) {

                            if (Blog_List_Response.get(0).getAudio() != null) {
                                btn_play.setVisibility(View.VISIBLE);
                                btn_stop.setVisibility(View.VISIBLE);
                                if (mediaPlayer != null) {
                                    if (mediaPlayer.isPlaying()) {
                                        mediaPlayer.stop();
                                        mediaPlayer.release();
                                        mediaPlayer = null;
                                        btn_play.setBackgroundResource(R.drawable.ic_play_button);
                                    }
                                }
                                audioFile = Blog_List_Response.get(0).getAudio();
                            } else {
                                if (mediaPlayer != null) {
                                    if (mediaPlayer.isPlaying()) {
                                        mediaPlayer.stop();
                                        mediaPlayer.release();
                                        mediaPlayer = null;
                                        btn_play.setBackgroundResource(R.drawable.ic_play_button);
                                    }
                                }
                                btn_play.setVisibility(View.INVISIBLE);
                                btn_stop.setVisibility(View.INVISIBLE);
                                audioFile = null;
                            }

                            if (Blog_List_Response.get(0).getContent().length() < 700) {
                                // sc.fullScroll(ScrollView.FOCUS_UP);
                                Log.d("Audio11", " " + Blog_List_Response.get(0).getAudio());
                                if (Blog_List_Response.get(0).getContent().contains("===০===")){
                                    webView.loadDataWithBaseURL(null, "<style type=\"text/css\">@font-face {font-family: solaimanlipinormal; src: url(\"file:///android_asset/solaimanlipinormal.ttf\")} p{font-family: 'solaimanlipinormal';}</style>   <style>img{display: inline;height: auto;max-width: 100%;}</style>" + Blog_List_Response.get(0).getContent().replace("===০===","").concat("<center>===০===</center>") + "<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>", "text/html", "utf-8", null);
                                }
                                else if(Blog_List_Response.get(0).getContent().contains("==০==")){
                                    webView.loadDataWithBaseURL(null, "<style type=\"text/css\">@font-face {font-family: solaimanlipinormal; src: url(\"file:///android_asset/solaimanlipinormal.ttf\")} p{font-family: 'solaimanlipinormal';}</style>   <style>img{display: inline;height: auto;max-width: 100%;}</style>" + Blog_List_Response.get(0).getContent().replace("==০==","").concat("<center>===০===</center>") + "<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>", "text/html", "utf-8", null);
                                }
                                else{
                                    webView.loadDataWithBaseURL(null, "<style type=\"text/css\">@font-face {font-family: solaimanlipinormal; src: url(\"file:///android_asset/solaimanlipinormal.ttf\")} p{font-family: 'solaimanlipinormal';}</style>   <style>img{display: inline;height: auto;max-width: 100%;}</style>" + Blog_List_Response.get(0).getContent()+ "<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>", "text/html", "utf-8", null);
                                }

                                //resultMenu.loadDataWithBaseURL(null, "<style type=\"text/css\">@font-face {font-family: solaimanlipinormal; src: url(\"file:///android_asset/solaimanlipinormal.ttf\")} p{font-family: 'solaimanlipinormal';}</style>   <style>img{display: inline;height: auto;max-width: 100%;}</style>" + Blog_List_Response.get(0).getContent().replace("===০===","").concat("<center>===০===</center>") /*+ "<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>"*/, "text/html", "utf-8", null);
                                webView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                //  sc.setScrollY(0);
                                toolbarTvTitle.setText(Blog_List_Response.get(0).getMenu());


                            } else {
                                if (Blog_List_Response.get(0).getContent().contains("===০===")){
                                    webView.loadDataWithBaseURL(null, "<style type=\"text/css\">@font-face {font-family: solaimanlipinormal; src: url(\"file:///android_asset/solaimanlipinormal.ttf\")} p{font-family: 'solaimanlipinormal';}</style>   <style>img{display: inline;height: auto;max-width: 100%;}</style>" + Blog_List_Response.get(0).getContent().replace("===০===","").concat("<center>===০===</center>") + "<br><br>", "text/html", "utf-8", null);
                                }
                                else if(Blog_List_Response.get(0).getContent().contains("==০==")){
                                    webView.loadDataWithBaseURL(null, "<style type=\"text/css\">@font-face {font-family: solaimanlipinormal; src: url(\"file:///android_asset/solaimanlipinormal.ttf\")} p{font-family: 'solaimanlipinormal';}</style>   <style>img{display: inline;height: auto;max-width: 100%;}</style>" + Blog_List_Response.get(0).getContent().replace("==০==","").concat("<center>===০===</center>") + "<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>", "text/html", "utf-8", null);
                                }
                                else {
                                    webView.loadDataWithBaseURL(null, "<style type=\"text/css\">@font-face {font-family: solaimanlipinormal; src: url(\"file:///android_asset/solaimanlipinormal.ttf\")} p{font-family: 'solaimanlipinormal';}</style>   <style>img{display: inline;height: auto;max-width: 100%;}</style>" + Blog_List_Response.get(0).getContent()+ "<br><br>", "text/html", "utf-8", null);
                                }
                                webView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                //   sc.setScrollY(0);
                                toolbarTvTitle.setText(Blog_List_Response.get(0).getMenu());

                            }
                            initialiseAudio(audioFile);
                            scrollView.setScrollY(0);
                            scrollView.fullScroll(ScrollView.FOCUS_UP);
                        }
                    }
                } catch (Exception e) {
                    Log.d("fail to load api by id", Log.getStackTraceString(e));
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.d("on failure", t.toString());

            }
        });

    }

    //Navigation drawer open/close
    private void openCloseDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @SuppressLint("RtlHardcoded")
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //Fills navigation drawer with menu api data

    public void replaceNavigationFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flContainerNavigationMenu, Navigation_Fragment.newInstance(fillData(menu_list)), "Navigation").commit();

    }

    public void setToolbarTitle(String title) {
        toolbarTvTitle.setText(title);
    }

    public void closeNavigationDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    //Replaces fragment i.e. chapters of ebook on click in navigation drawer

    public void replaceFragment(int position) {
        pos = position;
        String tag = null;

        if (position < menu_list.size()) {
            next_button = position;
            drawerLayout.closeDrawer(GravityCompat.START);
            new Send_data_task(menu_list.get(position).getId()).execute("my string parameter");
        }
        setToolbarTitle(tag);
        closeNavigationDrawer();

    }


    public void replaceFragment_button(int i) {
        drawerLayout.closeDrawer(GravityCompat.START);
        new Send_data_task(menu_list.get(i).getId()).execute("my string parameter");
        closeNavigationDrawer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    //Shows Navigation bar list data according to menu-0 api

    private ArrayList<NavigationData> fillData(List<Ebook> l) {
        ArrayList<NavigationData> navigationDataArrayList = new ArrayList<>();

        for (int i = 0; i < l.size(); i++) {
            NavigationData navigationData = new NavigationData();
            navigationData.setName(l.get(i).getMenu());
            navigationDataArrayList.add(navigationData);
        }

        return navigationDataArrayList;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        this.onTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    //Scroll down to next page
    @Override
    public void onScrollChanged() {
        View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
        int bottomDetector = view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY());

        if (bottomDetector == 0) {

            if (menu_list.size() > 0 && next_button + 1 < menu_list.size() && next_button >= 0) {
                if (status.equals("online")) {
                    next_button = next_button + 1;
                    replaceFragment_button(next_button);
                    Navigation_Fragment.adapter.setSelected(next_button);
                    //Navigation_Fragment.adapter.setSelected(pos);
                } else {
                /*replaceFragmentOffline(Integer.parseInt(menu_list.get(pos).getId()));
                Navigation_Fragment.adapter.setSelected(pos);*/
                    next_button = next_button + 1;
                    replaceFragmentOffline(next_button);
                    Navigation_Fragment.adapter.setSelected(next_button);
                }
            }


        }

    }


    //AsyncTask for dialog box

    private class Send_data_task extends AsyncTask<String, Integer, String> {

        ProgressDialog progressDialog4;

        // Runs in UI before background thread is called
        String sendData;

        Send_data_task(String s) {
            sendData = s;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Do something like display a progress bar
            progressDialog4 = ProgressDialog.show(Menu_Activity.this, "Loading", "Please Wait!");

            progressDialog4.setCanceledOnTouchOutside(false);
        }

        // This is run in a background thread
        @Override
        protected String doInBackground(String... params) {
            try {
                Thread.sleep(500);

                getById(Integer.parseInt(sendData));

            } catch (Exception e) {
                // Log.d("Paisi send data 2", e.toString());
            }

            return "this string is passed to onPostExecute";
        }

        // This is called from background thread but runs in UI
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            // Do things like update the progress bar
        }

        // This runs in UI when background thread finishes
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Do things like hide the progress bar or change a TextView
            progressDialog4.dismiss();
        }
    }


// Getting all menu api & saving to Realm database for offline usage

    private void saveData() {
        ProgressDialog progressDialog4;

        progressDialog4 = ProgressDialog.show(Menu_Activity.this, "Loading", "Please Wait!");

        progressDialog4.setCanceledOnTouchOutside(false);

        Call<JsonElement> call = apiInterface.getbookmenu("Basic YWRtaW46R3RlY2hFYm9vaw==");
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                try {
                    if (response.isSuccessful() && response.code() == 200) {

                        Gson gson = new Gson();
                        Type listType = new TypeToken<List<Ebook>>() {
                        }.getType();
                        List<Ebook> Blog_List_Response = gson.fromJson(response.body(), listType);
                        if (Blog_List_Response.size() > 0) {

                            book_list_all.clear();
                            book_list_all.addAll(Blog_List_Response);
                            realm.beginTransaction();

                            RealmResults<Ebook> new_result = realm.where(Ebook.class).findAll();

                            new_result.deleteAllFromRealm();

                            realm.insert(book_list_all);

                            realm.commitTransaction();

                        }
                        progressDialog4.dismiss();
                        alertbox("Offline Mode Ebook Downloaded");
                    }
                } catch (Exception e) {
                    Log.d("failed all menu api", Log.getStackTraceString(e));
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.d("on failure", t.toString());
            }
        });
    }
}


