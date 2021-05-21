package rentAcar;

import java.awt.*;

public class CarParking extends Block {
	
	public Point point;
    public boolean occupied = false;
    public boolean hasArrived = false;
    
    public CarParking(Point point, Shape shape, Color color){
        super(shape, color);
        this.point = point;

    }

    public void changeOccupied(boolean bool){
        this.occupied = bool;
    }

    public boolean isOccupied(){
        return occupied;
    }
    
    public Point getPoint() {
    	return this.point;
    }
    
    public boolean getHasArrived() {
    	return this.hasArrived;
    }
    
    public void setHasArrived(boolean bool) {
    	this.hasArrived = bool;
    }
}
