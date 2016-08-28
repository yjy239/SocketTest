package com.example.ipcforsocket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.ipcforsocket.socket.TPCService;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener{
	
	private static final int MESSAGE_RECEIVE_NEW_MSG = 1;
	private static final int MESSAGE_SOCKET_CONNECTD = 2;
	
	private Button button;
	private EditText edit;
	private TextView text;
	
	private PrintWriter writer;
	private Socket client;
	
	//UI中修改组件，最好交给Handler去完成
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch (msg.what) {
			case MESSAGE_RECEIVE_NEW_MSG:
				text.setText(text.getText()+(String)msg.obj);
				break;
				
			case MESSAGE_SOCKET_CONNECTD:
				button.setEnabled(true);
				break;

			default:
				break;
			}
		}
	};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edit = (EditText)findViewById(R.id.msg);
        text = (TextView)findViewById(R.id.msg_container);
        button = (Button)findViewById(R.id.send);
        button.setOnClickListener(this);
        Intent intent = new Intent(MainActivity.this,TPCService.class);
        startService(intent);
        //连接网络的线程
        new Thread(){
        	@Override
        	public void run(){
        		connectTPCService();
        	}
        }.start();
    }
    
    @Override
    protected void onDestroy(){
    	if(client != null){
    		try {
				client.shutdownInput();
				client.close();
			} catch (IOException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
    	}
    	super.onDestroy();
    }



	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v == button){
			final String msg = edit.getText().toString();
			if(!TextUtils.isEmpty(msg) && writer != null){
				writer.println(msg);
				Log.e("tag","client send:"+msg);
				edit.setText("");
				String time = formatDateTime(System.currentTimeMillis());
				final String showedMsg = "self" + time + ":" + msg + "\n";
				text.setText(text.getText()+ showedMsg);
			}
		}
		
	}
	//获取实现的格式
	private String formatDateTime(long time){
		return new SimpleDateFormat("(HH:mm:ss)").format(new Date(time));
	}
	
	private void connectTPCService(){
		Socket socket = null;
		//socket为空的时候，每个1秒不断的尝试的连接
		while (socket == null) {
			try {
				//要连接的端口
				socket = new Socket("localhost",8688);
				client = socket;
				writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
				mHandler.sendEmptyMessage(MESSAGE_SOCKET_CONNECTD);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				SystemClock.sleep(1000);
				System.out.println("retry connected");
				e.printStackTrace();
			}
		}
		
		//读取从服务端传入的流
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			while (!MainActivity.this.isFinishing()) {
				String msg = br.readLine();
//				System.out.println("receive:"+msg);
				if(msg != null){
					String time = formatDateTime(System.currentTimeMillis());
					final String showedMsg = "server" + time + ":"+msg+"\n";
					mHandler.obtainMessage(MESSAGE_RECEIVE_NEW_MSG, showedMsg).sendToTarget();
				}
			}
			System.out.println("quit..");
			writer.close();
			br.close();
			socket.close();
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}



    
}
