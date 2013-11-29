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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
	
    //upon Login button press
    public void toGameScreen(View view){ 	
		final String user_id = ((EditText)findViewById(R.id.userid_input)).getText().toString();
		String pass = ((EditText)findViewById(R.id.password_input)).getText().toString();
		
		//loading spinner
		final ProgressDialog progress = new ProgressDialog(this);
    	progress.setTitle("Logging in");
    	progress.setMessage("Please wait...");
    	progress.show();
    	
    	//setting up toast for invalid login
    	final Context context = getApplicationContext();
    	final CharSequence text = "Invalid Login Credentials. Try again.";
    	final int duration = Toast.LENGTH_LONG;
    	
    	//setting up intent for login
    	final Intent intent = new Intent(this, GameStartActivity.class);
    	
    	//setting up intent for when currently active player re-logs in
    	final Intent intent2 = new Intent(this, LobbyActivity.class);
    	
    	final AsyncHttpClient client = new AsyncHttpClient();
    	client.get("http://mafia-web-service.herokuapp.com/login/" + user_id + "/" + pass, new AsyncHttpResponseHandler(){
    		@Override
    		public void onSuccess(String response){
    			if(response.equals("Logged in successfully")){
    				client.setBasicAuth("specialkeythatnoonewilleverknow", "specialerpasswordisawesome");
    				client.get("http://mafia-web-service.herokuapp.com/doesPlayerExist/" + user_id , new AsyncHttpResponseHandler() {
    					@Override
    					public void onSuccess(String response){
    						if(response.equals("true")){
    							intent2.putExtra("userID", user_id);
    							startActivity(intent2);
    						}
    						else{
    							intent.putExtra("userID", user_id);
    		    				startActivity(intent);
    						}
    						progress.dismiss();
    					}
    				});	
    			}
    			else{
    				EditText user_id_box = (EditText)findViewById(R.id.userid_input);
    	    		EditText user_pass_box = (EditText)findViewById(R.id.password_input);
    	    		
    	    		user_id_box.setText("");
    	    		user_pass_box.setText("");
    	    		
    	    		progress.dismiss();
    	    		
    	    		Toast toast = Toast.makeText(context, text, duration);
    	    		toast.show();
    			}
    		}
    	});
    }
    
    //upon Register button press
    public void toRegisterScreen(View view){
    	Intent intent = new Intent(this, RegistrationActivity.class);
    	startActivity(intent);
    }
    
}
