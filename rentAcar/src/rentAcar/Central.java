package rentAcar;

import java.util.List;
import java.awt.*;
import java.util.ArrayList;

public class Central {
    private List<CarParking> carParkings;
    private List<Request> requests;
    private List<Car> cars;
    private Workshop workshop;

    public Central(List<CarParking> carParkings, List<Car> cars, Workshop workshop){
        this.carParkings = carParkings;
        this.cars = cars;
        this.requests = new ArrayList<>();
        this.workshop = workshop;
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

    public Workshop getWorkshop(){
        return this.workshop;
    }
    
    public void pushRequest(Request r) {
    	this.requests.add(r);
    }

    public void pushPriorityRequest(Request r){
        this.requests.add(0, r);
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

    public boolean isWorkShopFree(){
        return !this.workshop.isOccupied() && Board.getEntity(this.workshop.location) == null;
    }

    public void changeWorkshopOccupied(){
        this.workshop.changeOccupied();
    }
}
