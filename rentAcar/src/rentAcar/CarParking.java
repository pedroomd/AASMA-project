package rentAcar;

import java.awt.*;

public class CarParking {

    public boolean occupied = false;
    public Point location;
    
    public CarParking(Point location){
        this.location = location;
    }

    public void changeOccupied(){
        this.occupied = !this.occupied;
    }

    public boolean isOccupied(){
        return occupied;
    }
}
