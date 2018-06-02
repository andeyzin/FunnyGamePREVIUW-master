package com.example.andrey.luckytube;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends YouTubeBaseActivity{

    final static String apiKey = "AIzaSyBtR6GsaU4fUKAI4tZuLJfljOD8fIiF0S8";
    public static Context context;
    public static Machine task;

    public static String videoId = "hello";
    public static String videoTitle;
    public static Bitmap preview;

    public static YouTubePlayerView player;

    public static Resources res;
    public static List<VideoMyClass> videoMyClass;
    public static RVAdapter adapter;
    public static RecyclerView rv;

    public static final String mPath = "base.txt";
    public static final String musicPath = "music_base.txt";
    private static TagBase mTagBase;
    private static List<String> mLines;

    public boolean isFirstStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences getSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

                isFirstStart = getSharedPreferences.getBoolean("firstStart", true);

                if (isFirstStart) {
                    Intent i = new Intent(MainActivity.this, MyIntro.class);
                    startActivity(i);
                    SharedPreferences.Editor e = getSharedPreferences.edit();
                    e.putBoolean("firstStart", false);
                    e.apply();
                }
            }
        });
        t.start();

        player = (YouTubePlayerView)findViewById(R.id.player);

        res = getResources();
        context = this;

        rv = (RecyclerView)findViewById(R.id.rv);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(false);

        videoMyClass = new ArrayList<>();
        initializeData();
        initializeAdapter();

        task = new Machine();
        task.execute("startVideo", videoId, "any");
    }

    private static void initializeData(){
        videoMyClass.add(new VideoMyClass(null, null, null));
    }

    private static void initializeAdapter(){
        adapter = new RVAdapter();
        adapter.notifyDataSetChanged();

        rv.setAdapter(adapter);
        rv.scrollToPosition(0);
    }


    public void onClick(View v) {
        RVAdapter.lastTagValue = RVAdapter.tags.getSelectedItem().toString();
        RVAdapter.lastDurValue = RVAdapter.duration.getSelectedItem().toString();
        RVAdapter.lastEdtValue = RVAdapter.myTag.getText().toString();

        switch(v.getId()){
            //
            //    GENERATE BUTTON
            //
            case R.id.gen:
                super.onStop();
                super.onDestroy();
                super.onCreate(Bundle.EMPTY);

                task = new Machine();
                if (RVAdapter.tags.getSelectedItem().toString().equals("Музыка")){
                    task.execute("startVideo", getTag(), getDuration());
                } else {
                    task.execute("randomWord", getTag(), getDuration());
                }
                break;

            case R.id.space:
                super.onStop();
                super.onDestroy();
                super.onCreate(Bundle.EMPTY);

                startVideo();
                break;
        }

    }

    public void clear(View v) {
        videoMyClass.clear();

        initializeData();
        initializeAdapter();
    }

    public void shareBtn(View v) throws JSONException {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        String textToSend="I found this random video (https://www.youtube.com/watch?v="+videoId + ") with LuckyTube!!";
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, textToSend);
        try {
            startActivity(Intent.createChooser(intent, "Поделиться"));
        }
        catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(), "Some error", Toast.LENGTH_SHORT).show();
        }
    }

    public static String getTag(){
        String stringTag;
        Random r = new Random();
        switch (RVAdapter.tags.getSelectedItem().toString()){
            case  "Свой тег":
                if (!RVAdapter.myTag.getText().toString().isEmpty()){
                    stringTag = RVAdapter.myTag.getText().toString();
                    break;
                }
                Toast.makeText(context, "Пожалуйста введите тег", Toast.LENGTH_SHORT).show();

            case "Без тега":
                String seq = "abcdefghijklmnopqrstuvwxyz0123456789-_";
                String randSeq = "";
                for (int i = 0; i <= 3; i++) {
                    System.out.println(i);
                    randSeq += seq.charAt(r.nextInt(seq.length()));
                }
                stringTag = randSeq;
                break;
            case "Рандомный тег":
                mTagBase = new TagBase(context);
                mLines = mTagBase.readLine(mPath);
                stringTag = mLines.get(r.nextInt(mLines.size()));
                break;

            case "Музыка":
                mTagBase = new TagBase(context);
                mLines = mTagBase.readLine(musicPath);
                stringTag = mLines.get(r.nextInt(mLines.size()));
                break;

            default:
                stringTag = RVAdapter.tags.getSelectedItem().toString();
        }

        return stringTag;
    }

    public static String getDuration(){
        switch (RVAdapter.duration.getSelectedItem().toString()){
            case "Короткие":
                return "short";
            case "Длинные":
                return "long";
            default:
                return "any";
        }
    }

    public static void onFinishTask(){
        videoMyClass.add(1, new VideoMyClass(videoTitle, preview, videoId));

        startVideo();
    }

    public static void startVideo(){

        initializeAdapter();
        player.initialize(apiKey, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, final YouTubePlayer youTubePlayer, boolean b) { if (!b) {
                try {
                    youTubePlayer.loadVideo(videoId);
                } catch (Exception e) {
                    System.out.print(e.getMessage());
                }
            }}

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) { }
        });
    }
}