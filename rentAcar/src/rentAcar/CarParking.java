package rentAcar;

import java.awt.*;

public class CarParking extends Entity {

    public boolean occupied = false;
    
    public CarParking(Point point, Color color){
        super(point, color);
    }

    public void changeOccupied(){
        this.occupied = !this.occupied;
    }

    public boolean isOccupied(){
        return occupied;
    }
}
