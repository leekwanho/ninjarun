package com.example.ninjarun;

public class Score {
	final static int SIZE=30;
	int value;
	
	Cipher cipher[];//자릿수

	public Score() {
		value=0;
		
		cipher=new Cipher[8];//8자리까지 저장
		for(int i=0; i<cipher.length; i++){
			cipher[i]=new Cipher(480-cipher.length*SIZE+SIZE*i, 2);
		}
	}
	
	void setCipher(){
		Integer temp=value;
		String strValue=temp.toString();
	
		for(int i=0, j=cipher.length-strValue.length(); j<cipher.length; i++, j++){
			cipher[j].n=strValue.charAt(i)-'0';
		}
	}
}

class Cipher{
	int x, y;
	int n;
	
	Cipher(){}
	
	Cipher(int x, int y){
		this.x=x;
		this.y=y;
		n=0;
	}
}