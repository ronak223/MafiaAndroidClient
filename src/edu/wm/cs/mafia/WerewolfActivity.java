package edu.wm.cs.mafia;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.json.parsers.JSONParser;
import com.json.parsers.JsonParserFactory;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibraryConstants;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class WerewolfActivity extends Activity {

	//=====================================================================================
	String userID;
	int num_players;
	
	//0 indicated that it is the first day cycle, 1 indicates it is not
		int first_day_cycle = 0;
	
	//0 is day, 1 is night
	int day_night_cycle = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_werewolf);
		
		//getting current userID and number of players
		Intent intent = getIntent();
		userID = intent.getStringExtra("userID");
		num_players = intent.getIntExtra("numPlayers", 0);
		
		//init Async client for web service access
    	final AsyncHttpClient client = new AsyncHttpClient();
		client.setBasicAuth("specialkeythatnoonewilleverknow", "specialerpasswordisawesome");
		
		//getting listView by resource ID, and context
		final ListView lv = (ListView)findViewById(R.id.werewolf_scent_list);
		final Context cur_context = getApplicationContext();
		
		//setting up JsonFactory
    	JsonParserFactory factory=JsonParserFactory.getInstance();
    	final JSONParser parser=factory.newJsonParser();
    	
    	//initializing list for player names
		final ArrayList<String> scent_range_list = new ArrayList<String>();
		//setting ListView to array of player userIDs that are currently queued up to play
		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(cur_context,android.R.layout.simple_list_item_1, scent_range_list);
		lv.setAdapter(arrayAdapter); 
		
		//timer for continuously checking those in scent range
		final Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask(){
			
			@Override
			public void run(){
				client.get("http://mafia-web-service.herokuapp.com/playersNearTo/" + userID + "/10" , new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response){
						scent_range_list.clear();
						
						Map jsonData=parser.parseJson(response);
						Map rootJson= (Map) jsonData.get("root");
						List al2 = (List) jsonData.get("response");
						
						for(int i = 0; i < al2.size(); i++){
							String cur_userID=(String) ((Map)al2.get(i)).get("userID");
							String isDead = (String) ((Map)al2.get(i)).get("isDead");
							if(isDead.equals("false")){
								scent_range_list.add(cur_userID);
							}
						}
						
						runOnUiThread(new Runnable() {
						     public void run() {
						    	 arrayAdapter.notifyDataSetChanged();
						    }
						});
					}
				});	
			}
		}, 0, 2000);
		
		final ListView lv2 = (ListView)findViewById(R.id.werewolf_kill_list);
		
		//initializing list for player names
		final ArrayList<String> kill_range_list = new ArrayList<String>();
		//setting ListView to array of player userIDs that are currently queued up to play
		final ArrayAdapter<String> killarrayAdapter = new ArrayAdapter<String>(cur_context,android.R.layout.simple_list_item_1, kill_range_list);
		lv2.setAdapter(killarrayAdapter); 
		lv2.setClickable(true);
		//on click listener for kill list
		lv2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			
 	        @Override
 	        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
 	            final String cur_selected_kill = killarrayAdapter.getItem(position);
 	            client.get("http://mafia-web-service.herokuapp.com/killPlayer/" + userID + "/" + cur_selected_kill, new AsyncHttpResponseHandler() {
 	    			@Override
 	    			public void onSuccess(String response){
 	    				Toast toast = Toast.makeText(cur_context, "" + cur_selected_kill + " has been KILLED!", Toast.LENGTH_LONG);
 	    				toast.setGravity(Gravity.TOP, 0, 0);
 	    				toast.show();
 	    			}
 	            });
 	        }
 	    });
		
		
		//timer for always checking those in kill range, excluding other Werewolves and dead players. No players will show if it is night.
		final Timer timer2 = new Timer();
		timer2.scheduleAtFixedRate(new TimerTask(){
			
			@Override
			public void run(){
				client.get("http://mafia-web-service.herokuapp.com/playersNearTo/" + userID + "/4" , new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response){
						kill_range_list.clear();
						Map jsonData=parser.parseJson(response);
						Map rootJson= (Map) jsonData.get("root");
						List al3 = (List) jsonData.get("response");
					
					
						for(int i = 0; i < al3.size(); i++){
							String cur_userID=(String) ((Map)al3.get(i)).get("userID");
							String isDead=(String) ((Map)al3.get(i)).get("isDead");
							String alignment = (String)((Map)al3.get(i)).get("alignment");
							
							if(isDead.equals("false") && alignment.equals("Townsperson") && day_night_cycle == 1){
								kill_range_list.add(cur_userID);
							}
						}
						
						runOnUiThread(new Runnable() {
						     public void run() {
						    	 killarrayAdapter.notifyDataSetChanged();
						    	 
						    }
						});
						
					}
				});	
			}
		}, 1000, 2000);
		
		
		final TextView day_night_text = (TextView)findViewById(R.id.day_night_text);
		
		//timer checking if it is night (so kill time) every minute
		//also send user to voting screen at advent of every morning
		final Timer timer3 = new Timer();
		timer.scheduleAtFixedRate(new TimerTask(){
			
			@Override
			public void run(){
				client.get("http://mafia-web-service.herokuapp.com/getTimeState", new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response){
						if(response.equals("night")){
							if(day_night_cycle == 0 && first_day_cycle == 1){
								Intent voting_intent = new Intent(cur_context, VotingActivity.class);
								voting_intent.putExtra("userID", userID);
								startActivity(voting_intent);
							}
							first_day_cycle = 1;
							day_night_cycle = 1;
							runOnUiThread(new Runnable() {
							     public void run() {
							    	 day_night_text.setText("It is currently NIGHT.");
							    }
							});
						}
						else if(response.equals("day")){
							day_night_cycle = 0;
							runOnUiThread(new Runnable() {
							     public void run() {
							    	 day_night_text.setText("It is currently DAY.");
							    }
							});
						}
					}
				});	
			}
		}, 0, 60000);
		
		//timer to check if game has ended (when there are no Werewolves or TP)
		final Timer game_ending_timer = new Timer();
		game_ending_timer.scheduleAtFixedRate(new TimerTask(){
			
			@Override
			public void run(){
				client.get("http://mafia-web-service.herokuapp.com/getAllAlivePlayers", new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response){
						Map jsonData=parser.parseJson(response);
						Map rootJson= (Map) jsonData.get("root");
						List al= (List) jsonData.get("response");
						
						int num_werewolves = 0;
						int num_townspeople = 0;
						
						for(int i = 0; i < al.size(); i++){
							String alignment = (String) ((Map)al.get(i)).get("alignment");
							if(alignment.equals("Werewolf")){
								num_werewolves++;
							}
							else if(alignment.equals("Townsperson")){
								num_townspeople++;
							}
						}
						
						if(num_werewolves == 0 || num_townspeople == 0){
							Intent intent = new Intent(cur_context, GameEndingActivity.class);
							intent.putExtra("userID", userID);
							startActivity(intent);
							game_ending_timer.cancel();
							timer.cancel();
							timer2.cancel();
							timer3.cancel();
						}
					}
				});	
			}
		}, 400, 2000);
		
		//TODO voting screen for both werewolves and townspeople every morning
		//==========================================================================================================
		
        
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.werewolf, menu);
		return true;
	}
	
	@Override
    public void onResume() {
        super.onResume();

        refreshLocation();

        // This demonstrates how to dynamically create a receiver to listen to the location updates.
        // You could also register a receiver in your manifest.
        final IntentFilter lftIntentFilter = new IntentFilter(LocationLibraryConstants.getLocationChangedPeriodicBroadcastAction());
        registerReceiver(lftBroadcastReceiver, lftIntentFilter);
   }
	
	@Override
    public void onPause() {
        super.onPause();
        
        unregisterReceiver(lftBroadcastReceiver);
   }
	
	private void refreshLocation(){
		refreshLocation(new LocationInfo(this));
	}
	
	private void refreshLocation(final LocationInfo locationInfo){
		//init Async client for web service access
    	final AsyncHttpClient client2 = new AsyncHttpClient();
		client2.setBasicAuth("specialkeythatnoonewilleverknow", "specialerpasswordisawesome");

		if(locationInfo.anyLocationDataReceived()){
			client2.get("http://mafia-web-service.herokuapp.com/updateLocation/" + userID + "/" + Float.toString(locationInfo.lastLat) + "/" + Float.toString(locationInfo.lastLong), new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(String response){
				}
			});	
			if (locationInfo.hasLatestDataBeenBroadcast()) {
                Log.v("refreshLocation", "Latest location has been broadcast");
            }
		}
		
	}
	
	private final BroadcastReceiver lftBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	//Toast.makeText(getApplicationContext(), "In Broadcast Receiver", Toast.LENGTH_LONG).show();
            // extract the location info in the broadcast
            final LocationInfo locationInfo = (LocationInfo) intent.getSerializableExtra(LocationLibraryConstants.LOCATION_BROADCAST_EXTRA_LOCATIONINFO);
            // refresh the display with it
            refreshLocation(locationInfo);
        }
    };

}
