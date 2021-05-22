package rentAcar;

import java.awt.Color;
import java.awt.Point;
import java.util.List;

import rentAcar.Block.Shape;

public class Car extends Entity {


	//learning variable
	public enum Result { succeed, requestFailure, clientFailure}
	public int batteryWasted;
	public double learningRate = 0.1;
	public boolean parkAchieved = false;
	public int batteryToPark = 0;
	public double epsilon;
	public boolean learning = false;

	//
	public enum State {charging, startRequest, occupied, nonOccupied, needCharger, noBattery }
	public long threshold = 20;
	public static int maxBattery = 200;
	public int number;
	public State state = State.nonOccupied;
	public int direction = 90;
	public int battery = maxBattery;
	public Request request = null;
	public int distanceLeft;
	public Central central;
	public Client client;
	private Point ahead;
	public CarParking park = null;
	public Point destPoint = null;
	public Workshop workshop = null;
	public int requestsNotSucceed = 0;
	public int stuck = 0;
	

    public Car(Point point, Color color, int number) {
		super(point, color);
		this.number = number;
	}

	public void setCentral(Central central) {
    	this.central= central;
    }

	public int getBattery(){
		return this.battery;
	}

	public void setThreshold(long threshold){
		this.threshold = threshold;
	}
	
    
    public void agentReactiveDecision() {
    	
	  	ahead = aheadPosition(this.point, this.direction);
	  	changeCarColor();
		System.out.println(this.threshold);
		if(isCharging()) charge();
		else if(hasDestPoint()) nextPosition();
		else if((lowBattery() || this.learning) && this.state == State.nonOccupied) searchCarParking();
	  	else if(distanceComplete() && this.state == State.occupied) dropClient();
		else if(noBattery()) goToWorkshop();
	  	else if(!isFreeCell()) rotateRandomly();
	  	else if(random.nextInt(10) == 0) rotateRandomly();
	  	else moveAhead();
  	}
    
    public void changeCarColor(){
        if(battery == 5) this.color = Color.RED;
        else if (battery >= threshold) this.color = Color.pink;
        else if(battery <= threshold)this.color = color.yellow;
    }
    
    public boolean isFreeCell() {

		if(isBorder() || isOcean()) return false;
		else if (Board.getBlock(ahead).shape.equals(Shape.free) && Board.getEntity(ahead) == null){
			return true;
		}
		else return false;
  	}
    

    public boolean isOcean() {
  	  	Block block = Board.getBlock(ahead);
  	  	return block.shape.equals(Shape.ocean);
  	}
    
    public boolean isBorder() {
    	return ahead.x<0 || ahead.y<0 || ahead.x>=Board.nX || ahead.y>=Board.nY;
    }
    
    public boolean isClient() {
    	Entity entity = Board.getEntity(ahead);
		return entity!=null && entity instanceof Client; 
    }

	public boolean isCharging(){
		return state.equals(State.charging);
	}

	public boolean isCarParking() {
		Block block = Board.getBlock(ahead);
		return block.shape.equals(Shape.carParking); 
	}

	public boolean hasRequest(){
		return request != null;
	}
	public boolean hasDestPoint(){
		return destPoint != null;
	}

	public boolean hasWorkshop(){
		return workshop != null;
	}

	public boolean lowBattery() {
		return battery <= threshold;
	}

	public boolean noBattery(){
		return battery <= 0;
	}
    
    public boolean client() {
		return client != null;
	}
    
    public boolean distanceComplete() {
		return distanceLeft == 0;
	}

	public void charge(){
		
		if(this.battery >= threshold && this.central.getCarParking(this.park).isOccupied()) {
			this.central.setCarParkingOccupied(park, false);
		}
		
		if(hasRequest()) {
			this.state = State.startRequest;
			park = null;
		}
		
		else if(this.battery >= maxBattery || central.getCarParking(this.park).getHasArrived()){
			this.state = State.nonOccupied;
			park = null;
			this.color = Color.pink;
			if(this.battery >= maxBattery) this.battery = maxBattery;
			
		} 

		else this.battery += 20;
		
	}

    
    /* Rotate agent to right */
	public void rotateRandomly() {
		int rand = random.nextInt(3);
		if(rand == 0) rotateLeft();
		else if(rand == 1) rotateRight();
		else if(rand == 2) rotateBehind();
	}
	
	/* Rotate agent to right */
	public void rotateRight() {
		direction = (direction+90)%360;
		ahead = aheadPosition(this.point, this.direction);
	}
	
	/* Rotate agent to left */
	public void rotateLeft() {
		direction = (direction-90+360)%360;
		ahead = aheadPosition(this.point, this.direction);
	}
	
	public void rotateBehind() {
		direction = (direction + 180)%360;
		ahead = aheadPosition(this.point, this.direction);
	}
	
	
	private void completeDest() {
		if(this.state.equals(State.needCharger) && isCarParking()){
			if(Board.getEntity(ahead) == null) {
				destPoint = null;
				moveAhead();
				this.state = State.charging;
				this.central.setCarParkingHasArrived(this.park, false);
				if(batteryToPark > 0 && this.learning){
					this.parkAchieved = true;
					learn();
				} 
				
			}
			
			else {
				this.central.setCarParkingHasArrived(this.park, true);
			}
		}
		else if(this.state.equals(State.startRequest) && isClient()){
			destPoint = null;
			grabClient();
			state = State.occupied;
		}
	}
	

	public void goToWorkshop(){
		//System.out.println(number + " workshop");
		this.state = State.noBattery;
		
		if(hasRequest() && client()){
			dropClient();
			requestsNotSucceed++;
			if(this.learning)learn();
		}
		else if(hasRequest()){
			this.central.pushPriorityRequest(this.request);
			this.request = null;
			if(this.learning)learn();
		}
		else if(this.park != null){
			this.central.setCarParkingOccupied(this.park, false);
			this.central.setCarParkingHasArrived(this.park, false);
			this.park = null;
			if(batteryToPark > 0 && this.learning){
				this.parkAchieved = false;
				learn();
			} 
		}
		this.destPoint = null;
					
		if(this.central.isWorkShopFree()){
			this.central.changeWorkshopOccupied();
			workshop = this.central.getWorkshop();
			Board.updateEntityPosition(point, workshop.location);
			point = workshop.location;
			this.state = State.nonOccupied;
			this.battery = maxBattery;
			this.color = color.pink;
			this.direction = random.nextBoolean() ? 180 : 270;
		}
	}

	public void nextPosition(){
		if(noBattery()){
			goToWorkshop();
			return;
		}

        int dx = destPoint.x - point.x;
        int dy = destPoint.y - point.y;

		int nextX = point.x + Integer.signum(dx);
		int nextY = point.y + Integer.signum(dy);

		Point moveInX = new Point(nextX, point.y);
		Point moveInY = new Point(point.x, nextY);
		
		int rotateRight = (direction+90)%360; 
		int rotateLeft = (direction-90+360)%360;
		
		Point aheadRight = aheadPosition(this.point, rotateRight);
		Point aheadLeft = aheadPosition(this.point, rotateLeft);
		Point aheadBehind = aheadPosition(aheadLeft, rotateLeft);
		
		
		if (destPoint.equals(ahead)) completeDest();
		
		else if (destPoint.equals(aheadRight)) {
			rotateRight();
			completeDest();
		}
		
		else if (destPoint.equals(aheadLeft)) {
			rotateLeft();
			completeDest();
		}
		
		else if (destPoint.equals(aheadBehind)) {
			rotateBehind();
			completeDest();
		}
		
		else if ((moveInX.equals(ahead) || moveInY.equals(ahead)) && isFreeCell()) moveAhead();
		
		else {
			if(moveInX.equals(aheadRight) || moveInY.equals(aheadRight)) {
				rotateRight();
				if(isFreeCell()) moveAhead();
				else {
					int i = 0;
					while(!isFreeCell() && i < 5) {
						rotateRandomly();
						i += 1;
					}
					if(isFreeCell()) moveAhead();
				}
			}
			
			else if(moveInX.equals(aheadLeft) || moveInY.equals(aheadLeft)) {	
				rotateLeft();
				if(isFreeCell()) moveAhead();
				else {
					int i = 0;
					while(!isFreeCell() && i < 5) {
						rotateRandomly();
						i += 1;
					}
					if(isFreeCell()) moveAhead();
				}
			}
			
			else {
				int i = 0;
				while(!isFreeCell() && i < 5) {
					rotateRandomly();
					i += 1;
				}
				if(isFreeCell()) moveAhead();
			}
		}
	}
	
	/* Move agent forward */
	public void moveAhead() {

		if(hasWorkshop()){
			this.workshop = null;
			this.central.changeWorkshopOccupied();
		}	
		Board.updateEntityPosition(point,ahead);
		if(client()) {
			client.move(ahead); 
			distanceLeft -= 5;
		}
		point = ahead;
		battery -= 5;
	}
	
	/* Position ahead */
	private Point aheadPosition(Point initial, int direction) {
		Point newpoint = new Point(initial.x,initial.y);
		switch(direction) {
			case 0: newpoint.y++; break;
			case 90: newpoint.x++; break;
			case 180: newpoint.y--; break;
			default: newpoint.x--; 
		}
		return newpoint;
	}
	
	public void grabClient() {
		client = (Client) Board.getEntity(ahead);
		client.grab(point);
		distanceLeft = client.getRequest().getTravelDistance();
	}
	
	public void dropClient() {
		//System.out.println(number + " dropclient");
		this.client.drop();
	    this.client = null;
	    this.request = null;
	    this.state = State.nonOccupied;
		
	}

	public void searchCarParking(){
		//System.out.println(number + " searchcarparking");
		List<CarParking> carParkingsAvailable =  central.getAvailableCarParkings();
		CarParking closestCarParking = null;
		int minDistance = Integer.MAX_VALUE;
		
		//calculate the closest car park
		for(CarParking park: carParkingsAvailable) {
			int distance = manhattanDistance(this.point, park.point);
			if(distance < minDistance && (this.battery - minDistance) > threshold){
				minDistance = distance;
				closestCarParking = park;
			}
		}
		
		if(closestCarParking != null) {
			this.state = State.needCharger;
			destPoint = closestCarParking.point;
			this.park = closestCarParking;
			this.central.setCarParkingOccupied(closestCarParking, true);
			this.batteryToPark = this.battery;
		}
		else{
			minDistance = Integer.MAX_VALUE;
			List<CarParking> carParkings = this.central.getCarParkings();
			for(CarParking park: carParkings) {
				int distance = manhattanDistance(this.point, park.point);
				if(distance < minDistance){
					minDistance = distance;
					closestCarParking = park;
				}
			}
			this.state = State.needCharger;
			destPoint = closestCarParking.point;
			this.park = closestCarParking;
			this.batteryToPark = this.battery;

		}

		
	}

	public Integer manhattanDistance(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

	public void getRequest() {
		List<Request> requestsAvailable =  central.getRequests();
		List<Car> cars = central.getCars();
		boolean closestCar = true;
		
		if(this.state == State.charging || this.state == State.nonOccupied) {
			for(Request r : requestsAvailable) {
				int minDistance = manhattanDistance(this.point, r.getClientPoint());
				epsilon = Math.random();
				if((battery - r.getTravelDistance() - minDistance) > threshold || epsilon >= 0.8) {
					
					for(Car c : cars) {
						int distance = manhattanDistance(c.point, r.getClientPoint());
						if(distance < minDistance && (c.getBattery() - r.getTravelDistance() - distance) > threshold) {
							closestCar = false;
							break;
						}
					}
					
					if(closestCar) {
						if(this.state != State.charging) this.state = State.startRequest;
						request = r;
						central.popRequest(r);
						destPoint = r.getClientPoint();
						if(!((battery - r.getTravelDistance() - minDistance) > threshold)){
							this.learning = true;
							System.out.println(number + " :VAI APRENDER CARALHO!!!");
						} 
						break;
					}
				}
			}
		}
	}

	// =======   Learning functions =====================

	public long reward(){
		if(this.parkAchieved){
			if(this.batteryToPark < this.threshold){
				return this.batteryToPark - this.threshold;
			}
			else return 0;
		}
		else{
			if(this.batteryToPark > this.threshold){
				System.out.println("FILHOS DA PUTAAAAAAAAAAAA");
				return this.batteryToPark - this.threshold;
			}
			else if (batteryToPark == 0) return 10;
			else return 0;
		}

	}

	public void learn(){
		long u = reward();
		System.out.println(number + " reward: " + u);
		int carsNumber = this.central.numberOfcars();
		double learned = u * learningRate;
		threshold = Math.round((threshold + learned + (carsNumber - 1)*threshold) / carsNumber);
		sendThreshold();
		this.learning = false;
		this.batteryToPark = 0;
		this.parkAchieved = false;
	}

	public void sendThreshold(){
		List<Car> cars = this.central.getCars();
		for(Car car : cars){
			if(!car.equals(this)) car.setThreshold(this.threshold);
		}
	}

}
