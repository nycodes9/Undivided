package com.socor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Key;
import com.socor.LandingActivity.SearchListener;
import com.socor.dao.EventDAO;

public class EventSuggestionFragment extends ListFragment implements SearchListener {

	static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	static final JsonFactory JSON_FACTORY = new JacksonFactory();
	public static final String TAG = EventSuggestionFragment.class.getSimpleName();
	
	private OnFragmentInteractionListener mListener;
	List<MeetupObject> meetupList = new ArrayList<EventSuggestionFragment.MeetupObject>();
	DataAdapter eventsDataAdapter;

	public static EventSuggestionFragment newInstance(String param1, String param2) {
		EventSuggestionFragment fragment = new EventSuggestionFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	public EventSuggestionFragment() {
		eventsDataAdapter = new DataAdapter();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
		}
		setListAdapter(eventsDataAdapter);
		new NetworkTask().execute("Contemporary Art");
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
		}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		setEmptyText("Loading .... ");
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		if (null != mListener) {
			// Notify the active callbacks interface (the activity, if the
			// fragment is attached to one) that an item has been selected.
			mListener.onFragmentInteraction(EventDAO.ITEMS.get(position).id);
		}
	}

	/**
	 * This interface must be implemented by activities that contain this fragment to allow an interaction in this
	 * fragment to be communicated to the activity and potentially other fragments contained in that activity.
	 * <p>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html" >Communicating with Other
	 * Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		// TODO: Update argument type and name
		public void onFragmentInteraction(String id);
	}

	
	class DataAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return meetupList.size();
		}

		@Override
		public MeetupObject getItem(int position) {
			return meetupList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return Long.parseLong(meetupList.get(position).id);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			if (convertView == null) {
				convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_event_item, null);
			}
			
			TextView eventTitle = (TextView) convertView.findViewById(R.id.eventTitleTV);
			TextView eventDesc = (TextView) convertView.findViewById(R.id.eventDescTV);
			
			eventTitle.setText(meetupList.get(position).name);
			eventDesc.setText(meetupList.get(position).description);
			
			return convertView;
		}
		
	}
	
	public static class MeetupListResult{
		@Key
		public List<MeetupObject> results;
	}
	
	public static class MeetupObject{
		@Key
		public String id;
		@Key
		public String updated;
		@Key
		public String description;
		@Key
		public String name;
		@Key
		public String urlkey;
		@Key
		public String link;
	}
	
	public class NetworkTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			meetupList.clear();
			eventsDataAdapter.notifyDataSetChanged();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			Log.d(TAG, "In doInBackground search param : " + params[0]);
			GenericUrl meetupUrl = new GenericUrl("https://api.meetup.com/topics.json/");
			meetupUrl.put("search", params[0]);
			meetupUrl.put("key", "6050319762cd5529863304c2f16e");
			meetupUrl.put("page", 10);

			HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
				@Override
				public void initialize(HttpRequest request) {
					request.setParser(new JsonObjectParser(JSON_FACTORY));
				}
			});
			MeetupListResult meetupResults;
			try {
				HttpRequest request = requestFactory.buildGetRequest(meetupUrl);
				meetupResults = request.execute().parseAs(MeetupListResult.class);
				meetupList = meetupResults.results;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			Log.d(TAG, "In onPostExecute search success : " + result.booleanValue());
			eventsDataAdapter.notifyDataSetChanged();
		}
		
	}

	@Override
	public void onSearchChanged(String searchStr) {
		new NetworkTask().execute(searchStr);
	}
}
