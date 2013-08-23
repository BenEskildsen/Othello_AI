// Benjamin Eskildsen UAHS '12 Yale '16 

import java.awt.Color;
import java.awt.Point; 
import java.util.ArrayList;

public class StackPlayer extends Player{
    private Color myColor;
    private String name;
    private int cutoffDepth = 6; // how many moves ahead we are examining
    private int numPos = 0; // number of positions examined 
    //private long startTime; // when we start the move

    //////////////////////////////////////////////////////////////////////////
    public StackPlayer(Color c, String n)
    {
        super(c,n); myColor = c; name = n;
    }
    //////////////////////////////////////////////////////////////////////////
    
    //////////////////////////////////////////////////////////////////////////
    public Point getMove(Board b)
    {
        //startTime = System.currentTimeMillis();
        ArrayList<Move> legalMoves = generateLegalMoves(b, myColor, null);

        // search the game tree
        for (Move m : legalMoves) {
            //numPos++; // keep track of the number of positions examined
            dfs(m,1,otherColor(myColor));
        }

        // find the best move
        Point move = new Point();
        int maxValue = -9999;
        for (Move m : legalMoves){
            if (m.getValue() >= maxValue) {
                maxValue = m.getValue();
                move = m.getPoint();
            }
        }
        // uncomment this if you want some running diagnostics of the AI
        // You will also have to uncomment everything with startTime and
        // everything with numPos in it in order for these to work
        // System.out.println("\nnumber of positions examined: " + numPos);
        // long currentTime = System.currentTimeMillis();
        // long totalTime = currentTime - startTime;
        // System.out.println("examined in " + (totalTime/1000.0) + " seconds");
        // System.out.println((int)(1.0*numPos/(totalTime/1000.0)) + " positions examined/sec");
        // numPos = 0;
        return move;
    }
    //////////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////
    // depth first search the game tree until cutoff depth is reached
    private int dfs(Move m, int d, Color c)
    {
        // base case
        if (d == cutoffDepth){
            //m.setValue(m.getCurrentValue());
            return m.getCurrentValue();
        }

        ArrayList<Move> legalMoves = generateLegalMoves(m.getBoard(), c, m);
        // search the game tree
        if (d == 1) {  // case that there is no parent
            for (Move next_m : legalMoves) {
                //numPos++; // keep track of the number of positions examined
                m.setValue(dfs(next_m,d+1,otherColor(c)));
            }
        } else {
            for (Move next_m : legalMoves) {
                // this if statement is using Alpha-Beta Pruning.
                // There are certain (many) cases where further examining
                // a given move tree is a waste of time since it will never
                // happen. If this move is mine and the parent's value
                // is greater than this one, then the value of the parent
                // can never be changed by this move tree since the parent will
                // only change on bigger children and this child will not get 
                // bigger (because it is my color). And vice versa
                if ((m.getParent().getValue() < m.getValue() &&
                     (m.getColor() == myColor)) ||
                     (m.getParent().getValue() > m.getValue() &&
                     m.getColor() != myColor)) {
                        //numPos++; // update number of positions examined
                        m.setValue(dfs(next_m,d+1,otherColor(c)));
                }
            }
        }
        return m.getValue();
    }
    //////////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////
    private Color otherColor(Color c)
    {
        if (c == Color.BLACK){return Color.WHITE;} return Color.BLACK;
    }
    //////////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////
    // what are the legal moves for a player at a board position?
    private ArrayList<Move> generateLegalMoves(Board b, Color co, Move p)
    {
        ArrayList<Move> theLegalMoves = new ArrayList<Move>();
        for(int r = 0; r < 8; r++){
            for(int c = 0; c < 8; c++){
                if(b.isLegal(r,c,co)){
                    Board newBoard = b.getCopy();
                    newBoard.placePiece(r,c,co);
                    theLegalMoves.add(new Move(newBoard, co, new Point(r,c), p));
                }
            }
        }
        return theLegalMoves;
    }
    //////////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////
    // the evaluation function of how good a given position is
    private int calcValue(Board b, Point p, Color c)
    {
        Color opponentColor = otherColor(myColor);

        // to avoid the colorCount() method call:
        int mcCount = b.colorCount(myColor);
        int ocCount = b.colorCount(opponentColor);
        
        int pieceDif = mcCount - ocCount;
        int cornerDif = cornerCount(b,myColor) - cornerCount(b,opponentColor);
        int winningMove = 0;
        int sideDif = sideCount(b,myColor) - sideCount(b,opponentColor);

        if(ocCount == 0 || (mcCount+ocCount == 64 && mcCount>ocCount)){
            winningMove = 1000;
        } else if(mcCount == 0 || (mcCount+ocCount == 64 && mcCount<ocCount)){
            winningMove = -1000;
        } else if(mcCount+ocCount == 64 && mcCount == ocCount){
            winningMove = 50;
        }
        
        // the evaluation function: a given position has value equal to this
        // equation. ie, having more pieces, having more corners (weighted 
        // by 10) winning the game (which is worth 1000 points) and having
        // more pieces on the sides (since they are safer)
        return pieceDif + 10*cornerDif + winningMove + sideDif;
    }
    //////////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////
    private int sideCount(Board b, Color c)
    {
        int total = 0;
        //checking the columns and rows
        for(int i = 0; i<8; i++){
            //left column
            if(b.getState(i,0) == c){ total++;}
            //right column
            if(b.getState(i,7) == c){ total++;}
            //top row
            if(b.getState(0,i) == c){ total++;}
            //bottom row
            if(b.getState(7,i) == c){ total++;}
        }
        return total;
    }
    //////////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////
    private int cornerCount(Board b, Color c)
    {   
        int count = 0;
        if(b.getState(0,0) == c){ count++;}
        if(b.getState(0,7) == c){ count++;}
        if(b.getState(7,0) == c){ count++;}
        if(b.getState(7,7) == c){ count++;}
        return count;
    }
    //////////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////
    // A board that represents making a certain move.
    private class Move
    {
        private Point p;  // the point made for this move
        private Board b;  // the board with p on it
        private Color c;  // the color of the move made
        private Move parent; // the parent of this move ie the move before this one
        private int currentValue = -10000;  // the value of this position, 
                                            // not based on the future
        private int value; // the value of this position based on future moves
                           // unless this is leaf move -> currentValue==value
        public Move(Board theBoard, Color theColor, Point thePoint, Move par){
            b = theBoard;
            c = theColor;
            p = thePoint;
            parent = par;
            if (c == myColor){
                value = 9999;
            } else {
                value = -9999;
            }
        }

        // getters and setters:
        public Move getParent(){return parent;}
        public Point getPoint(){return p;}
        public Board getBoard(){return b;}
        public Color getColor(){return c;}
        public int getValue(){ return value;}
        public int getCurrentValue()
        {
            if (currentValue == -10000){
                currentValue = calcValue(b, p, c);
            }
            return currentValue;
        }
        // set the value iff it improves this position (which it always will
        // in the initial case since the values are set so extremely).
        // This is called Minimax since it minimizes my score and maximizes
        // my opponent's score s.t. I will make the best move possible since 
        // my best move is the best assuming that my opponent will respond
        // optimally.
        public void setValue(int v){
            if ((c == myColor && v < value) || (c != myColor && v > value)){
                value = v;
            }
        }
    }
    //////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////
}