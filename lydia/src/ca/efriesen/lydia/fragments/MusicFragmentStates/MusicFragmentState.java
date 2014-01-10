package ca.efriesen.lydia.fragments.MusicFragmentStates;

import android.view.View;
import android.widget.ListView;
import ca.efriesen.lydia_common.media.Media;

import java.util.ArrayList;

/**
 * Created by eric on 1/5/2014.
 */
public interface MusicFragmentState {

	public boolean onBackPressed();
	public void onDestroy();
	public void onListItemClick(ListView list, View v, int position, long id);
	public void setView(Boolean fromSearch, Media... medias);
	public void search(String text);
}
