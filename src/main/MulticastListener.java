package main;

import java.io.IOException;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;

import serviceInterfaces.MulticastChannel;

public class MulticastListener extends Thread {
	private MulticastChannel mchannel;
	private Queue<String> receivedMsgs;

	public MulticastListener(MulticastChannel MC) {
		this.mchannel = MC;
		Queue<String> receivedMsgs = new LinkedList<String>();
		this.receivedMsgs = receivedMsgs;
	}

	public void run() {
		while (!isInterrupted()) {
			try {
				byte[] received = new byte[8192];
				//System.out.println("Listener");
				mchannel.receive(received);
				receivedMsgs.add(new String(received));
				//System.out.println(new String(received));
			}
			catch (SocketException e) {
				break;
			}
			catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
	}
	
	public Queue<String> getQueue() {
		return this.receivedMsgs;
	}
}