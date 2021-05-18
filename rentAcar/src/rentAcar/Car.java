package rentAcar;

import java.awt.Color;
import java.awt.Point;
import java.util.List;
import java.util.ArrayList;

import rentAcar.Agent.Action;
import rentAcar.Block.Shape;

public class Car extends Entity {

	public enum State {charging, startRequest, occupied, nonOccupied, needCharger }
	public static int threshold = 30;
	public static int maxBattery = 150;
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

    public Car(Point point, Color color) {
		super(point, color);
	}

	public void setCentral(Central central) {
    	this.central= central;
    }

	public int getBattery(){
		return this.battery;
	}
	
    
    public void agentReactiveDecision() {
	  	ahead = aheadPosition(this.point, this.direction);
	  	if(isBorder() || isOcean()) rotateRandomly();
		else if(hasDestPoint()) nextPosition();
		else if(isCharging() && !hasRequest()) charge();

		else if(lowBattery() && !client()) searchCarParking();
	  	else if(isClient() && !client()) grabClient();
	  	else if(distanceComplete() && client()) dropClient();
	  	else if(!isFreeCell()) rotateRandomly();
	  	else if(random.nextInt(5) == 0) rotateRandomly();
	  	else moveAhead();
  	}
    
    public boolean isFreeCell() {

		if(isOcean() || isBorder()) return false;
		else if (Board.getBlock(ahead).shape.equals(Shape.free) && (Board.getEntity(ahead) == null || isClient())){
			return true;
		}
		else if(Board.getBlock(ahead).shape.equals(Shape.carParking) && isCarParking()){
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
		Entity entity = Board.getEntity(ahead);
		return entity!=null && entity instanceof CarParking; 
	}

	public boolean hasRequest(){
		return request != null;
	}
	public boolean hasDestPoint(){
		return destPoint != null;
	}

	public boolean lowBattery() {
		return battery <= threshold;
	}
    
    public boolean client() {
		return client != null;
	}
    
    public boolean distanceComplete() {
		return distanceLeft == 0;
	}

	public void charge(){
		if(this.battery == maxBattery){
			this.state = State.nonOccupied;
			park.changeOccupied();
			park = null;
		} 
		else this.battery += 10;
		
	}

    
    /* Rotate agent to right */
	public void rotateRandomly() {
		if(random.nextBoolean()) rotateLeft();
		else rotateRight();
	}
	
	/* Rotate agent to right */
	public void rotateRight() {
		direction = (direction+90)%360;
	}
	
	/* Rotate agent to left */
	public void rotateLeft() {
		direction = (direction-90+360)%360;
	}

	public void nextPosition(){

		int dx = destPoint.x - point.x;
		int dy = destPoint.y - point.y;

		//goal achieved
		if(dx == 0 && dy == 0){
			destPoint = null;
			if(state.equals(State.needCharger)){
				state = State.charging;
				park = (CarParking) Board.getEntity(this.point);
				park.changeOccupied();
			}
			else if(state.equals(State.startRequest)){
				state = State.occupied;
			}

		}

		int nextX = point.x + Integer.signum(dx);
		int nextY = point.y + Integer.signum(dy);

		Point moveInX = new Point(nextX, point.y);
		Point moveInY = new Point(point.x, nextY);

		if ((moveInX.equals(ahead) || moveInY.equals(ahead)) && isFreeCell()) moveAhead();
		else {
			int rotateRight = (direction+90)%360; 

			Point aheadRight = aheadPosition(this.point, rotateRight);

			if(moveInX.equals(aheadRight) || moveInY.equals(aheadRight)) rotateRight();
			else rotateLeft();
		} 
	}
	
	/* Move agent forward */
	public void moveAhead() {
		
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
		client.drop(ahead);
	    client = null;
	}

	public void searchCarParking(){
		this.state = State.needCharger;

		List<CarParking> carParkingsAvailable =  central.getAvailableCarParkings();
		CarParking closestCarParking = null;
		int minDistance = Integer.MAX_VALUE;
		
		//calculate the closest car park
		for(CarParking park: carParkingsAvailable){
			int distance = manhattanDistance(this.point, park.point);
			if(distance < minDistance){
				minDistance = distance;
				closestCarParking = park;
			}
		}
		destPoint = closestCarParking.point;

		
	}

	public Integer manhattanDistance(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

	public void getRequest() {
		List<Request> requestsAvailable =  central.getRequests();
		List<Car> cars = central.getCars();
		boolean closestCar = true;
		
		for(Request r : requestsAvailable) {
			int minDistance = manhattanDistance(this.point, r.getClientPoint());
			
			if((battery - r.getTravelDistance() - minDistance) > threshold) {
				
				for(Car c : cars) {
					int distance = manhattanDistance(c.point, r.getClientPoint());
					if(distance < minDistance && (c.getBattery() - r.getTravelDistance() - distance) > threshold) {
						closestCar = false;
						break;
					}
				}
				
				if(closestCar) {
					this.state = State.startRequest;
					request = r;
					central.popRequest(r);
					destPoint = r.getClientPoint();
					break;
				}
			}
		}
	}
}
