package rentAcar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.*;
import java.awt.*;


public class GUI extends JFrame {

	private static final long serialVersionUID = 1L;
	
	static JTextField speed, initialThreshold;
	static JPanel boardPanel;
	static JButton run, reset, step, setInitialThreshold;
	private int nX, nY;
	
	static JLabel carsDown, satisfiedClients, unsatisfiedClients, batteryThreshold, giveAwayCarParking, meanWaitingTime;

	public class Cell extends JPanel {

		private static final long serialVersionUID = 1L;
		
		public List<Entity> entities = new ArrayList<Entity>();
		
		
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for(Entity entity : entities) {
				g.setColor(entity.color);
				//System.out.println(entity.color);
				if(entity instanceof Client) {
					g.fillOval(12, 12, 17, 17);
		            g.setColor(Color.white);
	            	g.drawOval(12, 12, 17, 17);

				}
				else{
					switch(((Car)entity).direction) {
		    			case 0:  
							g.fillPolygon(new int[]{7, 22, 37}, new int[]{37, 7, 37}, 3);
							g.setColor(Color.BLACK); 
							g.drawString(Integer.toString(((Car)entity).number), 17, 28);
							//g.setColor(Color.BLACK); 
							break;
		    			case 90: 
							g.fillPolygon(new int[]{8, 38, 8}, new int[]{7, 22, 37}, 3);
							g.setColor(Color.BLACK); 
							g.drawString(Integer.toString(((Car)entity).number), 15, 27);
							break;
		    			case 180:
							g.fillPolygon(new int[]{10, 40, 25}, new int[]{7, 7, 37}, 3);
							g.setColor(Color.BLACK); 
							g.drawString(Integer.toString(((Car)entity).number), 20, 24); 
							break;
		    			default: 
							g.fillPolygon(new int[]{8, 38, 38}, new int[]{22, 7, 37}, 3);
							g.setColor(Color.BLACK); 
							g.drawString(Integer.toString(((Car)entity).number), 22, 26);  
		    		}
				}
            }
        }
	}

	public GUI() {
		//image = new ImageIcon(getClass().getResource("charger.png"));
		setTitle("Rent-A-Car - Autonomous Solutions");		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(null);
		setSize(955, 725);
		add(createButtonPanel());
		
		
		Board.initialize();
		Board.associateGUI(this);

		add(carsDownPanel());
		add(satisfiedClientsPanel());
		add(unsatisfiedClientsPanel());
		add(batteryThresholdPanel());
		add(initialThreshold());
		add(giveAwayCarParkingPanel());
		add(meanWaitingTimePanel());
		
		boardPanel = new JPanel();
		boardPanel.setSize(new Dimension(600,600));
		boardPanel.setLocation(new Point(20,60));
		
		nX = Board.nX;
		nY = Board.nY;
		boardPanel.setLayout(new GridLayout(nX,nY));
		for(int i=0; i<nX; i++)
			for(int j=0; j<nY; j++)
				boardPanel.add(new Cell());
		
		displayBoard();
		Board.displayObjects();
		update();
		add(boardPanel);
	}

	public void displayBoard() {
		for(int i=0; i<nX; i++){
			for(int j=0; j<nY; j++){
				int row=nY-j-1, col=i;
				Block block = Board.getBlock(new Point(i,j));
				JPanel p = ((JPanel)boardPanel.getComponent(row*nX+col));
				p.setBackground(block.color);
				JLabel jlabel = new JLabel(i+"/"+j);
				jlabel.setFont(new Font("Verdana",1,5));
				p.add(jlabel);
				p.setBorder(BorderFactory.createLineBorder(Color.white));
			}
		}
	}
	
	public void removeObject(Entity object) {
		int row=nY-object.point.y-1, col=object.point.x;
		Cell p = (Cell)boardPanel.getComponent(row*nX+col);
		p.setBorder(BorderFactory.createLineBorder(Color.white));			
		p.entities.remove(object);
	}
	
	public void displayObject(Entity object) {
		int row=nY-object.point.y-1, col=object.point.x;
		Cell p = (Cell)boardPanel.getComponent(row*nX+col);
		p.setBorder(BorderFactory.createLineBorder(Color.white));			
		p.entities.add(object);
		carsDown.setText("Nr of times battery ran over: " + Board.getCarsDown());
		satisfiedClients.setText("Satisfied clients: " + Board.getSatisfiedClients());
		unsatisfiedClients.setText("Unsatisfied clients: " + Board.getUnsatisfiedClients());
		batteryThreshold.setText("Learned battery threshold: " + Board.getThreshold());
		giveAwayCarParking.setText("Nr of times parking has been given away: " + Board.getGiveAwayCarParking());
		meanWaitingTime.setText("Mean waiting time for clients: " + Board.getMeanWaitTime());
	}

	public void update() {
		boardPanel.invalidate();
	}

	private Component createButtonPanel() {
		JPanel panel = new JPanel();
		panel.setSize(new Dimension(600,50));
		panel.setLocation(new Point(0,0));
		
		step = new JButton("Step");
		panel.add(step);
		step.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(run.getText().equals("Run")) Board.step();
				else Board.stop();
			}
		});
		reset = new JButton("Reset");
		panel.add(reset);
		reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Board.reset();
			}
		});
		run = new JButton("Run");
		panel.add(run);
		run.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(run.getText().equals("Run")){
					int time = -1;
					try {
						time = Integer.valueOf(speed.getText());
					} catch(Exception e){
						JTextPane output = new JTextPane();
						output.setText("Please insert an integer value to set the time per step\nValue inserted = "+speed.getText());
						JOptionPane.showMessageDialog(null, output, "Error", JOptionPane.PLAIN_MESSAGE);
					}
					if(time>0){
						Board.run(time);
	 					run.setText("Stop");						
					}
 				} else {
					Board.stop();
 					run.setText("Run");
 				}
			}
		});
		speed = new JTextField(" time per step in [1,100] ");
		speed.setMargin(new Insets(5,5,5,5));
		panel.add(speed);
		
		return panel;
	}


	private Component initialThreshold() {
		JPanel panel = new JPanel();
		panel.setSize(new Dimension(325,50));
		panel.setLocation(new Point(637,150));

		JLabel label = new JLabel("Initial battery threshold");
		panel.add(label);
		initialThreshold = new JTextField("40");
		initialThreshold.setMargin(new Insets(5,5,5,5));
		initialThreshold.setColumns(3);
		Board.setInitialThreshold(Integer.parseInt(initialThreshold.getText()));
		panel.add(initialThreshold);

		setInitialThreshold = new JButton("Set");
		panel.add(setInitialThreshold);
		setInitialThreshold.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0){
				try{
					//System.out.println("hmm"+Integer.parseInt(initialThreshold.getText()));
					Board.setInitialThreshold(Integer.parseInt(initialThreshold.getText()));
					System.out.println("hospitals capacity randomness: " ); 
				}catch(Exception e){
					JTextPane output=new JTextPane();
					output.setText("Please insert an valid integer value in Initial battery threshold\nValue inserted = " + initialThreshold.getText());
					JOptionPane.showMessageDialog(null,output,"Error",JOptionPane.PLAIN_MESSAGE);
				}
			}
		});

		return panel;
	}


	private Component carsDownPanel() {
		carsDown = new JLabel("Nr of times battery ran over: 0");
		carsDown.setSize(new Dimension(500,50));
		carsDown.setLocation(new Point(660,260));

		return carsDown;
	}

	private Component satisfiedClientsPanel() {
		satisfiedClients = new JLabel("Satisfied clients: 0");
		satisfiedClients.setSize(new Dimension(500,50));
		satisfiedClients.setLocation(new Point(660,280));
		return satisfiedClients;
	}

	private Component unsatisfiedClientsPanel() {
		unsatisfiedClients = new JLabel("Unsatisfied clients: 0");
		unsatisfiedClients.setSize(new Dimension(500,50));
		unsatisfiedClients.setLocation(new Point(660,300));
		return unsatisfiedClients;
	}

	private Component meanWaitingTimePanel() {
		meanWaitingTime = new JLabel("Mean waiting time for clients: 0");
		meanWaitingTime.setSize(new Dimension(500,50));
		meanWaitingTime.setLocation(new Point(660,320));
		return meanWaitingTime;
	}

	private Component giveAwayCarParkingPanel() {
		giveAwayCarParking = new JLabel("Nr of times parking has been given away: 0");
		giveAwayCarParking.setSize(new Dimension(500,50));
		giveAwayCarParking.setLocation(new Point(660,340));
		return giveAwayCarParking;
	}

	private Component batteryThresholdPanel() {
		batteryThreshold = new JLabel("Learned Battery threshold: 0");
		batteryThreshold.setSize(new Dimension(500,50));
		batteryThreshold.setLocation(new Point(660,360));
		return batteryThreshold;
	}
}
