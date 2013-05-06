package com.shining.spsecurity;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class DetailActivity extends Activity {

	private ListView listView;
	
	private ListAdapter listAdapter;
	
	private ArrayList<String> list_result=new ArrayList<String>();
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_detail);
		
		list_result=(ArrayList<String>)getIntent().getSerializableExtra("result");
		
		listView=(ListView)findViewById(R.id.listview);
		
		listAdapter=new ArrayAdapter<String>(this,R.layout.list_item, list_result);
		
		listView.setAdapter(listAdapter);
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	
		getMenuInflater().inflate(R.menu.detail, menu);
		return true;
	}

}
