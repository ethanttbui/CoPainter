import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.net.*;

public class CoPainter extends JFrame {
	//GUI components
	JMenuBar menuBar = new JMenuBar();
	JMenu menu = new JMenu("Action");
	JMenuItem clearMenu = new JMenuItem("Clear");
	JMenuItem saveMenu = new JMenuItem("Save");
	JMenuItem loadMenu = new JMenuItem("Load");
	JMenuItem exitMenu = new JMenuItem("Exit");
	DrawPanel dp = new DrawPanel();
	JPanel optionPnl = new JPanel();
	JPanel colorPnl = new JPanel();
	JPanel sizePnl = new JPanel();
	ColorButton blackBtn = new ColorButton(Color.BLACK);
	ColorButton redBtn = new ColorButton(Color.RED);
	ColorButton greenBtn = new ColorButton(Color.GREEN);
	ColorButton blueBtn = new ColorButton(Color.BLUE);
	ColorButton whiteBtn = new ColorButton(Color.WHITE);
	SizeButton hugeBtn = new SizeButton(25);
	SizeButton largeBtn = new SizeButton(20);
	SizeButton moderateBtn = new SizeButton(15);
	SizeButton smallBtn = new SizeButton(10);
	SizeButton tinyBtn = new SizeButton(5);
	
	//Drawing data
	ArrayList<Path> paths = new ArrayList<Path>();
	ArrayList<Point> points = new ArrayList<Point>();
	Color pathColor = new Color(0,0,0);
	int pathSize = 5;
	
	//Network info and components
	boolean isServer, hasClient = false;
	ServerSocket ss;
	Socket s;
	ObjectInputStream is;
	ObjectOutputStream os;
	ClientList clients;
	
	//Constructors for server and client
	public CoPainter(ServerSocket ss) {
		this.ss = ss;
		isServer = true;
		setTitle("Collaborative Painter - Server");
	}
	
	public CoPainter(Socket s, ObjectInputStream is, ObjectOutputStream os) {
		this.s = s;
		this.is = is;
		this.os = os;
		isServer = false;
		setTitle("Collaborative Painter - Client");
		clearMenu.setEnabled(false);
		loadMenu.setEnabled(false);
	}

	//Main and go methods
	public void go() {		
		//add menu bar
		setJMenuBar(menuBar);
		menuBar.add(menu);
		menu.add(clearMenu);
		menu.addSeparator();
		menu.add(saveMenu);
		menu.addSeparator();
		menu.add(loadMenu);
		menu.addSeparator();
		menu.add(exitMenu);
		
		clearMenu.addActionListener(new ClearAction());
		saveMenu.addActionListener(new SaveAction());
		loadMenu.addActionListener(new LoadAction());
		exitMenu.addActionListener(new ExitAction());
		
		//add color picker and pen size picker
		optionPnl.setLayout(new GridLayout(1,2,0,0));
		optionPnl.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		colorPnl.setLayout(new FlowLayout(FlowLayout.LEFT));
		sizePnl.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		colorPnl.add(blackBtn);
		colorPnl.add(redBtn);
		colorPnl.add(greenBtn);
		colorPnl.add(blueBtn);
		colorPnl.add(whiteBtn);
		
		sizePnl.add(smallBtn);
		sizePnl.add(tinyBtn);
		sizePnl.add(smallBtn);
		sizePnl.add(moderateBtn);
		sizePnl.add(largeBtn);
		sizePnl.add(hugeBtn);
		
		optionPnl.add(colorPnl);
		optionPnl.add(sizePnl);
		
		blackBtn.addActionListener(blackBtn);
		redBtn.addActionListener(redBtn);
		greenBtn.addActionListener(greenBtn);
		blueBtn.addActionListener(blueBtn);
		whiteBtn.addActionListener(whiteBtn);
		
		hugeBtn.addActionListener(hugeBtn);
		largeBtn.addActionListener(largeBtn);
		moderateBtn.addActionListener(moderateBtn);
		smallBtn.addActionListener(smallBtn);
		tinyBtn.addActionListener(tinyBtn);
		
		add(optionPnl,BorderLayout.SOUTH);
		
		//add drawing panel
		paths.add(new Path(new Color(0,0,0),5));
		add(dp);
		dp.addMouseListener(dp);
		dp.addMouseMotionListener(dp);
		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setSize(500,500);
		setVisible(true);
		
		//actions for server / client
		if (!isServer) {
			Thread clientThd = new Thread(new ClientReader());
			clientThd.start();
		} else {
			Thread serverThd = new Thread(new ServerListener());
			serverThd.start();
		}
	}
	
	public static void main(String[] args) {
		Startup startup = new Startup();
		startup.go();	
	}
	
	//Network actions	
	class ClientReader implements Runnable {
		public void run() {
			while (true)
				try {
					Object obj = is.readObject();
					if ( obj == null) {
						JOptionPane.showMessageDialog(null, "Host is gone!", "Connection dropped", JOptionPane.INFORMATION_MESSAGE);
						System.exit(0);
					} else {
						paths = (ArrayList<Path>) obj;
						paths.add(new Path(pathColor,pathSize));
						dp.repaint();
					}
				} 
				catch (Exception e) {e.printStackTrace();}
		}
	}
	
	class ServerReader implements Runnable {
		ObjectInputStream ois;
		ObjectOutputStream oos;
		
		public ServerReader (Socket client, ObjectOutputStream oos) {
			try {
			this.ois = new ObjectInputStream(client.getInputStream());
			this.oos = oos;
			} catch (Exception e) {e.printStackTrace();}
		}
		
		public void run() {
			while (true)
				try {
					Object obj = ois.readObject();
					if (obj == null) {
						clients.remove(oos);
						return;
					} else {
						paths = (ArrayList<Path>)obj;
						paths.add(new Path(pathColor,pathSize));
						dp.repaint();
						ServerSender();
					}
				} 
				catch (Exception e) {e.printStackTrace();}
		}
	}
	
	class ServerListener implements Runnable {
		public void run() {
			clients = new ClientList();
			try {
				while (true) {
					s = ss.accept();
					hasClient = true;
					os = new ObjectOutputStream(s.getOutputStream());
					os.reset();
					os.writeObject(paths);
					os.flush();
					clients.add(os);
					Thread clientReader = new Thread (new ServerReader(s,os));
					clientReader.start();
				}
			} catch (Exception e) {e.printStackTrace();}
		}
	}
	
	public void ServerSender() {
		try {
			for (ObjectOutputStream cos : clients) {
				cos.reset();
				cos.writeObject(paths);
				cos.flush();
			}
		} catch (IOException e) {e.printStackTrace();}
	}
	
	public void ClientSender () {
		try {
			os.reset();
			os.writeObject(paths);
			os.flush();
		} catch (IOException e) {e.printStackTrace();}
	}

	//Menu bar actions
	class ClearAction implements ActionListener {
    	public void actionPerformed(ActionEvent event) {
    		paths.clear();
    		paths.add(new Path(pathColor,pathSize));
    		dp.repaint();
    		if (isServer) {
    			ServerSender();
    		}
    	}
	}
	
	class SaveAction implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			JFileChooser chooser = new JFileChooser();
			int returnVal = chooser.showSaveDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					FileOutputStream fos = new FileOutputStream(chooser.getSelectedFile());
					ObjectOutputStream oos = new ObjectOutputStream(fos);
					oos.writeObject(paths);
					oos.close();
				} catch (IOException e) {e.printStackTrace();}
			}	
		}
	}
	
	class LoadAction implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			JFileChooser chooser = new JFileChooser();
			int returnVal = chooser.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {		
				try {
					FileInputStream fis = new FileInputStream(chooser.getSelectedFile());
					ObjectInputStream ois = new ObjectInputStream(fis);
					paths = (ArrayList<Path>) ois.readObject();
					dp.repaint();
					ServerSender();
					ois.close();
				}
				catch (IOException e) {e.printStackTrace();}
				catch (ClassNotFoundException e) {e.printStackTrace();}
			}	
		}
	}
	
	class ExitAction implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if (!isServer){
				paths = null;
				ClientSender();
			} else {
				paths = null;
				ServerSender();
			}
			System.exit(0);
		}
	}
	
	//Button actions
	class ColorButton extends JButton implements ActionListener {
		Color color;
		
		ColorButton (Color color) {
			this.color = color;
			setPreferredSize(new Dimension(40,40));
			setBackground(color);
		}
		
		public void actionPerformed(ActionEvent event) {
			paths.get(paths.size()-1).color = color;
			pathColor = color;
		}
	}
	
	class SizeButton extends JButton implements ActionListener {
		int size;		
		SizeButton (int size) {
			this.size = size;
			setIcon(new CircleIcon(size));
			setPreferredSize(new Dimension(40,40));
			setBackground(Color.WHITE);
		};
		
		public void actionPerformed(ActionEvent event) {
			paths.get(paths.size()-1).size = this.size;
			pathSize = this.size;
		}
	} 

	//Drawing
	class DrawPanel extends JPanel implements MouseListener, MouseMotionListener {	
		public void paintComponent(Graphics g) {
    		g.setColor(Color.WHITE);
    		g.fillRect(0, 0, getWidth(), getHeight());

    		for (Path pth : paths) {
    			Point prevPoint = null;
    			g.setColor(pth.color);
    			if(g instanceof Graphics2D) {
        			Graphics2D g2D = (Graphics2D) g;
        			g2D.setStroke(new BasicStroke(pth.size, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        		}
    			for (Point pnt: pth.points) {
    				if (prevPoint != null) {
    					g.drawLine(prevPoint.x, prevPoint.y, pnt.x, pnt.y);
    				} else 
    					g.drawLine(pnt.x, pnt.y, pnt.x, pnt.y);
    				prevPoint = pnt;
    			}
    		}
    	}
    	
    	public void mouseDragged(MouseEvent event) {
    		paths.get(paths.size()-1).points.add(event.getPoint());
    		repaint();
    	}
    	
    	public void mousePressed(MouseEvent event) {
    		paths.get(paths.size()-1).points.add(event.getPoint());
    		repaint();
    	}
    	
    	public void mouseReleased(MouseEvent event) {
    		if (isServer && hasClient )
    			ServerSender();
    		if (!isServer)
    			ClientSender();
    		paths.add(new Path(pathColor,pathSize));
    	}
    	
    	//overriding
    	public void mouseMoved(MouseEvent event) {}
    	public void mouseClicked(MouseEvent event) {}
    	public void mouseEntered(MouseEvent event) {}
    	public void mouseExited(MouseEvent event) {}
    }
}
