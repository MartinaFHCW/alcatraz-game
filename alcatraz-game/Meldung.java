import javax.swing.JOptionPane;
import javax.swing.JTextField;
 
public class Meldung{


	public static void main(String[] args){
 
		// Erstellung Array vom Datentyp Object, Hinzufügen der Komponenten		
		JTextField name = new JTextField();
		JTextField host = new JTextField();
		JTextField group = new JTextField();
		Object[] message = {"Name", name, 
        	"Spread", host,
        	"Group", group};
        
        
        if (args.length >= 3) {
			name.setText(args[0]);
			host.setText(args[1]);
			group.setText(args[2]);
		}

        JOptionPane pane = new JOptionPane( message, 
		  JOptionPane.PLAIN_MESSAGE, 
		  JOptionPane.OK_CANCEL_OPTION);
		pane.createDialog(null, "Titelmusik").setVisible(true);
 
		System.out.println("Eingabe: " + name.getText() + ", " + host.getText());
		System.exit(0);
	}
}
