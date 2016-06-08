package de.crazychicken.mazenet;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.xml.bind.UnmarshalException;

import networking.XmlInStream;
import networking.XmlOutStream;

public class Main {
	public static void main(String[] args) throws UnknownHostException, IOException, UnmarshalException {

		// default Werte
		String host = "localhost";
		//String host = "192.168.1.185";
		int port = 5123;
		
		// gönnt sich die Kommandozeilen-Argumente, damit man sich später auch zum Game-Server verbinden kann
		if (args.length == 2) {
			host = args[0];
			port = Integer.parseInt(args[1]);
		}
	  
		// Verbindung herstellen
		Socket socket = new Socket(host, port);
		Connection connection = new Connection(new XmlInStream(socket.getInputStream()), new XmlOutStream(socket.getOutputStream()));

		System.out.println("Starte...");

		// Spielen
		Client client = new Client(connection);
		client.play();
		
		// Verbindung wieder richtig schließen
		socket.close();
  }
}
