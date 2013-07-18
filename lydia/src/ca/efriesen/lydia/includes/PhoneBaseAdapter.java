package ca.efriesen.lydia.includes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ca.efriesen.lydia.R;
import ca.efriesen.lydia.databases.Message;
import java.util.ArrayList;

/**
 * Created by eric on 2013-06-03.
 */
public class PhoneBaseAdapter extends MessageBaseAdapter {
	private static ArrayList<Message> messageArrayList;
	private LayoutInflater mInflater;
	private Context context;

	public PhoneBaseAdapter(Context context, ArrayList<Message> results) {
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
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.recent_call_list_view, null);
			holder = new ViewHolder();
			holder.phoneNumber = (TextView) convertView.findViewById(R.id.phone_number);
			holder.time = (TextView) convertView.findViewById(R.id.time_received);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.phoneNumber.setText(Helpers.getContactDisplayNameByNumber(context, messageArrayList.get(position).getPhoneNumber()));
		holder.time.setText(getDateFormat(messageArrayList.get(position).getTimeReceived()));

		return convertView;
	}
}
