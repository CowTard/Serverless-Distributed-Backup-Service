package serviceInterfaces;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.text.SimpleDateFormat;

import main.Main;

public class MulticastChannel {
	private InetAddress group;
	private int port;
	private MulticastSocket socket;

	public MulticastChannel(String ip, int port) throws UnknownHostException {
		group = InetAddress.getByName(ip);
		this.port = port;
	}

	public void join() throws IOException {
		socket = new MulticastSocket(port);
		socket.joinGroup(group);
	}

	public void send(byte[] toSend) throws IOException {
		DatagramPacket packet = new DatagramPacket(toSend, toSend.length, group, port);
		socket.send(packet);
		//System.out.println("Sent: " + new String(toSend));
		String logString = "Sent from " + packet.getAddress().toString() + " : one chunk!" + '\n' + "------------------";
		Main.logfile.appendLog(logString);
	}

	public byte[] receive(byte[] buf) throws IOException {
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		socket.receive(packet);
		byte[] data = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
		Date dNow = new Date();
	    SimpleDateFormat time = new SimpleDateFormat ("hh:mm:ss dd.MM.yyyy");
		String logString = "[" + time.format(dNow) + " ] | Received from " + packet.getAddress().toString() + " : One chunk!" + '\n' + "------------------";
		Main.logfile.appendLog(logString);
		return data;
	}

	public void close() throws IOException {
		socket.leaveGroup(group);
		socket.close();
	}
}
