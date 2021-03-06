package no.ntnu.item.csv;

import no.ntnu.item.csv.capability.Capability;
import no.ntnu.item.csv.capability.CapabilityImpl;
import no.ntnu.item.csv.capability.CapabilityType;
import no.ntnu.item.csv.contrib.com.bitzi.util.Base32;
import no.ntnu.item.csv.csvobject.CSVFolder;
import no.ntnu.item.csv.exception.FailedToVerifySignatureException;
import no.ntnu.item.csv.exception.RemoteFileDoesNotExistException;
import no.ntnu.item.csv.exception.ServerCommunicationException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ManualImportShareActivity extends Activity {

	private static final int DIALOG_VERIFY_KEY = 1;
	public static final String REQUEST_RESULT_USERNAME = "username";
	public static final String REQUEST_RESULT_CAPABILITY = "capability";

	private EditText keyEditText;
	private EditText usernameEditText;
	private Button processButon;
	private String verifyHash;
	private Capability shareCapability;
	private String username;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manualimportshareactivity);

		this.keyEditText = (EditText) findViewById(R.id.manualshareactivity_keyedittext);
		this.usernameEditText = (EditText) findViewById(R.id.manualshareactivity_usernameedittext);
		this.processButon = (Button) findViewById(R.id.manualshareactivity_processbutton);

		this.processButon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				byte[] encKey = Base32.decode(keyEditText.getText().toString());
				shareCapability = new CapabilityImpl(CapabilityType.RW, encKey,
						null, false);
				username = usernameEditText.getText().toString();

				if (username.equals("")) {
					Toast.makeText(ManualImportShareActivity.this,
							"The folder name cannot be blank",
							Toast.LENGTH_LONG).show();
				} else {
					CSVFolder folder;
					try {
						folder = CSVActivity.fm.downloadFolder(shareCapability);
						if (folder == null) {
							Toast.makeText(
									ManualImportShareActivity.this,
									"The requested share capability does not exist",
									Toast.LENGTH_LONG).show();
						} else {
							verifyHash = Base32.encode(folder
									.getPublicKeyHash());
							showDialog(DIALOG_VERIFY_KEY);
						}
					} catch (ServerCommunicationException e) {
						Toast.makeText(ManualImportShareActivity.this,
								"There was an error communicating with server",
								Toast.LENGTH_LONG).show();
					} catch (RemoteFileDoesNotExistException e) {
						Toast.makeText(ManualImportShareActivity.this,
								"Invalid key specified", Toast.LENGTH_LONG)
								.show();
					} catch (FailedToVerifySignatureException e) {
						// Should not happen since we don't have a key to
						// compare with
					}
				}

			}
		});

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_VERIFY_KEY:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Is the following key correct?:\n"
					+ this.verifyHash);
			builder.setPositiveButton("Yes",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent intent = getIntent();
							intent.putExtra(REQUEST_RESULT_CAPABILITY,
									shareCapability.toString() + ":"
											+ verifyHash);
							intent.putExtra(REQUEST_RESULT_USERNAME, username);
							setResult(RESULT_OK, intent);
							dismissDialog(DIALOG_VERIFY_KEY);
							finish();
						}
					});
			builder.setNegativeButton("No",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dismissDialog(DIALOG_VERIFY_KEY);
						}
					});
			dialog = builder.create();
			break;
		default:
			dialog = null;
		}
		return dialog;
	}
}
