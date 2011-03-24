package no.ntnu.item.csv;

import no.ntnu.item.csv.capability.Capability;
import no.ntnu.item.csv.capability.CapabilityImpl;
import no.ntnu.item.csv.credentials.DisplayCapability;
import no.ntnu.item.csv.credentials.LocalCredentials;
import no.ntnu.item.csv.csvobject.man.CSVFileManager;
import no.ntnu.item.csv.firststart.FirstStartActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class CSVActivity extends Activity {
	/** Called when the activity is first created. */

	public String myString = "foobar";
	private Button bDownload;
	private Button bUpload;
	private Button bShare;
	public static CSVFileManager fm;

	// File manager enabling remote browsing in cloud
	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		LocalCredentials creds = new LocalCredentials(
				this.getApplicationContext(), false);
		if (creds.isFirstStart()) {
			Intent intent = new Intent();
			intent.setClass(this, FirstStartActivity.class);
			startActivityForResult(intent, 1);
		} else {
			fm = new CSVFileManager(creds.getRootCapability());
		}

		// Capture viewed buttons.
		bDownload = (Button) findViewById(R.id.menu_download);
		bUpload = (Button) findViewById(R.id.menu_upload);
		bShare = (Button) findViewById(R.id.menu_share);

		// Add button listeners.
		bDownload.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(CSVActivity.this, RemoteBrowseActivity.class);
				startActivity(intent);
			}
		});

		bUpload.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				DisplayCapability.displayCapability(CSVActivity.this,
						CSVActivity.fm.getCurrentFolder().getCapability())
						.show();
			}
		});

		bShare.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				 Intent intent = new Intent(CSVActivity.this, CreateShareActivity.class);
				 startActivity(intent);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// TODO: Modify this if we wait for other results
		String root = (String) data.getExtras().get("rootcap");
		Capability rootcap = CapabilityImpl.fromString(root);
		fm = new CSVFileManager(rootcap);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// FIXME: Just for testing purposes
		menu.add("Delete Capability");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getTitle().equals("Delete Capability")) {
			deleteFile(LocalCredentials.save_file);
			finish();
			return true;
		}
		return false;
	}

}
