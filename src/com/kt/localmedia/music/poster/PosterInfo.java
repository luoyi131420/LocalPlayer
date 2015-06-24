package com.kt.localmedia.music.poster;

public class PosterInfo {

	private int posterId;
	private String posterTitle;
	private String posterPath; 

	public PosterInfo() {
		super();
	}

	public PosterInfo(int id,String title, String path) {
		super();
		this.posterId = id;
		this.posterTitle = title;
		this.posterPath = path;
	}
	
	public int getPosterId() {
		return posterId;
	}

	public void setPosterId(int id) {
		this.posterId = id;
	}
	
	public String getPosterTitle() {
		return posterTitle;
	}

	public void setPosterTitle(String apptitle) {
		this.posterTitle = apptitle;
	}

	public String getPosterPath() {
		return posterPath;
	}

	public void setPosterPath(String path) {
		this.posterPath = path;
	}
}