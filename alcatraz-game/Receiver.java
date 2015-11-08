import spread.*; 
import java.net.*;

public class Receiver {

  public static void main(String args[]) {
	try {

		System.out.println("Starting spread member...");
		SpreadConnection connection = new SpreadConnection();
		connection.connect(InetAddress.getByName("localhost"), 0, "receiver1", false, true);

		System.out.println("Joining group...");
		SpreadGroup group = new SpreadGroup();
		group.join(connection, "group");

		while (true) {
			SpreadMessage message = connection.receive(); 

			if(message.isRegular()) {
				String s = new String(message.getData());

			    System.out.println("New message from " + message.getSender() + ": "+ s);
			}
			else
			    System.out.println("New membership message from " + message.getMembershipInfo().getGroup());
		}
	}
	catch (Exception e) {
                System.out.println("Trouble!");
                e.printStackTrace();
        }
  }
}
