package com.example.hwfeed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

public class MainActivity extends Activity {
	private List<ArticleDTO> list = new ArrayList<ArticleDTO>();
	private ListView listView;
	private ProgressDialog dialog;
	private String searchText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		listView = (ListView) findViewById(R.id.listView);

		AsyncTask<Void, Integer, String> task = new AsyncTask<Void, Integer, String>() {

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				dialog = ProgressDialog.show(MainActivity.this, "インターネット接続",
						"情報を取得中");
			}

			@Override
			protected String doInBackground(Void... params) {
				// 初期画面を検索ワードandroidで生成
				String downloadData = downloadJson("android");
				readJson(downloadData);
				return downloadData;
			}

			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				Log.i("JSON", result);
				dialog.dismiss();
				CustomAdapter adapter = new CustomAdapter(MainActivity.this,
						R.layout.item_list, list);
				listView.setAdapter(adapter);
			}
		};
		task.execute();

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				Intent intent = new Intent(MainActivity.this,
						SecondActivity.class);
				// 次アクティビティへはリンクURLのみを渡し、webViewで表示させる。
				intent.putExtra("linkUrl", list.get(position).getLinkUrl());
				startActivity(intent);
			}
		});
	}

	// インターネットからJSONデータを取得するメソッド
	public String downloadJson(String queryText) {
		StringBuilder bld = new StringBuilder();
		InputStream is = null;
		try {
			URL url = new URL(
					"https://ajax.googleapis.com/ajax/services/search/news?v=1.0&rsz=6&hl=ja&q="
							+ queryText);
			URLConnection connection = (HttpURLConnection) url.openConnection();
			is = connection.getInputStream();
			BufferedReader buff = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = buff.readLine()) != null) {
				bld.append(line);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (Exception e) {
			}
		}
		return bld.toString();
	}

	// JSONデータの読み込み
	public void readJson(String downloadData) {
		InputStream in = null;
		try {
			JSONObject json = new JSONObject(downloadData);
			JSONObject responseData = json.getJSONObject("responseData");
			JSONArray results = responseData.getJSONArray("results");
			for (int i = 0; i < results.length(); i++) {
				JSONObject result = results.getJSONObject(i);
				String title = result.getString("titleNoFormatting");
				String content = result.getString("content");
				String date = result.getString("publishedDate");
				String publisher = result.getString("publisher");
				String linkUrl = result.getString("unescapedUrl");

				// サムネイル画像を取得
				Bitmap bitmap = null;
				try {
					JSONObject image = result.getJSONObject("image");
					String imageUrl = image.getString("url");
					URL url = new URL(imageUrl);
					in = (InputStream) url.getContent();
					bitmap = BitmapFactory.decodeStream(in);
					in.close();
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				ArticleDTO dto = new ArticleDTO();
				dto.setTitle(title);
				dto.setContent(content);
				dto.setDate(date);
				dto.setPublisher(publisher);
				dto.setLinkUrl(linkUrl);
				// サムネイル画像があったらセットする
				if (bitmap != null) {
					dto.setImage(bitmap);
				}
				list.add(dto);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	// ActionBarにsearchViewを実装
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// action_bar_menu.xmlで定義したメニュー項目の適用
		getMenuInflater().inflate(R.menu.action_bar_menu, menu);
		// 検索ビューインスタンスの取得
		SearchView searchView = (SearchView) menu.findItem(R.id.menu_search)
				.getActionView();
		// リスナーをセットして検索ビューの入力を待ち受ける
		searchView.setOnQueryTextListener(new OnQueryTextListener() {
			// クエリ文字列がサブミット（入力内容送信）されたタイミングで呼ばれる
			@Override
			public boolean onQueryTextSubmit(String query) {
				searchText = query;
				AsyncTask<Void, Integer, String> task = new AsyncTask<Void, Integer, String>() {
					@Override
					protected void onPreExecute() {
						super.onPreExecute();
						dialog = ProgressDialog.show(MainActivity.this,
								"インターネット接続", "情報取得中");
						// 現在のリストをクリアする。
						list.clear();

					}

					@Override
					protected String doInBackground(Void... params) {
						String downloadData = downloadJson(searchText);
						readJson(downloadData);
						return downloadData;
					}

					@Override
					protected void onPostExecute(String result) {
						super.onPostExecute(result);
						Log.i("JSON", result);
						dialog.dismiss();
						CustomAdapter adapter = new CustomAdapter(
								MainActivity.this, R.layout.item_list, list);
						listView.setAdapter(adapter);
					}

				};
				task.execute();
				return false;
			}

			// クエリ文字列が変更されたタイミングで呼ばれる処理
			@Override
			public boolean onQueryTextChange(String newText) {
				return false;
			}
		});
		return super.onCreateOptionsMenu(menu);
	}

}
