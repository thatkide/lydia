package ca.efriesen.lydia.controllers.ButtonControllers;

/**
 * Created by eric on 2014-06-14.
 */
public abstract class MyButton {

	public void onClick() {	};
	public boolean onLongClick() {
		return false;
	};

	public void cleanUp() { };
	public abstract String getAction();
	public abstract String getDescription();
	public abstract String toString();
}
