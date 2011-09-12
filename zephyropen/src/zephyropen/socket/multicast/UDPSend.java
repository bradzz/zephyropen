package zephyropen.socket.multicast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPSend {

	public static void main(String args[]) {
		writeBroadcast("testing writeBroadcast "+ UDPServer.getLocalAddress() );
	}

	public static void writeBroadcast(String msg) {
		try {

			int port = 4444;

			byte[] message = msg.getBytes();

			// Get the internet address of the specified host
			InetAddress address = InetAddress.getByName("192.168.1.76");

			// Initialize a datagram packet with data and address
			DatagramPacket packet = new DatagramPacket(message, message.length, address, port);

			// Create a datagram socket, send the packet through it, close it.
			DatagramSocket dsocket = new DatagramSocket();
			dsocket.send(packet);
			dsocket.close();
			
		} catch (Exception e) {
			System.err.println(e);
		}
	}
}
