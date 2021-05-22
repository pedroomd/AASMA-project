package rentAcar;

import java.awt.*;

public class CarParking extends Entity {

    public boolean occupied = false;
    
<<<<<<< HEAD
    public CarParking(Point point, Shape shape, Color color){
        super(shape, color);
        this.point = point;

=======
    public CarParking(Point point, Color color){
        super(point, color);
>>>>>>> parent of eba18df (hhh)
    }

    public void changeOccupied(){
        this.occupied = !this.occupied;
    }

    public boolean isOccupied(){
        return occupied;
    }
}
