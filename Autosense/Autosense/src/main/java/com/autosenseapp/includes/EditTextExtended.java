package com.autosenseapp.includes;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import com.autosenseapp.activities.Dashboard;

/**
 * Created by eric on 2014-07-24.
 */
public class EditTextExtended extends EditText {
	private EditTextImeBackListener mOnImeBack;
	private Context context;

	public EditTextExtended(Context context) {
		super(context);
		this.context = context;
	}

	public EditTextExtended(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public EditTextExtended(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
	}

	@Override
	public boolean onKeyPreIme(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
			if (mOnImeBack != null) mOnImeBack.onImeBack(this, this.getText().toString());
			// reenter immersive mode on back pressed
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && context instanceof Dashboard) {
				((Dashboard)context).getWindow().getDecorView().setSystemUiVisibility(
						View.SYSTEM_UI_FLAG_LAYOUT_STABLE
								| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
								| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
								| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
								| View.SYSTEM_UI_FLAG_FULLSCREEN
								| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
				);
			}

		}
		return super.dispatchKeyEvent(event);
	}

	public void setOnEditTextImeBackListener(EditTextImeBackListener listener) {
		mOnImeBack = listener;
	}

}

interface EditTextImeBackListener {
	public abstract void onImeBack(EditTextExtended ctrl, String text);
}

