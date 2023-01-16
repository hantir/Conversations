package com.frizid.timeline.ui.adapter;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.wefika.flowlayout.FlowLayout;

import java.util.List;

import com.frizid.timeline.Config;
import com.frizid.timeline.R;
import com.frizid.timeline.databinding.ContactBinding;
import com.frizid.timeline.entities.ListItem;
import com.frizid.timeline.ui.SettingsActivity;
import com.frizid.timeline.ui.XmppActivity;
import com.frizid.timeline.ui.util.AvatarWorkerTask;
import com.frizid.timeline.ui.util.StyledAttributes;
import com.frizid.timeline.utils.EmojiWrapper;
import com.frizid.timeline.utils.IrregularUnicodeDetector;
import com.frizid.timeline.xmpp.Jid;

public class ListItemAdapter extends ArrayAdapter<ListItem> {

	protected XmppActivity activity;
	private boolean showDynamicTags = false;
	private OnTagClickedListener mOnTagClickedListener = null;
	private final View.OnClickListener onTagTvClick = view -> {
		if (view instanceof TextView && mOnTagClickedListener != null) {
			TextView tv = (TextView) view;
			final String tag = tv.getText().toString();
			mOnTagClickedListener.onTagClicked(tag);
		}
	};

	public ListItemAdapter(XmppActivity activity, List<ListItem> objects) {
		super(activity, 0, objects);
		this.activity = activity;
	}


	public void refreshSettings() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
		this.showDynamicTags = preferences.getBoolean(SettingsActivity.SHOW_DYNAMIC_TAGS, false);
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		LayoutInflater inflater = activity.getLayoutInflater();
		ListItem item = getItem(position);
		ViewHolder viewHolder;
		if (view == null) {
			ContactBinding binding = DataBindingUtil.inflate(inflater,R.layout.contact,parent,false);
			viewHolder = ViewHolder.get(binding);
			view = binding.getRoot();
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		view.setBackground(StyledAttributes.getDrawable(view.getContext(),R.attr.list_item_background));
		Log.d("jid_valeu", item.getJid().toString());

		List<ListItem.Tag> tags = item.getTags(activity);
		if (tags.size() == 0 || !this.showDynamicTags) {
			viewHolder.tags.setVisibility(View.GONE);
		} else {
			viewHolder.tags.setVisibility(View.VISIBLE);
			viewHolder.tags.removeAllViewsInLayout();
			for (ListItem.Tag tag : tags) {
				TextView tv = (TextView) inflater.inflate(R.layout.list_item_tag, viewHolder.tags, false);
				tv.setText(tag.getName());
				tv.setBackgroundColor(tag.getColor());
				tv.setOnClickListener(this.onTagTvClick);
				viewHolder.tags.addView(tv);
			}
		}
		final Jid jid = item.getJid();
		/*if (jid != null) {
			viewHolder.jid.setVisibility(View.VISIBLE);
			viewHolder.jid.setText(IrregularUnicodeDetector.style(activity, jid));
		} else {
			viewHolder.jid.setVisibility(View.GONE);
		}*/
		viewHolder.name.setText(EmojiWrapper.transform(item.getDisplayName()));
		AvatarWorkerTask.loadAvatar(item, viewHolder.avatar, R.dimen.avatar);

		if (jid.getDomain().toString().contains("conference.hantir.com")) {
			viewHolder.jid.setVisibility(View.GONE);
		}
		else
			{
				viewHolder.jid.setVisibility(View.VISIBLE);
				if (tags.size() == 0 ) {
					viewHolder.jid.setTextColor(ContextCompat.getColor(getContext(), R.color.grey500));
					viewHolder.jid.setText("Offline");
				} else
				{
					viewHolder.jid.setTextColor(ContextCompat.getColor(getContext(), R.color.green800));
					viewHolder.jid.setTypeface(null, Typeface.BOLD);
					viewHolder.jid.setText(tags.get(0).getName());
				}
		}
		return view;
	}

	public void setOnTagClickedListener(OnTagClickedListener listener) {
		this.mOnTagClickedListener = listener;
	}


	public interface OnTagClickedListener {
		void onTagClicked(String tag);
	}

	private static class ViewHolder {
		private TextView name;
		private TextView jid;
		private ImageView avatar;
		private FlowLayout tags;

		private ViewHolder() {

		}

		public static ViewHolder get(ContactBinding binding) {
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.name = binding.contactDisplayName;
			viewHolder.jid = binding.contactJid;
			viewHolder.avatar = binding.contactPhoto;
			viewHolder.tags = binding.tags;
			binding.getRoot().setTag(viewHolder);
			return viewHolder;
		}
	}

}
