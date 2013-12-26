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
				dialog = ProgressDialog.show(MainActivity.this, "�C���^�[�l�b�g�ڑ�",
						"�����擾��");
			}

			@Override
			protected String doInBackground(Void... params) {
				// ������ʂ��������[�handroid�Ő���
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
				// ���A�N�e�B�r�e�B�ւ̓����NURL�݂̂�n���AwebView�ŕ\��������B
				intent.putExtra("linkUrl", list.get(position).getLinkUrl());
				startActivity(intent);
			}
		});
	}

	// �C���^�[�l�b�g����JSON�f�[�^���擾���郁�\�b�h
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

	// JSON�f�[�^�̓ǂݍ���
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

				// �T���l�C���摜���擾
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
				// �T���l�C���摜����������Z�b�g����
				if (bitmap != null) {
					dto.setImage(bitmap);
				}
				list.add(dto);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	// ActionBar��searchView������
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// action_bar_menu.xml�Œ�`�������j���[���ڂ̓K�p
		getMenuInflater().inflate(R.menu.action_bar_menu, menu);
		// �����r���[�C���X�^���X�̎擾
		SearchView searchView = (SearchView) menu.findItem(R.id.menu_search)
				.getActionView();
		// ���X�i�[���Z�b�g���Č����r���[�̓��͂�҂��󂯂�
		searchView.setOnQueryTextListener(new OnQueryTextListener() {
			// �N�G�������񂪃T�u�~�b�g�i���͓��e���M�j���ꂽ�^�C�~���O�ŌĂ΂��
			@Override
			public boolean onQueryTextSubmit(String query) {
				searchText = query;
				AsyncTask<Void, Integer, String> task = new AsyncTask<Void, Integer, String>() {
					@Override
					protected void onPreExecute() {
						super.onPreExecute();
						dialog = ProgressDialog.show(MainActivity.this,
								"�C���^�[�l�b�g�ڑ�", "���擾��");
						// ���݂̃��X�g���N���A����B
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

			// �N�G�������񂪕ύX���ꂽ�^�C�~���O�ŌĂ΂�鏈��
			@Override
			public boolean onQueryTextChange(String newText) {
				return false;
			}
		});
		return super.onCreateOptionsMenu(menu);
	}

}
