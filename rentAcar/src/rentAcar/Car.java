package rentAcar;

import java.awt.Color;
import java.awt.Point;
import java.util.List;

import javax.swing.text.Style;

import java.util.ArrayList;

import rentAcar.Agent.Action;
import rentAcar.Block.Shape;

public class Car extends Entity {

	public enum State {charging, startRequest, occupied, nonOccupied, needCharger }
	public static int threshold = 30;
	public static int maxBattery = 100;
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
		
		if(hasDestPoint()) nextPosition();
		else if(isCharging() && !hasRequest()) charge();
		else if(lowBattery() && state.equals(State.nonOccupied)) searchCarParking();

	  	else if(distanceComplete() && client()) dropClient();
	  	else if(!isFreeCell()) rotateRandomly();
	  	else if(random.nextInt(5) == 0) rotateRandomly();
	  	else moveAhead();
  	}
    
    public boolean isFreeCell() {

		if(isBorder() || isOcean()) return false;
		else if (Board.getBlock(ahead).shape.equals(Shape.free) && (Board.getEntity(ahead) == null)){
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
			this.central.setCarkParkingOccupied(park);
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
		ahead = aheadPosition(this.point, this.direction);
	}
	
	/* Rotate agent to left */
	public void rotateLeft() {
		direction = (direction-90+360)%360;
		ahead = aheadPosition(this.point, this.direction);
	}

	public void nextPosition(){

        int dx = destPoint.x - point.x;
        int dy = destPoint.y - point.y;

        int nextX = point.x + Integer.signum(dx);
        int nextY = point.y + Integer.signum(dy);

		Point moveInX = new Point(nextX, point.y);
        Point moveInY = new Point(point.x, nextY);

        if (destPoint.equals(ahead)) {
			
            if(state.equals(State.needCharger) && isCarParking()){
				destPoint = null;
				moveAhead();
                state = State.charging;
            }
            else if(state.equals(State.startRequest) && isClient()){
				destPoint = null;
                grabClient();
                state = State.occupied;
            }
        }
        else if((moveInX.equals(ahead) || moveInY.equals(ahead)) && isFreeCell()) moveAhead();
        else {
            int rotateRight = (direction+90)%360; 
			int rotateLeft = (direction-90+360)%360;

            Point aheadRight = aheadPosition(this.point, rotateRight);
            Point aheadLeft = aheadPosition(this.point, rotateLeft);

            if(moveInX.equals(aheadRight) || moveInY.equals(aheadRight)) rotateRight();

            else if(moveInX.equals(aheadLeft) || moveInY.equals(aheadLeft)) rotateLeft();

            else {
				System.out.println("-----------------------------");
				System.out.println(point + "  " + destPoint);
                if(random.nextBoolean()) {
                    rotateRight();
                    if(isFreeCell()) moveAhead();
                }

                else {
                    rotateLeft();
                    if(isFreeCell()) moveAhead();
                }
            }
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
		client.drop();
	    client = null;
		state = State.nonOccupied;
	}

	public void searchCarParking(){

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

		if(closestCarParking != null){
			this.state = State.needCharger;
			destPoint = closestCarParking.point;
			this.park = closestCarParking;
			this.central.setCarkParkingOccupied(closestCarParking);
		}

		
	}

	public Integer manhattanDistance(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

	public void getRequest() {
		if(state.equals(State.nonOccupied)){
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
}
