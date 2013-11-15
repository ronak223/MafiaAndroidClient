package edu.wm.cs.mafia;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;


public class GameStartActivity extends Activity {
	
	Intent glob_intent;
	int isAdmin_flag = 0;
	NumberPicker day_night_freq_picker;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_start); 
		
		final TextView game_avail_text = (TextView)findViewById(R.id.game_availability_text);
		final Button join_or_create_game_button = (Button)findViewById(R.id.create_or_join_game_button);
		final TextView day_night_freq = (TextView)findViewById(R.id.day_night_freq_text);
		day_night_freq_picker = (NumberPicker)findViewById(R.id.day_night_freq_picker);
		day_night_freq_picker.setMaxValue(24);
		day_night_freq_picker.setMinValue(1);
		
		//intent for AsyncTask
		final Intent intent = new Intent(this, LobbyActivity.class);
		
		//loading spinner
		final ProgressDialog progress = new ProgressDialog(this);
    	progress.setTitle("Loading");
    	progress.setMessage("Please wait...");
    	progress.show();
		
		AsyncHttpClient client = new AsyncHttpClient();
		client.setBasicAuth("specialkeythatnoonewilleverknow", "specialerpasswordisawesome");
		client.get("http://mafia-web-service.herokuapp.com/getCurrentGame", new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response){
				if(response.equals("No games")){
					intent.putExtra("isAdmin", true);
					isAdmin_flag = 1;
				}
				else{
					game_avail_text.setText("A Game is Available!");
					join_or_create_game_button.setText("Join Game");
					day_night_freq.setVisibility(View.GONE);
					day_night_freq_picker.setVisibility(View.GONE);
					intent.putExtra("IsAdmin", false);
					isAdmin_flag = 0;
				}
				glob_intent = intent;
				progress.dismiss();
			}
		});	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game_start, menu);
		return true;
	}

	public void toLobby(View view){
		//TODO configure web application to decide amount of werewolves and townspeople
		//getting userID from sharedPrefs
		SharedPreferences sp1=this.getSharedPreferences("Login",0);
		final String userID = sp1.getString("UserID", null);
		
		//app context to use in AsyncTask
		final Context context = getApplicationContext();
		
		//initializing client for http requests
		final AsyncHttpClient client = new AsyncHttpClient();
		client.setBasicAuth("specialkeythatnoonewilleverknow", "specialerpasswordisawesome");
		
		//progress dialog
		final ProgressDialog progress = new ProgressDialog(this);
    	progress.setTitle("Loading");
    	progress.setMessage("Please wait...");
    	progress.show();
    	
		//first need to create player for either player. Location us default [0,0] and is updated on game start
		client.get("http://mafia-web-service.herokuapp.com/createPlayer/" + userID + "/" + "0" + "/" + "0" + "/Townsperson", new AsyncHttpResponseHandler(){
			@Override
			public void onSuccess(String response){
				if(response.equals("Player for user " + userID + " registered.")){
					Toast.makeText(context, "Player created for " + userID, Toast.LENGTH_SHORT).show();
					
					if(isAdmin_flag == 1){
						//setting up game. Will make associated player an admin for this game
						int cycle_freq = day_night_freq_picker.getValue();
						client.get("http://mafia-web-service.herokuapp.com/startGame/"+ userID +"/" + cycle_freq, new AsyncHttpResponseHandler() {
							@Override
							public void onSuccess(String response){
								if(response.equals("New game started successfully")){
									Toast.makeText(context, "Game created successfully!", Toast.LENGTH_LONG).show();
								}
								else{
									Toast.makeText(context, "Game creation unsuccessful...", Toast.LENGTH_LONG).show();
								}
								progress.dismiss();
							}
						});	
					}
				}
				else{
					Toast.makeText(context, "Player already registered", Toast.LENGTH_LONG).show();
				}
				progress.dismiss();
			}
		});
		startActivity(glob_intent);
	}
}
