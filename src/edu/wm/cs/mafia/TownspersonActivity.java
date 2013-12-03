package edu.wm.cs.mafia;

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
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.json.parsers.JSONParser;
import com.json.parsers.JsonParserFactory;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibraryConstants;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class TownspersonActivity extends Activity {

	//globals
	String userID;
	int num_players;
	
	//0 is false, 1 is true
	int isDeadFlag = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_townsperson);
		
		//getting current userID and number of players
		Intent intent = getIntent();
		userID = intent.getStringExtra("userID");
		num_players = intent.getIntExtra("numPlayers", 0);
		
		//init Async client for web service access
		final AsyncHttpClient client = new AsyncHttpClient();
		client.setBasicAuth("specialkeythatnoonewilleverknow", "specialerpasswordisawesome");
		
		//getting all textviews needed to manipulate
		final TextView day_night_text = (TextView)findViewById(R.id.tp_day_night_text);
		final TextView caution_text = (TextView)findViewById(R.id.tp_caution_text);
		final TextView dead_text = (TextView)findViewById(R.id.tp_dead_text);
		dead_text.setVisibility(View.GONE);
		
		//timer checking if it is night (so kill time) every minute
		final Timer day_night_timer = new Timer();
		day_night_timer.scheduleAtFixedRate(new TimerTask(){
			
			@Override
			public void run(){
				client.get("http://mafia-web-service.herokuapp.com/getTimeState", new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response){
						if(response.equals("night")){
							
							runOnUiThread(new Runnable() {
							     public void run() {
							    	 day_night_text.setText("It is currently NIGHT.");
							    	 caution_text.setText("There are Werewolves about. You can be killed.");
							    }
							});
						}
						else if(response.equals("day")){

							runOnUiThread(new Runnable() {
							     public void run() {
							    	 day_night_text.setText("It is currently DAY.");
							    	 caution_text.setText("You are safe...for now.");
							    }
							});
						}
					}
				});	
			}
		}, 0, 60000);
		
		//setting up JsonFactory
    	JsonParserFactory factory=JsonParserFactory.getInstance();
    	final JSONParser parser=factory.newJsonParser();
		
		//timer checking if player has been killed
		final Timer dead_timer = new Timer();
		dead_timer.scheduleAtFixedRate(new TimerTask(){
			
			@Override
			public void run(){
				client.get("http://mafia-web-service.herokuapp.com/getPlayer/" + userID, new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response){
						if(response.equals("false")){
							//do nothing
						}
						else{
							Map jsonData = parser.parseJson(response);
							String isDead = (String) jsonData.get("isDead");
							if(isDead=="true"){
								isDeadFlag = 1;
								runOnUiThread(new Runnable() {
								     public void run() {
								    	 dead_text.setVisibility(View.VISIBLE);
								    	 day_night_text.setVisibility(View.GONE);
								    	 caution_text.setVisibility(View.GONE);
								    }
								});
								dead_timer.cancel();
							}
						}
					}
				});	
			}
		}, 100, 2000);
		
		final Context cur_ctx = getApplicationContext();
		
		//timer checking if game is over (when no werewolves or TP are left)
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
							Intent intent = new Intent(cur_ctx, GameEndingActivity.class);
							intent.putExtra("userID", userID);
							startActivity(intent);
							game_ending_timer.cancel();
							dead_timer.cancel();
							day_night_timer.cancel();
						}
					}
				});	
			}
		}, 400, 60000);
		
		//TODO timer for checking if game is over, then moving to summary screen
		//TODO voting screen for both werewolves and townspeople every morning
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.townsperson, menu);
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
        	// extract the location info in the broadcast
            final LocationInfo locationInfo = (LocationInfo) intent.getSerializableExtra(LocationLibraryConstants.LOCATION_BROADCAST_EXTRA_LOCATIONINFO);
            // refresh the display with it
            refreshLocation(locationInfo);
        }
    };

}
