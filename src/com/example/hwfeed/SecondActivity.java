package com.example.hwfeed;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.webkit.WebView;
import android.widget.Toast;

public class SecondActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_second);

		WebView webView = (WebView) findViewById(R.id.webView1);

		Bundle bundle = getIntent().getExtras();
		String linkUrl = bundle.getString("linkUrl");
		try{
		webView.loadUrl(linkUrl);
		}catch(Exception e){
			Toast.makeText(SecondActivity.this, "èÓïÒÇÃéÊìæÇ…é∏îsÇµÇ‹ÇµÇΩ", Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.second, menu);
		return true;
	}

}
