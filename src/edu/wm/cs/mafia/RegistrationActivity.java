package edu.wm.cs.mafia;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class RegistrationActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registration);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.registration, menu);
		return true;
	}

	//on Confirm button press
	public void toLoginScreen(View view){
		
		//extracting string from TextFields on UI
		final String reg_userID = ((EditText)findViewById(R.id.reg_userid)).getText().toString();
		String reg_password = ((EditText)findViewById(R.id.reg_password)).getText().toString();
		String reg_reenter_password = ((EditText)findViewById(R.id.reg_reenter_password)).getText().toString();
		String reg_email = ((EditText)findViewById(R.id.reg_email)).getText().toString();
		
		if(reg_password.equals(reg_reenter_password)){
			//asynctask for registration
			AsyncHttpClient client = new AsyncHttpClient();
			client.get("http://mafia-web-service.herokuapp.com/register/" + reg_userID + "/" + reg_password, new AsyncHttpResponseHandler(){
				@Override
				public void onSuccess(String response){
					if(response.equals("User " + reg_userID + " registered")){
						Toast.makeText(getApplicationContext(), "Registration Successful!", Toast.LENGTH_SHORT).show();
					}
					else{
						Toast.makeText(getApplicationContext(), "UserID already registered. Try loggin in.", Toast.LENGTH_LONG).show();
					}
				}
			});
			
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
		}
		else{
			EditText reg_user_field = (EditText)findViewById(R.id.reg_userid);
			EditText reg_user_pass = (EditText)findViewById(R.id.reg_password);
			EditText reg_user_validate_pass = (EditText)findViewById(R.id.reg_reenter_password);
			EditText reg_user_email = (EditText)findViewById(R.id.reg_email);
			
			reg_user_field.setText("");
			reg_user_pass.setText("");
			reg_user_validate_pass.setText("");
			reg_user_email.setText("");
			
			Toast.makeText(getApplicationContext(), "Passwords did not match! Enter again.", Toast.LENGTH_LONG).show();
		}
	}
}



