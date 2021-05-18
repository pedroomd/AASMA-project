package rentAcar;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import rentAcar.Block.Shape;
import java.util.*;

public class Board {

	/** The environment */

	public static int nX = 15, nY = 15;

	private static Block[][] board;
	private static Entity[][] objects;
	private static int max_clients = 10;

	private static Central central;
	private static List<Car> cars;
	private static List<CarParking> carParkings;
	private static List<Client> clients;

	private static Random rand = new Random();
 
	
	/****************************
	 ***** A: SETTING BOARD *****
	 ****************************/
	
	public static void initialize() {
			

		/** A: create car parkings*/

		carParkings = new ArrayList<CarParking>();
		carParkings.add(new CarParking(new Point(4,10), Color.blue));
		carParkings.add(new CarParking(new Point(5,10), Color.blue));
		carParkings.add(new CarParking(new Point(10,4), Color.blue));
		carParkings.add(new CarParking(new Point(10,5), Color.blue));		

		/** B: create cars*/
		cars = new ArrayList<Car>();
		cars.add(new Car(new Point(2,12), Color.pink));
		cars.add(new Car(new Point(9,12), Color.pink));
		cars.add(new Car(new Point(8,7) , Color.pink));
		cars.add(new Car(new Point(13,7), Color.pink));
		cars.add(new Car(new Point(2,6) , Color.pink));
		cars.add(new Car(new Point(5,3) , Color.pink));

		clients = new ArrayList<>();

		central = new Central(carParkings, cars);

		for(Car c : cars) {
			c.setCentral(central);
		}

		objects = new Entity[nX][nY];

		/** A: create board */
		board = new Block[nX][nY];
		for(int i=0; i<nX; i++){
			for(int j=0; j<nY; j++){
				if (isOcean(i,j)){
					board[i][j] = new Block(Shape.ocean, Color.cyan);
				} 
				else {
					board[i][j] = new Block(Shape.free, new Color(144, 238, 144));
				}
			} 
		}
		
		for(CarParking c: carParkings){
			board[c.point.x][c.point.y] = new Block(Shape.carParking, c.color);
			objects[c.point.x][c.point.y]= c;
		} 
		for(Car c : cars){
			objects[c.point.x][c.point.y]=c;
		}

	}
	
	/****************************
	 ***** B: BOARD METHODS *****
	 ****************************/
	
	public static Entity getEntity(Point point) {
		return objects[point.x][point.y];
	}
	public static Block getBlock(Point point) {
		return board[point.x][point.y];
	}
	public static void updateEntityPosition(Point point, Point newpoint) {
		objects[newpoint.x][newpoint.y] = objects[point.x][point.y];
		objects[point.x][point.y] = null;
	}	
	public static void removeEntity(Point point) {
		objects[point.x][point.y] = null;
	}
	public static void insertEntity(Entity entity, Point point) {
		objects[point.x][point.y] = entity;
	}

	/***********************************
	 ***** C: ELICIT AGENT ACTIONS *****
	 ***********************************/
	
	private static RunThread runThread;
	private static GUI GUI;

	public static class RunThread extends Thread {
		
		int time;
		
		public RunThread(int time){
			this.time = time*time;
		}
		
	    public void run() {
	    	while(true){


	    		step();
				try {
					sleep(time);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    	}
	    }
	}
	
	public static void run(int time) {
		Board.runThread = new RunThread(time);
		Board.runThread.start();
	}

	public static void reset() {
		removeObjects();
		initialize();
		GUI.displayBoard();
		displayObjects();	
		GUI.update();
	}

	public static void sendMessage(Point point, Shape shape, Color color, boolean free) {
		for(Car a : cars) a.receiveMessage(point, shape, color, free);		
	}

	public static void sendMessage(Action action, Point pt) {
		for(Car a : cars) a.receiveMessage(action, pt);		
	}
	
	public static void step() {
		removeObjects();
		//for(Car a : cars) a.agentDecision();
		if(clients.size() < max_clients){
			//50% of a new client
			if(rand.nextBoolean()){
				int x = rand.nextInt(15);
				int y = rand.nextInt(15);
				if( board[x][y].shape.equals(Shape.free)){
					Request request = new Request(20, new Point(x,y));
					central.pushRequest(request);
					Client client = new Client(new Point(x,y), Color.BLACK, request);
					clients.add(client);
					objects[x][y] = client;
				}
			}
		}
		
		for(Car c : cars){
			c.getRequest();
			c.agentReactiveDecision();
		} 

		displayObjects();
		GUI.update();
	}

	public static void stop() {
		runThread.interrupt();
		runThread.stop();
	}

	public static void displayObjects(){
		for(Car c : cars) GUI.displayObject(c);
		for(CarParking c : carParkings) GUI.displayObject(c);
		for(Client c: clients) GUI.displayObject(c);
	}
	
	public static void removeObjects(){
		for(Car c : cars) GUI.removeObject(c);
		for(CarParking c : carParkings) GUI.removeObject(c);
		for(Client c : clients) GUI.removeObject(c);
	}
	
	public static void associateGUI(GUI graphicalInterface) {
		GUI = graphicalInterface;
	}

	public static boolean isOcean(int i, int j) {
		return ((j < 0 || i < 0) || (j == 0 && i >= 10) || ( j == 0 && i <= 7) || ( j == 1 && i == 4) || ( j == 1 && i == 5) ||
				( j == 1 && i == 6) || ( j == 1 && i >= 11) || ( j == 2 && i >= 13) || ( i == 14 && j <= 5) );
	}
	
	public static void removeClient(Entity entity) {
		clients.remove(entity);
		removeEntity(entity.point);
		GUI.removeObject(entity);
	}
}
