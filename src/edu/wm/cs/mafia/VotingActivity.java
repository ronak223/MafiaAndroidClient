package edu.wm.cs.mafia;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.json.parsers.JSONParser;
import com.json.parsers.JsonParserFactory;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class VotingActivity extends Activity {
	//TODO implement all votey stuffz
	
	String userID;
	String type;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_voting);
		
		Intent prev_intent = getIntent();
		userID = prev_intent.getStringExtra("userID");
		type = prev_intent.getStringExtra("type");
		
		//getting list view to update
		ListView voting_list = (ListView)findViewById(R.id.voting_list);
		
		//init Async client for web service access
		final AsyncHttpClient client = new AsyncHttpClient();
		client.setBasicAuth("specialkeythatnoonewilleverknow", "specialerpasswordisawesome");
		
		//initializing list for alive player names
		final ArrayList<String> player_name_list = new ArrayList<String>();
		
		//setting listview to array of player userIDs that are alive
		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1, player_name_list);
		voting_list.setAdapter(arrayAdapter);
		voting_list.setClickable(true);
		voting_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			
 	        @Override
 	        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
 	            final String cur_selected_vote = arrayAdapter.getItem(position);
 	            client.get("http://mafia-web-service.herokuapp.com/placeVote/" + userID + "/" + cur_selected_vote, new AsyncHttpResponseHandler() {
 	    			@Override
 	    			public void onSuccess(String response){
 	    				Toast toast = Toast.makeText(getApplicationContext(), "You have voted for: " + cur_selected_vote, Toast.LENGTH_LONG);
 	    				toast.setGravity(Gravity.TOP, 0, 0);
 	    				toast.show();
 	    				
 	    				//need to restart WW or TP activity
 	    				if(type.equals("Werewolf")){
 	    					Intent intent = new Intent(getApplicationContext(), WerewolfActivity.class);
 	    					intent.putExtra("userID", userID);
 	    					startActivity(intent);
 	    				}
 	    				else if(type.equals("Townsperson")){
 	    					Intent intent = new Intent(getApplicationContext(), TownspersonActivity.class);
 	    					intent.putExtra("userID", userID);
 	    					startActivity(intent);
 	    				}
 	    			}
 	            });
 	        }
 	    });
		
		//setting up JsonFactory
    	JsonParserFactory factory=JsonParserFactory.getInstance();
    	final JSONParser parser=factory.newJsonParser();
		
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
		
		//TODO find a way to properly count votes and kill appropriate player.
		//May potentially need one more activity for "having been hung" for WW or TP to go to under those circumstances
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.voting, menu);
		return true;
	}

}
