package com.autosenseapp.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.autosenseapp.AutosenseApplication;
import java.util.ArrayList;
import javax.inject.Inject;
import butterknife.ButterKnife;
import butterknife.InjectView;
import ca.efriesen.lydia_common.media.Media;

/**
 * Created by eric on 2014-10-11.
 */
public class MediaAdapter extends ArrayAdapter<Media> {

	private final ArrayList<Media> medias;
	private int layoutId;
	private Media currentMedia;
	@Inject LayoutInflater layoutInflater;

	public MediaAdapter(Context context, int layoutId, ArrayList<Media> medias) {
		super(context, layoutId, medias);
		((AutosenseApplication)context.getApplicationContext()).inject(this);
		this.medias = medias;
		this.layoutId = layoutId;
	}

	public void setCurrentMedia(Media media) {
		if (media != null) {
			currentMedia = media;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;

		if (convertView != null) {
			viewHolder = (ViewHolder) convertView.getTag();
		} else {
			convertView = layoutInflater.inflate(layoutId, parent, false);

			viewHolder = new ViewHolder(convertView);
			convertView.setTag(viewHolder);
		}

		Media media = medias.get(position);
		viewHolder.album.setText(media.getName());
		if (currentMedia != null && media.getId() == currentMedia.getId()) {
			viewHolder.album.setTypeface(null, Typeface.BOLD_ITALIC);
		} else {
			viewHolder.album.setTypeface(null, Typeface.NORMAL);
		}
		return convertView;
	}

	static class ViewHolder {
		@InjectView(android.R.id.text1) TextView album;

		public ViewHolder(View view) {
			ButterKnife.inject(this, view);
		}
	}
}
