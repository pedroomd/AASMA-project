package rentAcar;

import java.util.List;
import java.awt.*;
import java.util.ArrayList;

public class Central {
    private List<CarParking> carParkings;
    private List<Request> requests;
    private List<Car> cars;
    private Workshop workshop;
    private int carsDown = 0;
    private int satisfiedClients = 0;
    private int unsatisfiedClients = 0;
    private int giveAwayCarParking = 0;
    private int totalRequest = 0;
    private double meanWaitingTime = 0;

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
    
    public List<CarParking> getCarParkings(){
        return this.carParkings;
    }
    
    public CarParking getCarParking(CarParking park){
        int index = this.carParkings.indexOf(park);
        return this.carParkings.get(index);
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


    public void setCarParkingOccupied(CarParking park, boolean bool){
        int index = this.carParkings.indexOf(park);
        this.carParkings.get(index).changeOccupied(bool);
    }
    
    public void setCarParkingHasArrived(CarParking park, boolean bool){
        int index = this.carParkings.indexOf(park);
        this.carParkings.get(index).setHasArrived(bool);
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
        if(this.workshop.isOccupied()) carsDown++;
    }

    public int numberOfcars(){
        return cars.size();
    }

    public int getCarsDown(){
        return this.carsDown;
    }

    public int getSatisfiedClients(){
        return this.satisfiedClients;
    }


    public int getUnsatisfiedClients(){
        return this.unsatisfiedClients;
    }

    public int getGiveAwayCarParking(){
        return this.giveAwayCarParking;
    }

    public void increSatisfiedClients(){
        this.satisfiedClients++;
    }

    public void increUnsatisfiedClients(){
        this.unsatisfiedClients++;
    }

    public void giveAwayCarParking(){
        this.giveAwayCarParking++;
    }

    public void updateWaitTime(int stepRequest){
        int difference = Board.getStepCounter() - stepRequest;
        this.meanWaitingTime = (this.meanWaitingTime * this.totalRequest + difference) / (this.totalRequest + 1);
        this.totalRequest++;
    }

    public double getMeanWaitTime(){
        return this.meanWaitingTime;
    }

}
