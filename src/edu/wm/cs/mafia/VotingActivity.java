package edu.wm.cs.mafia;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class VotingActivity extends Activity {
	//TODO implement all votey stuffz
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_voting);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.voting, menu);
		return true;
	}

}
