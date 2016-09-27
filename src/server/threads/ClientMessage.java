package server.threads;

import java.io.BufferedReader;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;

import server.util.Message;

public class ClientMessage implements Runnable {

	private BufferedReader reader; 
	private BlockingQueue<Message> messageQueue;
	
	public ClientMessage(BufferedReader reader, BlockingQueue<Message> messageQueue) {
		this.reader = reader;
		this.messageQueue = messageQueue;
	}
	
	@Override
	//This thread reads messages from the client's socket input stream
	public void run() {
		
			
			System.out.println(Thread.currentThread().getName() 
					+ " - Reading messages from client connection");
			
			String clientMsg = null;
			try {
			while ((clientMsg = reader.readLine()) != null) {
			
			
				System.out.println(Thread.currentThread().getName() 
						+ " - Message from client received: " + clientMsg);
				Message msg = new Message(true, clientMsg);
				messageQueue.add(msg);
			}
			
			Message msg = new Message(false, "quit");
			messageQueue.add(msg);

		} catch (SocketException e) {
			Message msg = new Message(false, "exit");
			messageQueue.add(msg);
//			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
