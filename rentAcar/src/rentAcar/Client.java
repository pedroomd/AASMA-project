package rentAcar;

import java.awt.Color;
import java.awt.Point;

public class Client extends Entity {

	private Request request;

	public Client(Point point, Color color, Request request) {
		super(point, color);
		this.request = request;
	}
	
	/*****************************
	 ***** AUXILIARY METHODS ***** 
	 *****************************/

	public synchronized void grab(Point newpoint) {
		Board.removeEntity(point);
		point = newpoint;
	}
	
	public synchronized void drop() {
		//Board.removeEntity(point);
		Board.removeClient(this);
	}

	public void move(Point newpoint) {
		point = newpoint;
	}
	
	public Request getRequest() {
		return this.request;
	}
}
