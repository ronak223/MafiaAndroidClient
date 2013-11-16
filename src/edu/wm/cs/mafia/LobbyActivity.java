package edu.wm.cs.mafia;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ArrayAdapter;
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
		
		//getting listView by resource ID, and context
		final ListView lv = (ListView)findViewById(R.id.lobby_names_listview);
		final Context cur_context = getApplicationContext();
		
		//loading spinner
		final ProgressDialog progress = new ProgressDialog(this);
    	progress.setTitle("Loading");
    	progress.setMessage("Please wait...");
    	progress.show();
    	
    	//setting up JasonFactory
    	JsonParserFactory factory=JsonParserFactory.getInstance();
    	final JSONParser parser=factory.newJsonParser();
    	
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

}
