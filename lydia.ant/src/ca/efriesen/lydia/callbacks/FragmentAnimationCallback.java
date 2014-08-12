package ca.efriesen.lydia.callbacks;

/**
 * Created by eric on 2014-07-05.
 */
public interface FragmentAnimationCallback {
	public static final int SHOW = 0;
	public static final int HIDE = 1;
	public void animationComplete(int direction);
	public void animationStart(int direction);
}
