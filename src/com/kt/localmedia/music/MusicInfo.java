package com.kt.localmedia.music;

public class MusicInfo {
	private static final String TAG = "KTMusicInfo";
	private int musicId;
	private String musicTitle;
	private String musicArtist;	
	private long musicDuration;
	private long musicSize;
	private String musicUrl; 
	private int posterId;
	private String posterPath;
	private String posterTitle;
	
	public MusicInfo() {
		super();
	}
	
	public int getMusicId() {
		return musicId;
	}

	public void setMusicId(int id) {
		this.musicId = id;
	}
	
	public String getMusicTitle() {
		return musicTitle;
	}

	public void setMusicTitle(String title) {
		this.musicTitle = title;
	}

	public String getMusicArtist() {
		return musicArtist;
	}

	public void setMusicArtist(String artist) {
		this.musicArtist = artist;
	}
	
	public long getMusicDuration() {
		return musicDuration;
	}

	public void setMusicDuration(long duration) {
		this.musicDuration = duration;
	}
	
	public long getMusicSize() {
		return musicSize;
	}

	public void setMusicSize(long size) {
		this.musicSize = size;
	}
	
	public String getMusicUrl() {
		return musicUrl;
	}

	public void setMusicUrl(String url) {
		this.musicUrl = url;
	}
	
}