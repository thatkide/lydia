package ca.efriesen.lydia.activities;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import ca.efriesen.lydia.R;

/**
 * User: eric
 * Date: 2013-04-10
 * Time: 11:20 PM
 */
public class WebActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstance) {
		super.onCreate(savedInstance);
		setContentView(getLayoutInflater().inflate(R.layout.web_activity, null));

		WebView web = (WebView) findViewById(R.id.web);
		web.setWebViewClient(new WebViewClient());

		WebSettings webSettings = web.getSettings();
		webSettings.setJavaScriptEnabled(true);

		web.loadUrl("http://www.google.ca");
	}
}
