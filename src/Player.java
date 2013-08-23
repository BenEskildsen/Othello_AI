import java.awt.Color;
import java.awt.Point;

public abstract class Player{
	Color myColor;
	String name;
	
	public Player(Color c, String n){
		myColor = c;
		name = n;
	}
	
	public Color getColor(){
		return myColor; 
	}
	
	public String getName(){
		return name;
	}
	
	public abstract Point getMove(Board theBoard);
}