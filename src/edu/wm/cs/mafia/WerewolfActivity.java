package edu.wm.cs.mafia;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.json.parsers.JSONParser;
import com.json.parsers.JsonParserFactory;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class WerewolfActivity extends Activity {

	//=====================================================================================
	String userID;
	int num_players;
	
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
		final Timer timer3 = new Timer();
		timer.scheduleAtFixedRate(new TimerTask(){
			
			@Override
			public void run(){
				client.get("http://mafia-web-service.herokuapp.com/getTimeState", new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response){
						if(response.equals("night")){
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
				client.get("http://mafia-web-service.herokuapp.com/getAllPlayers", new AsyncHttpResponseHandler() {
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
		}, 400, 60000);
		
		//TODO constantly update position
		//==========================================================================================================
		
        
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.werewolf, menu);
		return true;
	}
	
	//for updating current user's location
	public void updateLocation(Location location){
		//init Async client for web service access
    	final AsyncHttpClient client2 = new AsyncHttpClient();
		client2.setBasicAuth("specialkeythatnoonewilleverknow", "specialerpasswordisawesome");
		
		client2.get("http://mafia-web-service.herokuapp.com/updateLocation/" + userID + "/" + location.getLatitude() + "/" + location.getLongitude(), new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response){
			}
		});	
	}

}
