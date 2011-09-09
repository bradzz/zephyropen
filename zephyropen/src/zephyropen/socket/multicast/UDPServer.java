package zephyropen.socket.multicast;

import java.net.*;
import java.util.*;

public class UDPServer {

	public static void showInterfaces() {
		Enumeration<NetworkInterface> interfaces = null;
		try {
			interfaces = NetworkInterface.getNetworkInterfaces();
			if (interfaces != null)
				while (interfaces.hasMoreElements()) {
					NetworkInterface ni = (NetworkInterface) interfaces.nextElement();

					System.out.print("interface name: " + ni.getName());

					if (ni.supportsMulticast())
						System.out.print(" supports muticast");
					if (ni.isVirtual())
						System.out.print(" is virtual");
					if (ni.isUp())
						System.out.print(" is up");
					if (ni.isLoopback())
						System.out.print(" is loopback");

					System.out.println("\naddress list:");

					Enumeration<InetAddress> addrs = ni.getInetAddresses();
					while (addrs.hasMoreElements()) {
						InetAddress a = (InetAddress) addrs.nextElement();

						System.out.println(a.getHostAddress());
					}
				}
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
	}

	/** get a list of ip's for this local network */ 
	public static String getLocalAddress() {
		String address = "";
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			if (interfaces != null)
				while (interfaces.hasMoreElements()) {
					NetworkInterface ni = (NetworkInterface) interfaces.nextElement();
					if (!ni.isVirtual())
						if (!ni.isLoopback())
							if (ni.isUp()) {
								Enumeration<InetAddress> addrs = ni.getInetAddresses();
								while (addrs.hasMoreElements()) {
									InetAddress a = (InetAddress) addrs.nextElement();
									address += a.getHostAddress() + " ";
								}
							}
				}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return address.trim();
	}

	public static void main(String args[]) {

		showInterfaces();
		System.out.println("found local: " + getLocalAddress());
		
		try {
			int port = 4444;
			long now;
			DatagramSocket dsocket = new DatagramSocket(port);
			byte[] buffer = new byte[2048];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

			while (true) {
				now = System.currentTimeMillis();
				dsocket.receive(packet);
				String msg = new String(buffer, 0, packet.getLength());
				System.out.println((System.currentTimeMillis() - now)
						+ " " + packet.getAddress().getHostName() + ": " + msg);

				// Reset the length of the packet before reusing it.
				packet.setLength(buffer.length);
			}
		} catch (Exception e) {
			System.err.println(e);
		}
	}
}
