package com.autosenseapp.fragments.MusicFragmentStates;

import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import ca.efriesen.lydia_common.media.Media;

/**
 * Created by eric on 1/5/2014.
 */
public interface MusicFragmentState {

	public boolean onBackPressed();
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo);
	public boolean onContextItemSelected(MenuItem item);
	public void onDestroy();
	public void onListItemClick(ListView list, View v, int position, long id);
	public void setView(Boolean fromSearch, Media... medias);
	public void search(String text);
}
