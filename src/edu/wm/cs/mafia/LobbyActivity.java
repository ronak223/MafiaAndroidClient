package edu.wm.cs.mafia;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.json.parsers.JSONParser;
import com.json.parsers.JsonParserFactory;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class LobbyActivity extends Activity {
	int isAdmin = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lobby);
		
		//hiding BEGIN button
		final Button begin_button = (Button)findViewById(R.id.lobby_begin_button);
		begin_button.setVisibility(View.GONE);

		//init progress bar
		final ProgressDialog progress = new ProgressDialog(this);
    	progress.setTitle("Loading");
    	progress.setMessage("Please wait...");
		
    	//init Async client for web service access
    	final AsyncHttpClient client = new AsyncHttpClient();
		client.setBasicAuth("specialkeythatnoonewilleverknow", "specialerpasswordisawesome");
		
		
		//getting userID from intent
		Intent intent = getIntent();
		String userID = intent.getStringExtra("userID");
		
		progress.show();
		
		//checking for admin
		client.get("http://mafia-web-service.herokuapp.com/getSpecificValue/" + userID + "/isAdmin", new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response){
				if(response.equals("false")){
					isAdmin = 0;
					Button start_game_button = (Button)findViewById(R.id.lobby_start_game_button);
					start_game_button.setVisibility(View.GONE);	
				}
				else{
					isAdmin = 1;
					TextView help_text = (TextView)findViewById(R.id.lobby_please_wait_textview);
					help_text.setText("Players will be listed as they join; press Start Game to initialize game.");
				}
				progress.dismiss();
			}
		});	
    	
		//getting listView by resource ID, and context
		final ListView lv = (ListView)findViewById(R.id.lobby_names_listview);
		final Context cur_context = getApplicationContext();
		
    	//progress.show();
    	
    	//setting up JsonFactory
    	JsonParserFactory factory=JsonParserFactory.getInstance();
    	final JSONParser parser=factory.newJsonParser();
    	
    	//initializing list for player names
		final ArrayList<String> player_name_list = new ArrayList<String>();
		//setting ListView to array of player userIDs that are currently queued up to play
		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(cur_context,android.R.layout.simple_list_item_1, player_name_list);
		lv.setAdapter(arrayAdapter); 
		
		//timer for updating player listview in lobby every 2 seconds automatically
		final Timer timer_player_updater = new Timer();
		timer_player_updater.scheduleAtFixedRate(new TimerTask(){
			
			@Override
			public void run(){
				client.get("http://mafia-web-service.herokuapp.com/getAllPlayers", new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response){
						player_name_list.clear();
						
						Map jsonData=parser.parseJson(response);
						Map rootJson= (Map) jsonData.get("root");
						List al= (List) jsonData.get("response");
						
						for(int i = 0; i < al.size(); i++){
							String userID=(String) ((Map)al.get(i)).get("userID");
							player_name_list.add(userID);
							
						}
						//setting ListView to array of player userIDs that are currently queued up to play
						//ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(cur_context,android.R.layout.simple_list_item_1, player_name_list);
						//lv.setAdapter(arrayAdapter); 
						runOnUiThread(new Runnable() {
						     public void run() {
						    	 arrayAdapter.notifyDataSetChanged();
						    }
						});
					}
				});	
			}
		}, 0, 2000);
		
		//using Timer class to create separate, fix-rate thread for updating when game has been initiated
		final Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask(){
			
			@Override
			public void run(){
				client.get("http://mafia-web-service.herokuapp.com/isGameActive", new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response){
						if(response.equals("true") && isAdmin == 0){
							runOnUiThread(new Runnable() {
							     public void run() {
							    	 begin_button.setVisibility(View.VISIBLE);
							    }
							});
							timer.cancel();
						}
					}
				});	
			}
		}, 0, 2000);
		
		//TODO allow only ADMIN to restart game frequency here; once game is started, it is set
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.lobby, menu);
		return true;
	}
	
	public void startGame(View view){
		//TODO need to send admin's coords from here to update
		//loading spinner
		final ProgressDialog progress = new ProgressDialog(this);
    	progress.setTitle("Loading");
    	progress.setMessage("Please wait...");
    	progress.show();
    	
    	//TODO if WW, go to WW screen, else if TP, go to TP screen
		AsyncHttpClient client = new AsyncHttpClient();
		client.setBasicAuth("specialkeythatnoonewilleverknow", "specialerpasswordisawesome");
		client.get("http://mafia-web-service.herokuapp.com/initGame", new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response){
				//dismissing progress bar
				//if true, then we good, else false, not enough players
				progress.dismiss();
			}
		});	
	}
	
	//for button that appears on admin's StartGame
	public void beginNonAdmin(View view){
		//TODO send coordinate updates here for non-admins
		//TODO if WW, go to WW screen, else if TP, go to TP screen
	}

}
