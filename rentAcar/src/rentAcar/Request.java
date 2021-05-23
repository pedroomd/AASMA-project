package rentAcar;

import java.awt.*;

public class Request {

    private int travelDistance;
    private Point clientPoint;
    private int requestStep;

    public Request(int travelDistance, Point clienPoint, int requestStep) {
        this.travelDistance = travelDistance;
        this.clientPoint = clienPoint;
        this.requestStep = requestStep;
    }
    
    public int getTravelDistance() {
    	return this.travelDistance;
    }

    public Point getClientPoint() {
    	return this.clientPoint;
    }

    public int getRequestStep() {
    	return this.requestStep;
    }
}
