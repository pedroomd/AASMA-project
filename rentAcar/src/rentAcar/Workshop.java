package rentAcar;

import java.awt.*;

public class Workshop {

    public Point location;
    private boolean occupied = false;

    public Workshop(Point location){
        this.location = location;
    }

    public void changeOccupied(){
        this.occupied = !this.occupied;
    }

    public boolean isOccupied(){
        return occupied;
    }
}
