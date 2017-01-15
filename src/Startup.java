import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class Startup {
	JFrame startupFrm = new JFrame();
	JPanel labelPnl = new JPanel();
	JPanel tfPnl = new JPanel();
	JPanel buttonPnl = new JPanel();
	JLabel hostLbl = new JLabel(" Host: ");
	JLabel portLbl = new JLabel(" Port: ");
	JTextField hostTf = new JTextField(20);
	JTextField portTf = new JTextField(20);
	JButton startBtn = new JButton("Start as a host");
	JButton connectBtn = new JButton("Connect to a host");
	boolean isConnected = false;
	ServerSocket ss;
	Socket s;
	ObjectInputStream is;
	ObjectOutputStream os;
	
	public void go() {
		startupFrm.setTitle("Collaborative Painter");
		startupFrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		buttonPnl.setLayout(new GridLayout(1,2,0,0));
		buttonPnl.add(startBtn);
		buttonPnl.add(connectBtn);
		labelPnl.setLayout(new GridLayout(2,1,0,0));
		labelPnl.add(hostLbl);
		labelPnl.add(portLbl);
		tfPnl.setLayout(new GridLayout(2,1,0,0));
		tfPnl.add(hostTf);
		tfPnl.add(portTf);
		startupFrm.add(labelPnl,BorderLayout.WEST);
		startupFrm.add(tfPnl,BorderLayout.CENTER);
		startupFrm.add(buttonPnl,BorderLayout.SOUTH);
		startBtn.addActionListener(new StartAction());
		connectBtn.addActionListener(new ConnectAction());
		startupFrm.setSize(280,120);
		startupFrm.setResizable(false);
		startupFrm.setVisible(true);
	}
	
	public void setupNetworking(int choice) {
		if (choice == 1) {
			try {
				ss = new ServerSocket(Integer.parseInt(portTf.getText()));
				isConnected = true;
			} catch (Exception e) {
				String msg = "Unable to listen to port " + portTf.getText() + "!";
				JOptionPane.showMessageDialog(null, msg, "Fail to start", JOptionPane.INFORMATION_MESSAGE);
			}
		} else if (choice == 2) {
			try {
				s = new Socket(hostTf.getText(),Integer.parseInt(portTf.getText()));
				os = new ObjectOutputStream(s.getOutputStream());
				is = new ObjectInputStream(s.getInputStream());
				isConnected = true;
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Unable to connect to host!", "Fail to start", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
	
	class StartAction implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			setupNetworking(1);
			if (isConnected) {
				CoPainter cp = new CoPainter(ss);
				cp.go();
				startupFrm.dispose();
			}
		}
	}
	
	class ConnectAction implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			setupNetworking(2);
			if (isConnected){
				CoPainter cp = new CoPainter(s,is,os);
				cp.go();
				startupFrm.dispose();
			}
		}
	}
}

