package com.example.testgallery;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import java.io.File;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button myButton;
        myButton = (Button)findViewById(R.id.button);        
        myButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				String filePath = Environment.getExternalStorageDirectory().getPath() + "/DCIM";
				File file = new File(filePath, "1.jpg");
				Uri uri;
				if (Build.VERSION.SDK_INT >= 24) {				    
				    uri = FileProvider.getUriForFile(MainActivity.this, getApplicationContext().getPackageName() + ".fileprovider", file);
				    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

				} else {
				    uri = Uri.fromFile(file);
				}
				Log.i("mine", "Uri:" + uri);
				intent.setDataAndType(uri, "image/*");
				startActivity(intent);
				
				/*Intent intent = new Intent(Intent.ACTION_VIEW, null);
				intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null);
				startActivity(intent);*/
			}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
