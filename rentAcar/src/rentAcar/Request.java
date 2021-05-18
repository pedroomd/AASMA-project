package rentAcar;

import java.awt.*;

public class Request {

    private int travelDistance;
    private Point clientPoint;

    public Request(int travelDistance, Point clienPoint) {
        this.travelDistance = travelDistance;
        this.clientPoint = clienPoint;
    }
    
    public int getTravelDistance() {
    	return this.travelDistance;
    }

    public Point getClientPoint() {
    	return this.clientPoint;
    }
}
