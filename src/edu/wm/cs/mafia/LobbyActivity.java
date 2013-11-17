package edu.wm.cs.mafia;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.json.parsers.JSONParser;
import com.json.parsers.JsonParserFactory;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class LobbyActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lobby);
		
		//hiding Start Game button if not admin
		/*
		Intent intent = getIntent();
		boolean isAdmin = intent.getBooleanExtra("isAdmin", false);
		if(isAdmin == false){
			Button start_game_button = (Button)findViewById(R.id.lobby_start_game_button);
			start_game_button.setVisibility(View.GONE);	
		}
		*/
		//init progress bar
		final ProgressDialog progress = new ProgressDialog(this);
    	progress.setTitle("Loading");
    	progress.setMessage("Please wait...");
		
    	//init Async client for web service access
    	AsyncHttpClient client = new AsyncHttpClient();
		client.setBasicAuth("specialkeythatnoonewilleverknow", "specialerpasswordisawesome");
		
		////getting userID from sharedPrefs
		SharedPreferences sp1=this.getSharedPreferences("Login",0);
		final String userID = sp1.getString("UserID", null);
		
		progress.show();
		
		//checking for admin
		client.get("http://mafia-web-service.herokuapp.com/getSpecificValue/" + userID + "/isAdmin", new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response){
				if(response == "false"){
					Button start_game_button = (Button)findViewById(R.id.lobby_start_game_button);
					start_game_button.setVisibility(View.GONE);	
				}
				progress.hide();
			}
		});	
    	
		//getting listView by resource ID, and context
		final ListView lv = (ListView)findViewById(R.id.lobby_names_listview);
		final Context cur_context = getApplicationContext();
		
    	progress.show();
    	
    	//setting up JsonFactory
    	JsonParserFactory factory=JsonParserFactory.getInstance();
    	final JSONParser parser=factory.newJsonParser();
    	
		client.get("http://mafia-web-service.herokuapp.com/getAllPlayers", new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response){
				Map jsonData=parser.parseJson(response);
				Map rootJson= (Map) jsonData.get("root");
				List al= (List) jsonData.get("response");
				
				//initializing list for player names
				ArrayList<String> player_name_list = new ArrayList<String>();
				
				for(int i = 0; i < al.size(); i++){
					String userID=(String) ((Map)al.get(i)).get("userID");
					player_name_list.add(userID);
					
				}
				//setting ListView to array of player userIDs that are currently queued up to play
				ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(cur_context,android.R.layout.simple_list_item_1, player_name_list);
				lv.setAdapter(arrayAdapter); 
				
				//dismissing progress bar
				progress.dismiss();
			}
		});	
		
		//TODO Need a field for "active" game, such that others can't join once game is active
		//TODO Add automatic updating of players to listView as they join
		//TODO button to allow ADMIN to start game, getting coords of remaining players and starting game, as well as setting werewolf/townsperson ratio (through web service)
		//TODO players that are not admin must wait for admin to start game before playing
		//TODO allow only ADMIN to restart game frequency here; once game is started, it is set
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.lobby, menu);
		return true;
	}
	
	public void refreshPlayerList(View view){
		//setting up JsonFactory
    	JsonParserFactory factory=JsonParserFactory.getInstance();
    	final JSONParser parser=factory.newJsonParser();
    	
    	//getting listView by resource ID, and context
		final ListView lv = (ListView)findViewById(R.id.lobby_names_listview);
		final Context cur_context = getApplicationContext();
    	
    	//loading spinner
		final ProgressDialog progress = new ProgressDialog(this);
    	progress.setTitle("Loading");
    	progress.setMessage("Please wait...");
    	progress.show();
    	
    	AsyncHttpClient client = new AsyncHttpClient();
		client.setBasicAuth("specialkeythatnoonewilleverknow", "specialerpasswordisawesome");
		client.get("http://mafia-web-service.herokuapp.com/getAllPlayers", new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response){
				Map jsonData=parser.parseJson(response);
				Map rootJson= (Map) jsonData.get("root");
				List al= (List) jsonData.get("response");
				
				//initializing list for player names
				ArrayList<String> player_name_list = new ArrayList<String>();
				
				for(int i = 0; i < al.size(); i++){
					String userID=(String) ((Map)al.get(i)).get("userID");
					player_name_list.add(userID);
					
				}
				//setting ListView to array of player userIDs that are currently queued up to play
				ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(cur_context,android.R.layout.simple_list_item_1, player_name_list);
				lv.setAdapter(arrayAdapter); 
				
				//dismissing progress bar
				progress.dismiss();
			}
		});	
	}

	public void startGame(View view){
		//loading spinner
		final ProgressDialog progress = new ProgressDialog(this);
    	progress.setTitle("Loading");
    	progress.setMessage("Please wait...");
    	progress.show();
    	
		AsyncHttpClient client = new AsyncHttpClient();
		client.setBasicAuth("specialkeythatnoonewilleverknow", "specialerpasswordisawesome");
		client.get("http://mafia-web-service.herokuapp.com/initGame", new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response){
				//dismissing progress bar
				progress.dismiss();
			}
		});	
	}

}
