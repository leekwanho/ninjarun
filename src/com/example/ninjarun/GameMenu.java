package com.example.ninjarun;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import org.json.*;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.view.*;
import android.widget.*;

public class GameMenu extends Activity {
	static final int PORT=4121;
	
	private Socket clientSocket;
	PrintWriter out;
	BufferedReader in;
	Thread scoreThread;//스코어 받기위한 쓰레드
	String receive=" ";//스코어 받은거 저장할 스트링 변수
	
	int myRank;//자신이 몇등인지 저장변수
	
	Boolean threadOnOff=true;
	Boolean serverOnOff=true;
	
	AlertDialog mPopupDlg;//다이얼로그 종료용
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gamemenu);

		UserInfo.topRank.clear();//초기화 안해주면 어플이 작동되는동안 계속 누적이됨
		
		//아이디를 보내고 그에따른 스코어를 받기위한 스레드
		scoreThread = new Thread() {
			   public void run() {

					try{
						SocketAddress socketAddress=new InetSocketAddress(GameInfo.ipAddress,PORT);
						clientSocket=new Socket();//소켓생성
						clientSocket.connect(socketAddress, 3000);//서버연결은 3초동안
						
						threadOnOff=true;//스레드 랭킹 받을때까지 무한반복
						
						send();//값을 보냄
						
			    		while(threadOnOff){
			    			
			    			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			    			
			    			if(!in.equals(null)){
				    			receive=in.readLine();//받는값
			    				break;
			    			}//값을 받았으면 종료			    
			    		}
					}catch(Exception e){
						e.printStackTrace();
						serverOnOff=false;//서버 죽어있다고 알림
					}
			   }
		};

		scoreThread.start();//스레드 실행
		
		while(serverOnOff){//응답을 받을때까지 계속 반복. 받았다면 break로 빠져나옴
			if(!receive.equals(" ")){//전달받은 값이 있을때 실행
								
				try {
					JSONObject jb=new JSONObject(receive);//JSON생성
					
					UserInfo.myScore=jb.getInt("myscore");//내점수뽑아옴
					
					JSONArray ja=new JSONArray(jb.getString("topscore"));
					//topscore는 배열형식이므로 JSON배열 생성					
					
					for(int i=0;i<10;i++){//탑랭크점수 저장
						UserInfo.topRank.add(ja.getString(i));//탑스코어에 저장
					}
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			}
		}
		
		if(serverOnOff.equals(false)){
			Toast.makeText(this, "랭크 불러오기에 실패했습니다", Toast.LENGTH_SHORT).show();
		}
		
		myRank=0;//랭킹 디폴트는 0
		
		for(int i=0;i<UserInfo.topRank.size();i++){
			if(UserInfo.myScore==Integer.parseInt(UserInfo.topRank.get(i))){//자신의 스코어가 탑10에 있을시
				myRank=i+1;//myRank에 저장
				break;
			}
		}
		
		//리스트뷰 어댑터 설정
		ListView menuList = (ListView) findViewById(R.id.topRank);
		menuList.setAdapter(new CustomAdapter(this,R.layout.gamerankitem,UserInfo.topRank));
		TextView textView=(TextView)findViewById(R.id.myRank);
		
		//자신의 랭킹 및 스코어 출력
		if(myRank==0){//랭킹 변화없는건 탑10에 없는것
			textView.setText("나의점수 : " + UserInfo.myScore + "점 \n랭킹10위밖");
		}
		else{//그외엔 탑10에 존재
			textView.setText("나의점수 : " + UserInfo.myScore + "점\n랭킹" + myRank + "위");//자신의 랭킹 및 스코어 출력
			menuList.setSelection(myRank-1);//포커스 이동
		}
	}
	
	public void mOnClick(View v){
		
		switch(v.getId()){//클릭시 이동
		case R.id.recreate_btn:
			recreate();
			break;
			
		case R.id.makerinfo_btn:
			startActivity(new Intent(this,MakerInfo.class));
			break;
			
		case R.id.game_btn:
			// Activity 시작
			startActivity(new Intent(this,NinjaRunActivity.class));
			finish();
			break;
		}
	}
		
	public void send(){//스코어를 받기위해 아이디를 보냄
		try{
			out=new PrintWriter(clientSocket.getOutputStream(),true);
			out.println(UserInfo.id);
		}catch(Exception e){
	    	Toast.makeText(this, "send 실패", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	 protected void onPause() {
		super.onPause();
		try {
			threadOnOff=false;
			
			if(clientSocket!=null){
				clientSocket.close(); //소켓을 닫는다.
			}
			if(in!=null){
				in.close();
			}
			if(out!=null){
				out.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		catch (Exception e){}
	 }
	
	@Override
	public void onBackPressed(){//뒤로가기시 로그인페이지로
		final Intent intent=new Intent(this,Login.class);
		
		/** 다이얼로그 셋팅 */
		View view = this.getLayoutInflater().inflate(R.layout.dialog_gamemenu, null);//뷰를 가져옴

		mPopupDlg=new AlertDialog.Builder(this).create();//생성자
		mPopupDlg.setView(view,0,0,0,0);//여백 생기지 않게하기위함
		mPopupDlg.show();//다이얼로그 실행
		
		Button exitBtn=(Button) view.findViewById(R.id.exit);//다이얼로그에서 종료버튼
		exitBtn.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				mPopupDlg.dismiss();//다이얼로그없애고
				startActivity(intent);//로그인 액티비티로 나간후
				finish();//액티비티종료
			}			
		});
		
		Button backBtn=(Button) view.findViewById(R.id.back);
		backBtn.setOnClickListener(new Button.OnClickListener(){
			public void onClick(View v) {
				mPopupDlg.dismiss();//다이얼로그만 없앰
			}			
		});
	}
}

//리스트뷰 어댑터
class CustomAdapter extends ArrayAdapter<String>{
	
	private LayoutInflater inflater=null;
	private ArrayList<String> info=new ArrayList<String>();
	
	//생성자 기본세팅
	public CustomAdapter(Context c,int textViewResourceId,ArrayList<String> arrays){
		super(c,textViewResourceId,arrays);
		this.info=arrays;
		this.inflater=LayoutInflater.from(c);

	}
	
	@Override
	public int getCount() {
		return super.getCount();
	}

	@Override
	public String getItem(int position) {
		return super.getItem(position);
	}

	@Override
	public long getItemId(int position) {
		return super.getItemId(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v=convertView;
		v=inflater.inflate(R.layout.gamerankitem,null);
		
		String s=info.get(position);
		//배열에 있는걸 s에 저장
		((TextView)v.findViewById(R.id.rank_item_menu)).setText(position+1 + "위 점수 : " + s);
		//리스트뷰의 텍스트뷰에 텍스트 저장

		switch(position){//1~10중 1~3위는 색깔지정
		case 0:
			v.setBackgroundResource(R.drawable.gold);
			break;
		case 1:
			v.setBackgroundResource(R.drawable.silver);
			break;
		case 2:
			v.setBackgroundResource(R.drawable.bronze);
			break;		
		}
		
		return v;
	}
	
}