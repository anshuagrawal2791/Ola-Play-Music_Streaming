package com.ola.hackerearth.ola;

import java.util.Arrays;

/**
 * Created by anshu on 19/12/17.
 */
public class Song {
    private String song, url, cover_image;
    private  String artists[];
    public Song(String song, String url, String artists, String cover_image) {
        this.song=song;
        this.cover_image = cover_image;
        this.url=url;
        this.artists = artists.split(",");
        for(int i=0;i<this.artists.length;i++){
            this.artists[i]= this.artists[i].trim();
        }

    }

    public Song() {
    }

    public String getSong() {
        return song;
    }

    public String getUrl() {
        return url;
    }

    public String getCover_image() {
        return cover_image;
    }

    public String[] getArtists() {
        return artists;
    }

    public void setSong(String song) {
        this.song = song;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setCover_image(String cover_image) {
        this.cover_image = cover_image;
    }

    public void setArtists(String artists) {
        this.artists = artists.split(",");
        for(int i=0;i<this.artists.length;i++){
            this.artists[i]= this.artists[i].trim();
        }
    }

    @Override
    public String toString() {
        return "Song{" +
                "song='" + song + '\'' +
                ", url='" + url + '\'' +
                ", cover_image='" + cover_image + '\'' +
                ", artists=" + Arrays.toString(artists) +
                '}';
    }
}
