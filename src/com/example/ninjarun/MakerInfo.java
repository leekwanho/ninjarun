package com.example.ninjarun;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;

public class MakerInfo extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.maker_info);
		
		InputStream iFile=getResources().openRawResource(R.raw.maker_info);
		
		TextView textView=(TextView)findViewById(R.id.maker_info);
		
		try{
			InputStreamReader isr = new InputStreamReader(iFile, "euc-kr");//ÇÑ±Û±úÁü¹æÁö
			StringBuffer sBuffer=new StringBuffer();
			BufferedReader br=new BufferedReader(isr);
			String str=null;
			
			while((str=br.readLine())!=null){
				sBuffer.append(str + "\n");
			}
			br.close();
			textView.setText(sBuffer.toString());
			
		}catch(Exception e){}
	
		
	}

}
