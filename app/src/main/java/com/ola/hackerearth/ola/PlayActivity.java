package com.ola.hackerearth.ola;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;

public class PlayActivity extends AppCompatActivity {

    String title,imageUrl,trackUrl;
    JSONArray artistsJSON;
    String[] artists;

    private ImageView play;
    private boolean playPause;
    private MediaPlayer mediaPlayer;
    private ProgressDialog progressDialog;
    private boolean initialStage = true;
    byte[] image;
    ImageView imageView;
    VolleySingleton volleySingleton;
    ImageLoader imageLoader;
    ImageView background;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        volleySingleton = VolleySingleton.getinstance();
        imageLoader = volleySingleton.getimageloader();
        Intent in = getIntent();
        imageView = (ImageView)findViewById(R.id.logo);
        background = (ImageView)findViewById(R.id.background);

        TextView toolbartitle = (TextView)findViewById(R.id.toolbar_title);
        TextView toolbartitle2 = (TextView)findViewById(R.id.toolbar_title2);
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/ola.otf");
        toolbartitle.setTypeface(custom_font);

        if(in!=null){
            title=in.getStringExtra(Constants.INTENT_TITLE);
            trackUrl=in.getStringExtra(Constants.INTENT_TRACK_URL);
            imageUrl=in.getStringExtra(Constants.INTENT_IMAGE_URL);

            if(!imageUrl.matches(""))
            {
                ;
                imageLoader.get(imageUrl, new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                        imageView.setImageDrawable(null);
                        Bitmap b = response.getBitmap();
                        imageView.setImageBitmap(b);


                    }

                    @Override
                    public void onErrorResponse(com.android.volley.VolleyError error) {
                        imageView.setImageDrawable(PlayActivity.this.getResources().getDrawable(R.mipmap.info));

                    }
                });
            }
            else
                imageView.setImageDrawable(PlayActivity.this.getResources().getDrawable(R.mipmap.info));


            try {
                artistsJSON=new JSONArray(in.getStringExtra(Constants.INTENT_ARTISTS));
                artists = new String [artistsJSON.length()];
                for(int i=0;i<artistsJSON.length();i++)
                    artists[i]=artistsJSON.getString(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        play = (ImageView) findViewById(R.id.audioStreamBtn);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        progressDialog = new ProgressDialog(this);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!playPause) {
                    play.setImageDrawable(getDrawable(R.mipmap.pause));

                    if (initialStage) {
                        new Player().execute(trackUrl);
                    } else {
                        if (!mediaPlayer.isPlaying())
                            mediaPlayer.start();
                    }

                    playPause = true;

                } else {
                    play.setImageDrawable(getDrawable(R.mipmap.playbutton));

                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                    }

                    playPause = false;
                }
            }
        });

    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        initialStage=true;

    }

    class Player extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... strings) {
            Boolean prepared = false;

            try {
                mediaPlayer.setDataSource(strings[0]);
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        initialStage = true;
                        playPause = false;
                        play.setImageDrawable(getDrawable(R.mipmap.playbutton));
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                    }
                });

                mediaPlayer.prepare();
                prepared = true;

            } catch (Exception e) {
                Log.e("OLA app", e.toString());
                prepared = false;
            }

            return prepared;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if (progressDialog.isShowing()) {
                progressDialog.cancel();
            }

            mediaPlayer.start();
            initialStage = false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setMessage("Buffering...");
            progressDialog.show();
        }
    }

}
