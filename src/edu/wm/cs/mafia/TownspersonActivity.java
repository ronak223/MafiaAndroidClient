package edu.wm.cs.mafia;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.json.parsers.JSONParser;
import com.json.parsers.JsonParserFactory;
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
		
		
		//TODO constantly update position
		//TODO timer for checking if game is over, then moving to summary screen
		//TODO voting screen for both werewolves and townspeople every morning
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.townsperson, menu);
		return true;
	}

}
