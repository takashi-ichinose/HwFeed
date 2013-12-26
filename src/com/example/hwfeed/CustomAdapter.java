package com.example.hwfeed;

import java.util.List;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

public class CustomAdapter extends ArrayAdapter<ArticleDTO> {
	private int resource;

	public CustomAdapter(Context context, int resource, List<ArticleDTO> objects) {
		super(context, resource, objects);
		this.resource = resource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = View.inflate(getContext(), this.resource, null);

		TextView titleView = (TextView) convertView
				.findViewById(R.id.titleView);
		TextView contentView = (TextView) convertView
				.findViewById(R.id.contentView);
		TextView dateView = (TextView) convertView.findViewById(R.id.dateView);
		TextView publisherView = (TextView) convertView
				.findViewById(R.id.publisherView);
		ImageView thumbnailsImageView = (ImageView) convertView
				.findViewById(R.id.thumbnailsImageView);

		ArticleDTO item = getItem(position);

		titleView.setText(Html.fromHtml(item.getTitle()));
		contentView.setText(Html.fromHtml(item.getContent()));
		dateView.setText(item.getDate());
		publisherView.setText(item.getPublisher());
		thumbnailsImageView.setImageBitmap(item.getImage());
		// imageâÊëúÇ™Ç»Ç¢éûÇÕImageViewÇÃóÃàÊÇÇ»Ç≠Ç∑ÅB
		if (item.getImage() == null) {
			thumbnailsImageView.getLayoutParams().width = 0;
			thumbnailsImageView.getLayoutParams().height = 0;
		}
		thumbnailsImageView.setScaleType(ScaleType.FIT_START);

		return convertView;
	}

}
