package edu.wm.cs.mafia;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.json.parsers.JSONParser;
import com.json.parsers.JsonParserFactory;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class GameEndingActivity extends Activity {

	//TODO Implement GameEnd Summary
	//TODO after summary, remove all kills, players, and the game from DB
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_ending);
		
		//getubg userID, and winning team
		Intent intent = getIntent();
		String userID = intent.getStringExtra("userID");
		String winning_team = intent.getStringExtra("winning_team");
		
		//getting Views
		TextView winning_team_text = (TextView)findViewById(R.id.winning_team_text);
		final TextView highest_points_text = (TextView)findViewById(R.id.highest_points_text);
		final TextView total_kills_text = (TextView)findViewById(R.id.total_kills_text);
		ListView alive_players_list = (ListView)findViewById(R.id.alive_player_summary_list);
		
		//setting winning team text
		winning_team_text.setText(winning_team);
		
		//initializing list for alive player names
		final ArrayList<String> player_name_list = new ArrayList<String>();
		
		//setting listview to array of player userIDs that are alive
		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1, player_name_list);
		alive_players_list.setAdapter(arrayAdapter);
		
		//setting up JsonFactory
    	JsonParserFactory factory=JsonParserFactory.getInstance();
    	final JSONParser parser=factory.newJsonParser();
    	
		//init Async client for web service access
		final AsyncHttpClient client = new AsyncHttpClient();
		client.setBasicAuth("specialkeythatnoonewilleverknow", "specialerpasswordisawesome");
		
		//populating alive player lists
		client.get("http://mafia-web-service.herokuapp.com/getAllAlivePlayers", new AsyncHttpResponseHandler() {
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
				runOnUiThread(new Runnable() {
				     public void run() {
				    	 arrayAdapter.notifyDataSetChanged();
				    }
				});
			}
		});	
		
		//getting highscore player
		client.get("http://mafia-web-service.herokuapp.com/getHighscore", new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response){
				Map jsonData=parser.parseJson(response);
				Map rootJson= (Map) jsonData.get("root");
				final String highscore_player = (String) jsonData.get("userID");
				
				runOnUiThread(new Runnable() {
				     public void run() {
				    	 highest_points_text.setText(highscore_player);
				    }
				});
			}
		});	
		
		//getting number of kills
		client.get("http://mafia-web-service.herokuapp.com/getKillCount", new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(final String response){
				runOnUiThread(new Runnable() {
				     public void run() {
				    	 total_kills_text.setText(response);
				    }
				});
			}
		});	
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game_ending, menu);
		return true;
	}
	
	public void concludeGame(View view){
		//init Async client for web service access
		final AsyncHttpClient client2 = new AsyncHttpClient();
		client2.setBasicAuth("specialkeythatnoonewilleverknow", "specialerpasswordisawesome");
		
		//conclude game on server
		client2.get("http://mafia-web-service.herokuapp.com/concludeGame", new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response){
				//do nothing, simply takes care of DB stuff
			}
		});	
		
		//send user back to MainActivity
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		
	}

}
