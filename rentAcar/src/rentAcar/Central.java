package rentAcar;

import java.util.List;
import java.util.ArrayList;
import java.awt.Point;

public class Central {
    private List<CarParking> carParkings;
    private List<Request> requests;
    private List<Car> cars;

    public Central(List<CarParking> carParkings, List<Car> cars){
        this.carParkings = carParkings;
        this.cars = cars;
        this.requests = new ArrayList<>();
    }
    
    public void setCarParkings(List<CarParking> carParkings){
        this.carParkings = carParkings;
    }

    public void setCars(List<Car> cars){
    	this.cars = cars;
    }
    
    public List<Request> getRequests(){
        return this.requests;
    }

    public List<Car> getCars(){
        return this.cars;
    }
    
    public void pushRequest(Request r) {
    	this.requests.add(r);
    }
    
    public void popRequest(Request r) {
    	this.requests.remove(r);
    }

    public void setCarkParkingOccupied(CarParking park){
        int index = this.carParkings.indexOf(park);
        this.carParkings.get(index).changeOccupied();
    }

    public List<CarParking> getAvailableCarParkings(){
        List<CarParking> carParkingsAvailable = new ArrayList<>();
        for(CarParking park: carParkings){
            if(!park.isOccupied()){
                carParkingsAvailable.add(park);
            }
        }
        return carParkingsAvailable;
    }
}
