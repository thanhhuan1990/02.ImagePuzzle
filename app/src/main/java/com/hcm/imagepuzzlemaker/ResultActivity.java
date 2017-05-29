package com.hcm.imagepuzzlemaker;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.hcm.imagepuzzlemaker.db.DBImagePuzzle;
import com.hcm.imagepuzzlemaker.db.Result;
import com.hcm.imagepuzzlemaker.util.AdmobUtil;
import com.hcm.imagepuzzlemaker.util.Util;

public class ResultActivity extends Activity {

	private DBImagePuzzle	db;
	private ListView		lvResult;
	private ImageButton		btnRefresh;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_result);
		
		AdmobUtil.initAdView(this);
		
		TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
		String strResultListTitle = getResources().getString(R.string.Result_List_Title);
		Util.changeTextViewColour(getApplicationContext(), txtTitle, strResultListTitle);
		
		db = new DBImagePuzzle(getApplicationContext());
		List<Result> arResult = db.query();
		
		Collections.sort(arResult, 
						new Comparator<Result>() {
							@Override
							public int compare(Result lhs, Result rhs) {
								int result = rhs.getType() - lhs.getType();
								if(result == 0) {
									return lhs.getTime() - rhs.getTime();
								} else {
									return result;
								}
							};
							
						});
		
		ResultAdapter adapter = new ResultAdapter(getApplicationContext(), arResult);
		
		lvResult	= (ListView) findViewById(R.id.lvResult);
		lvResult.setAdapter(adapter);
		
		btnRefresh = (ImageButton) findViewById(R.id.btnRefresh);
		btnRefresh.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	/*
	 * Adapter of list
	 */
	public class ResultAdapter extends BaseAdapter {
		
		private final LayoutInflater mInflater;
		private List<Result> array;
		
		public ResultAdapter(Context context, List<Result> array) {
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.array = array;
		}

		public int getCount() {
			return array.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			
			final int pos = position;
			
			ViewHolder holder;
			
			View rowView = convertView;
			// Inflate converview
			if (convertView == null) {
				rowView = mInflater.inflate(R.layout.listitem_result, parent, false);
				holder = new ViewHolder();
				
				holder.txtIndex = (TextView)rowView.findViewById(R.id.txtIndex);
				holder.txtIndex.setText((pos+1) + "");
				
				holder.txtType = (TextView)rowView.findViewById(R.id.txtType);
				holder.txtType.setText(array.get(pos).getType() + "x" + array.get(pos).getType());
				
				holder.txtTime = (TextView)rowView.findViewById(R.id.txtTime);
				int seconds = array.get(pos).getTime() % 60;
				int minutes = (array.get(pos).getTime() / 60) % 60;
				holder.txtTime.setText(String.format("%02d : %02d", minutes, seconds));
				
				rowView.setTag(holder);
			} else {
				holder = (ViewHolder) rowView.getTag();
			}
			
			return rowView;
		}
	}
	
	private class ViewHolder {
		public TextView txtIndex;
		public TextView txtType;
		public TextView txtTime;
	}
}
