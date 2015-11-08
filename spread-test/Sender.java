import spread.*;
import java.net.*;

public class Sender {

    public static void main(String args[]) {

	try {
		System.out.println("Starting spread member...");
		SpreadConnection connection = new SpreadConnection();
		connection.connect(InetAddress.getByName("localhost"), 0, "sender", false, true);

		System.out.println("Joining group...");
		SpreadGroup group = new SpreadGroup();
		group.join(connection, "group");

		System.out.println("Prepare message...");
		SpreadMessage message = new SpreadMessage();

		message.setData("message".getBytes());
		message.addGroup("group");
		message.setReliable();

		connection.multicast(message);

	} 
	catch (Exception e) {
      		System.out.println("Trouble!");
      		e.printStackTrace();
	}


   }
}
