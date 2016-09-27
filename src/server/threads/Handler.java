package server.threads;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

import server.util.Common;

public class Handler implements Runnable{

	private BufferedWriter output;
	private BufferedReader reader;
	private CountDownLatch countDownLatch;
	private String result;
	private String message;
	private Socket socket;
	public Handler(Socket socket,String message,CountDownLatch countDownLatch){
		
		try {
			reader  = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			output  = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
//			output.write(message+"\n");
//			output.flush();
			this.socket=socket;
			this.message=message;
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.countDownLatch=countDownLatch;
	}
	
	public void run() {
		try {
			
			switch(Common.unmarshalling(this.message,"type")){
				case "lockindentity":
					output.write(this.message+"\n");
					output.flush();
					this.result = reader.readLine();
					break;
				case "releaseidentity":
					output.write(this.message+"\n");
					output.flush();
					break;
				case "lockroomid":
					output.write(this.message+"\n");
					output.flush();
					this.result = reader.readLine();
					break;
				case "releaseroomid":
					output.write(this.message+"\n");
					output.flush();
					break;
				case "deleteroom":
					output.write(this.message+"\n");
					output.flush();
					break;
			}
			countDownLatch.countDown();
			this.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	public String getResult() {
		if(result !=null){
			return result;
		}
		else{
			return "";
		}
		
	}
	


}
