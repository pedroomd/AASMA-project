package rentAcar;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import rentAcar.Block.Shape;
import java.util.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;

public class Board {

	/** The environment */

	public static int nX = 15, nY = 15;

	private static Block[][] board;
	private static Entity[][] objects;
	private static int max_clients = 4;

	private static Central central;
	private static List<Car> cars;
	private static List<CarParking> carParkings;
	private static List<Client> clients;
	private static Workshop workshop;
	private static int stepCounter = 1;
	private static int initialThreshold = -1;
	public enum CarsBehavior {Conservative, Risky}
	private static CarsBehavior carsBehavior;


	//statistic variables
	private static FileWriter csvWriter;
	private static String CURRENTDIRECTORY = System.getProperty("/Users/pedromd/Desktop/AASMA-project");
    private static File LOGSDIRECTORY = new File(CURRENTDIRECTORY, "logs");
	private static int lastCarsDown;
	private static int lastSatisfiedClients;
	private static int lastUnsatisfiedClients;
	private static double lastMeanWaitTime;

	private static Random rand = new Random();
 
	
	/****************************
	 ***** A: SETTING BOARD *****
	 ****************************/
	
	public static void initialize() {
			

		/** A: create car parkings*/

		carParkings = new ArrayList<CarParking>();
		carParkings.add(new CarParking(new Point(4,10), Shape.carParking, Color.blue));
		carParkings.add(new CarParking(new Point(5,10), Shape.carParking, Color.blue));
		carParkings.add(new CarParking(new Point(10,4), Shape.carParking, Color.blue));
		carParkings.add(new CarParking(new Point(10,5), Shape.carParking, Color.blue));		


		/** B: create cars*/
		cars = new ArrayList<Car>();
		cars.add(new Car(new Point(2,12), Color.pink,1));
		cars.add(new Car(new Point(9,12), Color.pink,2));
		cars.add(new Car(new Point(8,7) , Color.pink,3));
		cars.add(new Car(new Point(13,7), Color.pink,4));
		cars.add(new Car(new Point(2,6) , Color.pink,5));
		cars.add(new Car(new Point(5,3) , Color.pink,6));

		/**C: create workshop */

		workshop = new Workshop(new Point(14,14));

		clients = new ArrayList<>();

		central = new Central(carParkings, cars, workshop);

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
		} 
		for(Car c : cars){
			objects[c.point.x][c.point.y]=c;
		}
		//workshop
		board[workshop.location.x][workshop.location.y] = new Block(Shape.workshop, Color.gray);

		if (!LOGSDIRECTORY.exists()){
            LOGSDIRECTORY.mkdir();
		}
		//creating csv
		try{
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
			String filename = timeStamp + ".csv";

			csvWriter = new FileWriter(LOGSDIRECTORY + "/" + filename, true);
			csvWriter.append("Step");
			csvWriter.append(";");
			csvWriter.append("Nr of cars that battery ran out");
			csvWriter.append(";");
			csvWriter.append("Satisfied clients");
			csvWriter.append(";");
			csvWriter.append("Unsatisfied clients");
			csvWriter.append(";");
			csvWriter.append("Learned battery threshold");
			csvWriter.append(";");
			csvWriter.append("Nr of times parking has been given up");
			csvWriter.append(";");
			csvWriter.append("Mean waiting time for clients");
			csvWriter.append("\n");

		} catch(Exception e){
			e.printStackTrace();
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
		stepCounter = 0;
		initialThreshold = -1;
	}
	
	
	public static void step() {
		removeObjects();
		if(clients.size() < max_clients){
			//50% of a new client
			if(rand.nextBoolean()){
				int x = rand.nextInt(15);
				int y = rand.nextInt(15);
				Point pointRandom = new Point(x,y);
				int travelDistance = (rand.nextInt(5) + 1) * 10;
				if( board[x][y].shape.equals(Shape.free) && getEntity(pointRandom) == null){
					Request request = new Request(travelDistance, pointRandom, stepCounter);
					central.pushRequest(request);
					Client client = new Client(pointRandom, Color.BLACK, request);
					clients.add(client);
					objects[x][y] = client;
				}
			}
		}
		
		
		for(Car c : cars){
			c.getRequest();
			c.agentDecision();
		}

		displayObjects();
		GUI.update();
	
		

		if(stepCounter % 100 == 1) logData();
		if(stepCounter == 50001) stop();
		
		stepCounter++;
	}

	public static void logData(){

		List<List<String>> dataLines = new ArrayList<>();
		dataLines.add(Arrays.asList(
				String.valueOf(stepCounter - 1),
				String.valueOf(getCarsDown()),
				String.valueOf(getSatisfiedClients()),
				String.valueOf(getUnsatisfiedClients()),
				String.valueOf(getThreshold()),
				String.valueOf(getGiveAwayCarParking()),
				String.format("%.2f", getMeanWaitTime())));


		try {
			for (List<String> rowData : dataLines) {
				csvWriter.append(String.join(";",  rowData));
				csvWriter.append("\n");
			}

			csvWriter.flush();

		} catch(IOException e) {
			e.printStackTrace();
		}
		/*
		lastCarsDown = getCarsDown();
		lastSatisfiedClients = getSatisfiedClients();
		lastUnsatisfiedClients = getUnsatisfiedClients();*/
	}


	public static void stop() {
		runThread.interrupt();
		runThread.stop();
	}

	public static void displayObjects(){
		for(Car c : cars) GUI.displayObject(c);
		for(Client c: clients) GUI.displayObject(c);


	}
	
	public static void removeObjects(){
		for(Car c : cars) GUI.removeObject(c);
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
		//GUI.removeObject(entity);
	}

	public static int getStepCounter(){
		return stepCounter;
	}

	public static void setInitialThreshold(int threshold){
		
		for(Car car: cars) car.setThreshold(threshold);
		initialThreshold = threshold;

	}

	public static int getInitialThreshold(){
		return initialThreshold;
	}

	public static int getCarsDown(){
		return central.getCarsDown();
	}

	public static int getSatisfiedClients(){
		return central.getSatisfiedClients();
	}

	public static int getUnsatisfiedClients(){
		return central.getUnsatisfiedClients();
	}

	public static long getThreshold(){
		return cars.get(0).threshold;
	}

	public static int getGiveAwayCarParking(){
		return central.getGiveAwayCarParking();
	}

	public static double getMeanWaitTime(){
		return central.getMeanWaitTime();
	}

	public static void removeObject(Entity entity){
		GUI.removeObject(entity);
	}

	public static void displayObject(Entity entity){
		GUI.displayObject(entity);
	}

	public static void setCarsBehavior(CarsBehavior behavior){
		if(behavior.equals(CarsBehavior.Conservative)){
			for(Car car: cars) car.setThreshold(100); 
			central.setEpsilon(-1);
			carsBehavior = CarsBehavior.Conservative;
		}
		else if(behavior.equals(CarsBehavior.Risky)){
			for(Car car: cars) car.setThreshold(initialThreshold);
			central.setEpsilon(0.9);
			carsBehavior = CarsBehavior.Risky;
		}
	}

}
