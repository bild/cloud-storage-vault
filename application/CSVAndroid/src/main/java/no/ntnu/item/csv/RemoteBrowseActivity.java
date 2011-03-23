package no.ntnu.item.csv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import no.ntnu.item.csv.capability.Capability;
import no.ntnu.item.csv.exception.NoSuchAliasException;
import no.ntnu.item.csv.workers.CreateFolderTask;
import no.ntnu.item.csv.workers.DownloadTask;
import no.ntnu.item.csv.workers.UploadTask;

import org.apache.http.client.ClientProtocolException;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class RemoteBrowseActivity extends ListActivity {

	public final int MENU_CREATE_FOLDER = 2;
	public final int MENU_UPLOAD_FILE = 1;

	private static Map<String, Capability> files;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		doBrowsing();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_UPLOAD_FILE, 0, "Upload File");
		menu.add(0, MENU_CREATE_FOLDER, 0, "Create Folder");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = new Intent();

		switch (item.getItemId()) {
		case MENU_CREATE_FOLDER:
			intent.setClass(this, NewFolderActivity.class);
			startActivityForResult(intent, MENU_CREATE_FOLDER);
			return true;
		case MENU_UPLOAD_FILE:
			intent.setClass(this, LocalBrowseActivity.class);
			startActivityForResult(intent, MENU_UPLOAD_FILE);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void doBrowsing() {
		files = CSVActivity.fm.ls();
		// files.put("..", null);

		List<String> tmpList = new ArrayList<String>();
		tmpList.addAll(files.keySet());
		Collections.sort(tmpList);
		tmpList.add(0, "..");
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.test_list_item, tmpList));
		System.out.print("Done");

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				TextView tmp = (TextView) view;
				String alias = tmp.getText().toString();
				Capability cap = files.get(alias);
				if (alias.equals("..") || cap.isFolder()) {
					try {
						CSVActivity.fm.cd(alias);
						doBrowsing();
					} catch (ClientProtocolException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoSuchAliasException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					new DownloadTask(RemoteBrowseActivity.this).execute(alias);
				}
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case MENU_UPLOAD_FILE:
				new UploadTask(this).execute(data.getStringExtra("FILEPATH"));
				break;
			case MENU_CREATE_FOLDER:
				new CreateFolderTask(this).execute(data
						.getStringExtra(NewFolderActivity.NEW_FOLDER));
			default:
				;
			}
		}
	}
}
