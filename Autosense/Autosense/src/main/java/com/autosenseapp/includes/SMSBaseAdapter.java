package com.autosenseapp.includes;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.autosenseapp.R;
import com.autosenseapp.databases.Message;
import java.util.ArrayList;

/**
 * Created by eric on 2013-06-03.
 */
public class SMSBaseAdapter extends MessageBaseAdapter {
	private static ArrayList<Message> messageArrayList;
	private LayoutInflater mInflater;
	private Context context;

	public SMSBaseAdapter(Context context, ArrayList<Message> results) {
		super();
		messageArrayList = results;
		mInflater = LayoutInflater.from(context);
		this.context = context;
	}

	public int getCount() {
		return messageArrayList.size();
	}

	public Object getItem(int position) {
		return messageArrayList.get(position);
	}

	public long getItemId(int position) {
		return messageArrayList.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.sms_list_view, null);
			holder = new ViewHolder();
			holder.message = (TextView) convertView.findViewById(R.id.message);
			holder.phoneNumber = (TextView) convertView.findViewById(R.id.phone_number);
			holder.time = (TextView) convertView.findViewById(R.id.time_received);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Boolean fromMe = messageArrayList.get(position).getFromMe();

		if (fromMe) {
			RelativeLayout.LayoutParams alignLeft = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			alignLeft.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);

			RelativeLayout.LayoutParams alignRight = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			alignRight.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);

			holder.phoneNumber.setLayoutParams(alignRight);
			holder.time.setLayoutParams(alignLeft);
			holder.message.setGravity(Gravity.RIGHT);
		}

		holder.message.setText(messageArrayList.get(position).getMessage());
		holder.phoneNumber.setText(Helpers.getContactDisplayNameByNumber(context, messageArrayList.get(position).getPhoneNumber()));
		holder.time.setText(getDateFormat(messageArrayList.get(position).getTimeReceived()));

		return convertView;
	}
}
