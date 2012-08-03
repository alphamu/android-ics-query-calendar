package com.alimuzaffar.meetingroomdisplay.activity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Instances;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alimuzaffar.meetingroomdisplay.BuildConfig;
import com.alimuzaffar.meetingroomdisplay.R;

public class CheckCalendarActivity extends Activity {

	private static final String DEBUG_TAG = "CheckCalendarActiviy";
	public static final String[] INSTANCE_PROJECTION = new String[] {
			Instances.EVENT_ID, // 0
			Instances.BEGIN, // 1
			Instances.END, //2
			Instances.TITLE, // 3
			Instances.DESCRIPTION, //4
			Instances.ORGANIZER //5
	};

	// The indices for the projection array above.
	private static final int PROJECTION_ID_INDEX = 0;
	private static final int PROJECTION_BEGIN_INDEX = 1;
	private static final int PROJECTION_END_INDEX = 2;
	private static final int PROJECTION_TITLE_INDEX = 3;
	private static final int PROJECTION_DESCRIPTION_INDEX = 4;
	private static final int PROJECTION_ORGANIZER_INDEX = 5;
	
	DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy hh:mm a");

	// Specify the date range you want to search for recurring
	// event instances
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_check_calendar);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Calendar beginTime = Calendar.getInstance();
		long startMillis = beginTime.getTimeInMillis(); //current time in millis
		
		Calendar endTime = Calendar.getInstance();
		endTime.add(Calendar.HOUR, 12);
		long endMillis = endTime.getTimeInMillis(); //now + 12 hours. Fixing the end time to something
													//like 7 o'clock is a bad idea because the app will
													//blow when current time is after end time!

		Cursor cur = null;
		ContentResolver cr = getContentResolver();

		// The ID of the recurring event whose instances you are searching
		// for in the Instances table. Or getch everything by asking for > 0,
		// you can probably make these null as well.
		String selection = Instances.EVENT_ID + " > ?";
		String[] selectionArgs = new String[] {"0"};

		// Construct the query with the desired date range.
		Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
		ContentUris.appendId(builder, startMillis);
		ContentUris.appendId(builder, endMillis);

		// Submit the query
		cur = cr.query(builder.build(),
		        INSTANCE_PROJECTION, selection,
		        selectionArgs, "startDay ASC, startMinute ASC");
		
		
		TextView messageView = (TextView)findViewById(R.id.message);
		TextView beginView = (TextView)findViewById(R.id.begin);
		TextView endView = (TextView)findViewById(R.id.end);
		TextView organizerView = (TextView)findViewById(R.id.organizer);
		TextView titleView = (TextView)findViewById(R.id.title);
		TextView descriptionView = (TextView)findViewById(R.id.description);
		
		boolean firstFound = false;
		while (cur.moveToNext()) {
			// Get the field values
			long eventId = cur.getLong(PROJECTION_ID_INDEX);
			long beginVal = cur.getLong(PROJECTION_BEGIN_INDEX);
			long endVal = cur.getLong(PROJECTION_END_INDEX);
			String title = cur.getString(PROJECTION_TITLE_INDEX);
			String description = cur.getString(PROJECTION_DESCRIPTION_INDEX);
			String organizer = cur.getString(PROJECTION_ORGANIZER_INDEX);
		
			StringBuilder event = new StringBuilder();
			event.append(eventId).append(",");
			event.append(title).append("\n");
			event.append("Organizer: ").append(organizer).append(",\n");
			event.append("Start: ").append(formatter.format(new Date(beginVal))).append("\n");
			event.append("End:").append(formatter.format(new Date(endVal))).append("\n");
			event.append(description).append("\n");
			
			if(BuildConfig.DEBUG)
				Log.d(DEBUG_TAG, event.toString());
			
			// Do something with the values.			
			long timeLeft = (beginVal) - System.currentTimeMillis();
			
			if(!firstFound && timeLeft > 0) {
				RelativeLayout ll = (RelativeLayout)findViewById(R.id.background);
				String message = "ROOM FREE UNTIL";
				if(timeLeft < 1000 * 60 * 2) {
					ll.setBackgroundResource(android.R.color.holo_red_light);
					message = "EVENT STARTING NOW";
				} else if (timeLeft < 1000 * 60 * 10) {
					ll.setBackgroundResource(android.R.color.holo_orange_light);
					message = "EVENT STARTING SOON";
				}
				
				messageView.setText(message);
				beginView.setText(formatter.format(new Date(beginVal)));
				endView.setText(formatter.format(new Date(endVal)));
				organizerView.setText(organizer);
				titleView.setText(title);
				descriptionView.setText(description);
				
				firstFound = true;
			}
		}
		if(!firstFound) {
			messageView.setText("NO EVENTS FOUND, Presumable that means the room is free!");
		}
	}
	
	
}
