package alcatraz.client;

import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import alcatraz.RemotePlayer;
import alcatraz.server.Server;
import java.awt.event.WindowEvent;
import javax.swing.JOptionPane;

public class ClientGUI extends JFrame {

	private static final long serialVersionUID = 1L;

	private JPanel contentPane;
	private JTextField tfName;
	private JTextField tfServerAdr;
	private JTextArea outputArea;
	private JRadioButton rb2players;
	private JRadioButton rb3players;
	private JRadioButton rb4players;
	private JButton btnRegister;
	private JButton btnUnregister;
	
	public ClientGUI(final RemotePlayer p) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 786, 703);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		tfName = new JTextField();
		tfName.setBounds(10, 28, 200, 20);
		contentPane.add(tfName);
		tfName.setColumns(10);

		tfServerAdr = new JTextField();
		tfServerAdr.setBounds(10, 70, 200, 20);
		contentPane.add(tfServerAdr);
		tfServerAdr.setColumns(10);

		JLabel lblUsername = new JLabel("Username");
		lblUsername.setBounds(10, 10, 180, 14);
		contentPane.add(lblUsername);

		JLabel lblServerIpAddress = new JLabel("Server IP Address");
		lblServerIpAddress.setBounds(10, 52, 180, 14);
		contentPane.add(lblServerIpAddress);

		Label lblNumberOfPlayers = new Label("Number Of Players");
		lblNumberOfPlayers.setBounds(10, 98, 180, 22);
		contentPane.add(lblNumberOfPlayers);

		rb2players = new JRadioButton("2");
		rb2players.setBounds(10, 120, 35, 20);
		contentPane.add(rb2players);

		rb3players = new JRadioButton("3");
		rb3players.setBounds(50, 120, 35, 20);
		contentPane.add(rb3players);

		rb4players = new JRadioButton("4");
		rb4players.setBounds(90, 120, 35, 20);
		contentPane.add(rb4players);

		ButtonGroup group = new ButtonGroup();
		group.add(rb2players);
		group.add(rb3players);
		group.add(rb4players);

		btnUnregister = new JButton("unregister");
		btnUnregister.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent ae) {
				// get the backup server address
				p.setServerAdr(tfServerAdr.getText());
				
		    	// unregister player
		    	if(Client.unregisterPlayer(p) == true) {
					btnUnregister.setEnabled(false);
					btnRegister.setEnabled(true);
					getOutputArea().append("You successfully unregistered!\n");
				}
				else {
					getOutputArea().append("Could not unregister!\n");
				}
				
				
			}
		});
		btnUnregister.setEnabled(false);
		btnUnregister.setBounds(110, 158, 95, 23);
		contentPane.add(btnUnregister);

		btnRegister = new JButton("register");
		btnRegister.addActionListener(new ActionListener() {
		    //@Override
		    public void actionPerformed(ActionEvent ae) {

				if (gatherFieldInformation(p) == true) {
					
					/* publish the client object - afterwards the remoteplayer with the rmiUri gets
					 * passed to the server via register - then the server holds the list with
					 * all the remoteplayers (and the uris) and passes this list to every remoteplayer
					 */
					
					p.setRmiUri(Client.publishObject(p));
				
					if(Client.registerPlayer(p) == true) {
						setRegisterButton(false);
						setUnregisterButton(true); 
						getOutputArea().append("You successfully registered:\n");
						getOutputArea().append("Name: " + p.getName() + "\n");
						getOutputArea().append("Server: " + p.getServerAdr() + "\n");
						getOutputArea().append("Game for: " + p.getDesiredNumPlayers() + "\n\n");
						setTitle("Alcatraz - " + p.getName());
						
						if (Client.startgameInt == 1){
							setUnregisterButton(false);
						}
					}
					else {
						getOutputArea().append("Username already taken or Server not found!");
					}
				} else {
					getOutputArea().append("Please fill out all fields.\n");
				}

			}
		});

		btnRegister.setBounds(10, 158, 95, 23);
		contentPane.add(btnRegister);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 193, 200, 274);
		contentPane.add(scrollPane);

		setOutputArea(new JTextArea()); 
		getOutputArea().setEditable(false);
		scrollPane.setViewportView(getOutputArea());
	}

	// ================================================================================
	// ================================================================================
	// METHODS

	/**
	 * Checks if there is any input - does not check if the input is valid.
	 * 
	 * @author max
	 * @param p
	 * @return Returns false if there is an empty field or no radiobutton is
	 *         selected.<br>
	 *         Returns true if there is input in each field and one radiobutton
	 *         is selected
	 */
	private boolean gatherFieldInformation(RemotePlayer p) {

		boolean allClear = true;
		boolean nameClear = true;
		boolean serverAdrClear = true;
		boolean numberClear = true;

		// Player name
		if (!(tfName.getText().equals(""))) {
			p.setName(tfName.getText());
		} else {
			nameClear = false;
		}

		// Server address
		if (!(tfServerAdr.getText().equals(""))) {
			p.setServerAdr(tfServerAdr.getText());
		} else {
			serverAdrClear = false;
		}

		// Number of Players
		if (rb2players.isSelected()) {
			p.setDesiredNumPlayers(2);
		} else if (rb3players.isSelected()) {
			p.setDesiredNumPlayers(3);
		} else if (rb4players.isSelected()) {
			p.setDesiredNumPlayers(4);
		} else {
			numberClear = false;
		}

		// if something was not filled out properly
		if (nameClear == false | serverAdrClear == false | numberClear == false) {
			allClear = false;
		}

		return allClear;
	}
	
	public void setRegisterButton(boolean state) {
		btnRegister.setEnabled(state);
	}
	
	public void setUnregisterButton(boolean state) {
		btnUnregister.setEnabled(state);
	}
	
	// place the game board on the right side of the window
	public void setBoard(JPanel board) {
		board.setBounds(230, 0, 550, 700);
		contentPane.add(board);
	}

	// send a message to the output area
	public void showMessage(String s) {
		getOutputArea().append(s);
	}

	public JTextArea getOutputArea() {
		return outputArea;
	}

	public void setOutputArea(JTextArea outputArea) {
		this.outputArea = outputArea;
	}
        
        public void windowClosing(WindowEvent e) throws InterruptedException
        {
            // can do cleanup here if necessary
            this.showMessage("Hello");
            contentPane.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        }        
}
