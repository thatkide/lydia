package ca.efriesen.lydia.includes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.gesture.*;
import android.view.DragEvent;
import android.view.View;
import android.widget.Toast;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia_common.includes.Intents;

import java.util.ArrayList;

/**
 * User: eric
 * Date: 2013-03-17
 * Time: 8:25 PM
 */
//public class GestureListener implements GestureOverlayView.OnGesturePerformedListener {
//
//	private static final String TAG = "Gesture";
//
//	private Activity activity;
//	private Context context;
//	private GestureLibrary gestureLibrary;
//
//	public GestureListener(Activity activity, Context context) {
//		this.context = context;
//		this.activity = activity;
//		gestureLibrary = GestureLibraries.fromRawResource(context, R.raw.gestures);
//		if (!gestureLibrary.load()) {
//			Toast.makeText(context, "Gesture lib failed", Toast.LENGTH_SHORT).show();
//		}
//	}

//	public void onGesturePerformed(GestureOverlayView overlayView, Gesture gesture) {
//		ArrayList<Prediction> predictions = gestureLibrary.recognize(gesture);
//
//		if (predictions.size() > 0) {
//			String name = predictions.get(0).name;
//			if (predictions.get(0).score > 1.0) {
//				if (name.equalsIgnoreCase("right")) {
//					// right swipe is next song
//					activity.sendBroadcast(new Intent(Intents.NEXT));
//				} else if (name.equalsIgnoreCase("left")) {
//					// left swipe is previous
//					activity.sendBroadcast(new Intent(Intents.PREVIOUS));
//				} else if (name.equalsIgnoreCase("play")) {
//					// send the play/pause intent.  we will also listen for this broadcast to change our buttons
//					activity.sendBroadcast(new Intent(Intents.PLAYPAUSE));
//				} else if (name.equalsIgnoreCase("up")) {
//					Toast.makeText(context, "up", Toast.LENGTH_SHORT).show();
//				} else if (name.equalsIgnoreCase("down")) {
//					Toast.makeText(context, "down", Toast.LENGTH_SHORT).show();
//				}
//			}
//		}
//	}
//}
