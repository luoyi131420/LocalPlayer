package com.kt.localmedia.music;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kt.localmedia.R;


public class MusicAdapter extends BaseAdapter {
	private static final String TAG = "KTMusicAdapter";
	ArrayList<String> musicinfo;
	Context context;

	public MusicAdapter(ArrayList<String> music_list, Context context) {
		super();
		this.musicinfo = music_list;
		this.context = context;
	}

	@Override
	public int getCount() {
		if (null != musicinfo) {
			return musicinfo.size();
		} else {
			return 0;
		}
	}

	@Override
	public Object getItem(int position) {
		return musicinfo.get(position);
	}
	
	@Override
	public long getItemId(int id) {
		return id;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.music_play_list, null);
			viewHolder = new ViewHolder();
			viewHolder.title = (TextView) convertView.findViewById(R.id.music_title);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		String path=musicinfo.get(position);
		int n = path.length();
		int m = path.lastIndexOf("/");
		String mMusicTitle=path.substring(m+1, n);
		viewHolder.title.setText(mMusicTitle);	
		return convertView;
	}
}

class ViewHolder {
	public TextView title,artist,duration;
	public ImageView image;
}