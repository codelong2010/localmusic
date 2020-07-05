package com.example.musicplay;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.animee.musicplay.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    ImageView nextIv,playIv,lastIv,albumIv;
    TextView singerTv,songTv;
    RecyclerView musicRv;
    List<LocalMusicBean>mDatas;
    private LocalMusicAdapter adapter;

    int currnetPlayPosition = -1;
    int currentPausePositionInSong = 0;
    MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mediaPlayer = new MediaPlayer();
        mDatas = new ArrayList<>();
        adapter = new LocalMusicAdapter(this, mDatas);
        musicRv.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        musicRv.setLayoutManager(layoutManager);
        loadLocalMusicData();
        setEventListener();
    }

    private void setEventListener() {
        adapter.setOnItemClickListener(new LocalMusicAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                currnetPlayPosition = position;
                LocalMusicBean musicBean = mDatas.get(position);
                playMusicInMusicBean(musicBean);
            }
        });
    }

    public void playMusicInMusicBean(LocalMusicBean musicBean) {
        singerTv.setText(musicBean.getSinger());
        songTv.setText(musicBean.getSong());
        stopMusic();
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(musicBean.getPath());
            String albumArt = musicBean.getAlbumArt();
            Log.i("lsh123", "playMusicInMusicBean: albumpath=="+albumArt);
            Bitmap bm = BitmapFactory.decodeFile(albumArt);
            Log.i("lsh123", "playMusicInMusicBean: bm=="+bm);
            albumIv.setImageBitmap(bm);
            playMusic();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void playMusic() {
        if (mediaPlayer!=null&&!mediaPlayer.isPlaying()) {
            if (currentPausePositionInSong == 0) {
                try {
                    mediaPlayer.prepare();
                    mediaPlayer.start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                mediaPlayer.seekTo(currentPausePositionInSong);
                mediaPlayer.start();
            }
            playIv.setImageResource(R.mipmap.icon_pause);
        }
    }
    private void pauseMusic() {
        if (mediaPlayer!=null&&mediaPlayer.isPlaying()) {
            currentPausePositionInSong = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
            playIv.setImageResource(R.mipmap.icon_play);
        }
    }
    private void stopMusic() {
        if (mediaPlayer!=null) {
            currentPausePositionInSong = 0;
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
            mediaPlayer.stop();
            playIv.setImageResource(R.mipmap.icon_play);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMusic();
    }

    private void loadLocalMusicData() {
        ContentResolver resolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = resolver.query(uri, null, null, null, null);
        int id = 0;
        while (cursor.moveToNext()) {
            String song = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String singer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            id++;
            String sid = String.valueOf(id);
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
            String time = sdf.format(new Date(duration));
            String album_id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            String albumArt = getAlbumArt(album_id);
            LocalMusicBean bean = new LocalMusicBean(sid, song, singer, album, time, path,albumArt);
            mDatas.add(bean);
        }
        adapter.notifyDataSetChanged();
    }


    private String getAlbumArt(String album_id) {
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[]{"album_art"};
        Cursor cur = this.getContentResolver().query(
                Uri.parse(mUriAlbums + "/" + album_id),
                projection, null, null, null);
        String album_art = null;
        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToNext();
            album_art = cur.getString(0);
        }
        cur.close();
        cur = null;
        return album_art;
    }
    private void initView() {
        nextIv = findViewById(R.id.local_music_bottom_iv_next);
        playIv = findViewById(R.id.local_music_bottom_iv_play);
        lastIv = findViewById(R.id.local_music_bottom_iv_last);
        albumIv = findViewById(R.id.local_music_bottom_iv_icon);
        singerTv = findViewById(R.id.local_music_bottom_tv_singer);
        songTv = findViewById(R.id.local_music_bottom_tv_song);
        musicRv = findViewById(R.id.local_music_rv);
        nextIv.setOnClickListener(this);
        lastIv.setOnClickListener(this);
        playIv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.local_music_bottom_iv_last:
                if (currnetPlayPosition ==0) {
                    Toast.makeText(this,"已经是第一首了，没有上一曲！",Toast.LENGTH_SHORT).show();
                    return;
                }
                currnetPlayPosition = currnetPlayPosition-1;
                LocalMusicBean lastBean = mDatas.get(currnetPlayPosition);
                playMusicInMusicBean(lastBean);
                break;
            case R.id.local_music_bottom_iv_next:
                if (currnetPlayPosition ==mDatas.size()-1) {
                    Toast.makeText(this,"已经是最后一首了，没有下一曲！",Toast.LENGTH_SHORT).show();
                    return;
                }
                currnetPlayPosition = currnetPlayPosition+1;
                LocalMusicBean nextBean = mDatas.get(currnetPlayPosition);
                playMusicInMusicBean(nextBean);
                break;
            case R.id.local_music_bottom_iv_play:
                if (currnetPlayPosition == -1) {
                    Toast.makeText(this,"请选择想要播放的音乐",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mediaPlayer.isPlaying()) {
                    pauseMusic();
                }else {
                    playMusic();
                }
                break;
        }
    }
}
