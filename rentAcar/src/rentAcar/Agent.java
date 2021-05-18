package rentAcar;

import java.awt.Color;
import java.awt.Point;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import rentAcar.Block.Shape;

/**
 * Agent behavior
 * @author Rui Henriques
 */
public class Agent extends Entity {

	public enum Desire { grab, drop, initialPosition }
	public enum Action { moveAhead, rotate, grab, drop, rotateRight, rotateLeft}
	
	public static int NUM_BOXES = 8;
	
	public int direction = 90;
	public Client cargo;
	
	public Point initialPoint;
	public int boxesOnShelves, boxesOnRamp;
	public Map<Point,Block> warehouse; //internal map of the warehouse
	public List<Point> freeShelves; //free shelves
	public List<Point> boxedRamp; //ramp cells with boxes
	
	public List<Desire> desires;
	public AbstractMap<Desire,Point> intention;
	
	public Queue<Action> plan;
	public Action lastAction;
	
	private Point ahead;
	
	public Agent(Point point, Color color){ 
		super(point, color);
		cargo = null;
		initialPoint = point;
		boxesOnShelves = 0;
		boxesOnRamp = NUM_BOXES;
		freeShelves = new ArrayList<Point>();
		boxedRamp = new ArrayList<Point>();
		warehouse = new HashMap<Point,Block>();
		plan = new LinkedList<Action>();
	} 
	
	/**********************
	 **** A: decision ***** 
	 **********************/
		
	public void agentDecision() {
		updateBeliefs();
		
		if(hasPlan() && !succeededIntention() && !impossibleIntention()){
			Action action = plan.remove();
            if(isPlanSound(action)) execute(action); 
            else buildPlan();
            if(reconsider()) deliberate();
			
		} else {
			updateBeliefs();
			deliberate();
			buildPlan();
			if(!hasPlan()) agentReactiveDecision();
		}
	}		

	private void buildPlan() {
	}

	private boolean isPlanSound(Action action) {
		return false;
	}

	private void deliberate() {
	}

	private boolean reconsider() {
		return false;
	}

	private void execute(Action action) {
	}

	private boolean impossibleIntention() {
		return false;
	}

	private boolean succeededIntention() {
		return false;
	}

	/*******************************/
	/**** B: reactive behavior ****/
	/*******************************/

	public void agentReactiveDecision() {
	  ahead = aheadPosition();
	  if(isWall()) rotateRandomly();
	  else if(isRamp() && isBoxAhead() && !cargo()) grabBox();
	  else if(isShelf() && !isBoxAhead() && cargo() && shelfColor().equals(cargoColor())) dropBox();
	  else if(!isFreeCell()) rotateRandomly();
	  else if(random.nextInt(5) == 0) rotateRandomly();
	  else moveAhead();
	}
	
	/**************************/
	/**** C: communication ****/
	/**************************/
	
	private void updateBeliefs() {
		ahead = aheadPosition();
		//Discovery interesting things
		if(isRamp()){
			Block block = Board.getBlock(ahead);
			Board.sendMessage(ahead, Shape.ramp, block.color, true);
		}
	}
	

	public void receiveMessage(Point point, Shape shape, Color color, Boolean free) {
	}
	
	public void receiveMessage(Action action, Point pt) {
	} 

	/*******************************/
	/**** D: planning auxiliary ****/
	/*******************************/

	private Queue<Action> buildPathPlan(Point p1, Point p2) {
		Stack<Point> path = new Stack<Point>();
		Node node = shortestPath(p1,p2);
		path.add(node.point);
		while(node.parent!=null) {
			node = node.parent;
			path.push(node.point);
		}
		Queue<Action> result = new LinkedList<Action>();
		p1 = path.pop();
		int auxdirection = direction;
		while(!path.isEmpty()) {
			p2 = path.pop();
			result.add(Action.moveAhead);
			result.addAll(rotations(p1,p2));
			p1 = p2;
		}
		direction = auxdirection;
		result.remove();
		return result;
	}
	
	private List<Action> rotations(Point p1, Point p2) {
		List<Action> result = new ArrayList<Action>();
		while(!p2.equals(aheadPosition())) {
			Action action = rotate(p1,p2);
			if(action==null) break;
			execute(action);
			result.add(action);
		}
		return result;
	}

	private Action rotate(Point p1, Point p2) {
		boolean vertical = Math.abs(p1.x-p2.x)<Math.abs(p1.y-p2.y);
		boolean upright = vertical ? p1.y<p2.y : p1.x<p2.x;
		if(vertical) {  
			if(upright) { //move up
				if(direction!=0) return direction==90 ? Action.rotateLeft : Action.rotateRight;
			} else if(direction!=180) return direction==90 ? Action.rotateRight : Action.rotateLeft;
		} else {
			if(upright) { //move right
				if(direction!=90) return direction==180 ? Action.rotateLeft : Action.rotateRight;
			} else if(direction!=270) return direction==180 ? Action.rotateRight : Action.rotateLeft;
		}
		return null;
	}

	
	/********************/
	/**** E: sensors ****/
	/********************/
	
	/* Check if agent is carrying box */
	public boolean cargo() {
		return cargo != null;
	}
	
	/* Return the color of the box */
	public Color cargoColor() {
	  return cargo.color;
	}

	/* Return the color of the shelf ahead or 0 otherwise */
	public Color shelfColor(){
		return Board.getBlock(ahead).color;
	}

	/* Check if the cell ahead is floor (which means not a wall, not a shelf nor a ramp) and there are any robot there */
	public boolean isFreeCell() {
	  return isRoomFloor() && Board.getEntity(ahead)==null;
	}

	public boolean isRoomFloor() {
		return Board.getBlock(ahead).shape.equals(Shape.free);
	}
	
	/* Check if the cell ahead contains a box */
	public boolean isBoxAhead(){
		Entity entity = Board.getEntity(ahead);
		return entity!=null && entity instanceof Client;
	}

	/* Return the type of cell */
	public Shape cellType() {
	  return Board.getBlock(ahead).shape;
	}

	/* Return the color of cell */
	public Color cellColor() {
	  return Board.getBlock(ahead).color;
	}

	/* Check if the cell ahead is a shelf */
	public boolean isShelf() {
	  Block block = Board.getBlock(ahead);
	  return block.shape.equals(Shape.shelf);
	}

	/* Check if the cell ahead is a ramp */
	public boolean isRamp(){
	  Block block = Board.getBlock(ahead);
	  return block.shape.equals(Shape.ramp);
	}

	/* Check if the cell ahead is a wall */
	private boolean isWall() {
		return ahead.x<0 || ahead.y<0 || ahead.x>=Board.nX || ahead.y>=Board.nY;
	}

	/* Check if the cell ahead is a wall */
	private boolean isWall(int x, int y) {
		return x<0 || y<0 || x>=Board.nX || y>=Board.nY;
	}

	/**********************/
	/**** F: actuators ****/
	/**********************/

	/* Rotate agent to right */
	public void rotateRandomly() {
		if(random.nextBoolean()) rotateLeft();
		else rotateRight();
	}
	
	/* Rotate agent to right */
	public void rotateRight() {
		direction = (direction+90)%360;
	}
	
	/* Rotate agent to left */
	public void rotateLeft() {
		direction = (direction-90+360)%360;
	}
	
	/* Move agent forward */
	public void moveAhead() {
		Board.updateEntityPosition(point,ahead);
		if(cargo()) cargo.moveBox(ahead);
		point = ahead;
	}

	/* Cargo box */
	public void grabBox() {
	  cargo = (Client) Board.getEntity(ahead);
	  cargo.grabBox(point);
	}

	/* Drop box */
	public void dropBox() {
		cargo.dropBox(ahead);
	    cargo = null;
	}
	
	/**********************/
	/**** G: auxiliary ****/
	/**********************/

	/* Check if plan is empty */
	private boolean hasPlan() {
		return !plan.isEmpty();
	}

	/* Position ahead */
	private Point aheadPosition() {
		Point newpoint = new Point(point.x,point.y);
		switch(direction) {
			case 0: newpoint.y++; break;
			case 90: newpoint.x++; break;
			case 180: newpoint.y--; break;
			default: newpoint.x--; 
		}
		return newpoint;
	}
	
	/* For queue used in shortest path */
	public class Node { 
	    Point point;   
	    Node parent; //cell's distance to source 
	    public Node(Point point, Node parent) {
	    	this.point = point;
	    	this.parent = parent;
	    }
	    public String toString() {
	    	return "("+point.x+","+point.y+")";
	    }
	} 
	
	public Node shortestPath(Point src, Point dest) { 
	    boolean[][] visited = new boolean[100][100]; 
	    visited[src.x][src.y] = true; 
	    Queue<Node> q = new LinkedList<Node>(); 
	    q.add(new Node(src,null)); //enqueue source cell 
	    
		//access the 4 neighbours of a given cell 
		int row[] = {-1, 0, 0, 1}; 
		int col[] = {0, -1, 1, 0}; 
	     
	    while (!q.isEmpty()){//do a BFS 
	        Node curr = q.remove(); //dequeue the front cell and enqueue its adjacent cells
	        Point pt = curr.point; 
	        for (int i = 0; i < 4; i++) { 
	            int x = pt.x + row[i], y = pt.y + col[i]; 
    	        if(x==dest.x && y==dest.y) return new Node(dest,curr); 
	            if(!isWall(x,y) && !warehouse.containsKey(new Point(x,y)) && !visited[x][y]){ 
	                visited[x][y] = true; 
	    	        q.add(new Node(new Point(x,y), curr)); 
	            } 
	        }
	    }
	    return null; //destination not reached
	} 

}
