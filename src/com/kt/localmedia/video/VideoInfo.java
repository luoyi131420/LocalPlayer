package com.kt.localmedia.video;



public class VideoInfo {
	private static final String TAG = "KTVideoInfo";
	private int videoId;
	private String videoTitle;
	private String videoUrl; 
	
	public VideoInfo() {
		super();
	}
	
	public int getVideoId() {
		return videoId;
	}

	public void setVideoId(int id) {
		this.videoId = id;
	}
	
	public String getVideoTitle() {
		return videoTitle;
	}
	
	public void setVideoTitle(String title) {
		this.videoTitle = title;
	}
	
	public String getVideoUrl() {
		return videoUrl;
	}

	public void setVideoUrl(String url) {
		this.videoUrl = url;
	}	
}