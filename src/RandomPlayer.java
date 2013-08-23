import java.util.Random;
import java.awt.Color;
import java.awt.Point;

public class RandomPlayer extends Player{
    private Color myColor;
    private String name;
    private Random generator = new Random();
    private boolean first;
    private boolean firstAssigned = false;

    public RandomPlayer(Color c, String n){
        super(c,n);
        myColor = c;
        name = n;
    }
    
    public Color getColor(){
        return myColor; 
    }
    
    public String getName(){
        return name;
    }
    
    public Point getMove(Board theBoard){
        if (!firstAssigned){
            if (theBoard.getState(3,3) == Color.black){
                if (theBoard.getState(4,3) == Color.white){
                    if (theBoard.getState(3,4) == Color.white){
                        if (theBoard.getState(4,4) == Color.black){
                            first = true;
                            firstAssigned = true;
                            //System.out.println(myColor.toString() + " IM FIRST");
                        } else {
                            first = false;
                        }
                    } else {
                        first = false;
                    }
                } else {
                    first = false;
                }
            } else {
                first = false;
            }
        }
        int r = 0;
        int c = 0;
        while(!theBoard.isLegal(r,c,myColor)){
            r = generator.nextInt(8);
            c = generator.nextInt(8);
        }
        return new Point(r,c);
    }





}