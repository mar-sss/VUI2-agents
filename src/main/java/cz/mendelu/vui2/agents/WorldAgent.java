package cz.mendelu.vui2.agents;

import javafx.geometry.Pos;

import java.util.*;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class WorldAgent extends ReactionAgent {

    protected HashMap<Position, Content> world;

    protected Position actualPosition;

    protected WorldDirection worldDirection;

    protected StringBuilder pathToHome;

    public WorldAgent() {
        super();
        this.world = new HashMap<>();
        this.actualPosition = new Position(0,0);
        this.worldDirection = WorldDirection.N;
        this.world.put(actualPosition, Content.DOCK);
        this.pathToHome = new StringBuilder();
    }

    @SuppressWarnings("Duplicates")
    @Override
    public Action doAction(boolean canMove, boolean dirty, boolean dock) {
        printWorld();
        //ArrayList<Position> pathToDock = AStarFindWay(actualPosition, new Position(0,0));
        //System.out.println("Path to dock: " + pathToDock);
        //AStarFindWay(actualPosition, getDock()); // from actual position to dock
        //System.out.println(makeMovesFromAToB(actualPosition, getDock()));
        //previously printPath -- getPathAsPositionArray(getDock());

        canMove = !canMove;
        if (!goBack && !dock){// If I am going already back or I am in dock, no needed for home path cost calculation
            //calculate cost to home
            pathToHome = makeMovesFromAToB(actualPosition, getDock());
            //append correct rotation to the first neighbor
            ArrayList<Position> pathToHomeAsArray = getPathAsPositionArray(getDock());
            Position firstNeighbor = pathToHomeAsArray.remove(1);
            WorldDirection worldDirectionToNeighbor = getWDToNeighbor(actualPosition, firstNeighbor);
            int turns = 0;
            WorldDirection originWD = worldDirection;
            while (worldDirectionToNeighbor != null && worldDirection != worldDirectionToNeighbor) {
                turnRight();
                turns++;
            }
            if (turns == 3) { //if it was needed 3x rotation to get right direction, just rotate left
                pathToHome.append("L");
            } else {
                for (int i = 0; i < turns; i++) {
                    pathToHome.append("R");
                }
            }
            worldDirection = originWD; //I used worldDirection for calculation of rotations, but I dont want to change it
        }
        System.out.println(pathToHome + " Cost: " + pathToHome.length());
        timeToSimulation--; // I will do some move
        if((!goBack) && (timeToSimulation <=pathToHome.length()+3)){ //+3 because of turn off and rotations!
            goBack = true;
        }

        // if I have order to go back, I will do all moves from actionList
        if (goBack){
            if (pathToHome.length() == 0){
                return Action.TURN_OFF;
            }
            char action = pathToHome.toString().charAt(pathToHome.length()-1);
            pathToHome.deleteCharAt(pathToHome.length()-1);
            switch (action){ //just make actions as are in pathToHome
                case 'F': return forward();
                case 'R': return turnRight();
                case 'L': return turnLeft();
                default: return Action.TURN_OFF;
            }
        }

        // if it is the first action of the agent, let leave the dock
        if(!firstAction){
            if(dirty){ // if there is dirt in the dock, clean it at first
                //actionList.append("C");
                return Action.CLEAN;
            }
            if(canMove) { // if I can move from dock, go forward
                firstAction = true; // this was my first move
                return forward();
            } else{ //if I can't move at the beginning, let rotate right
                addAheadCellToWorld(Content.WALL);
                return turnRight();
            }
        }

        // clean the dirt if there is some
        if(dirty){
            return Action.CLEAN;
        }

        if(canMove && numberOfSteps == 0){ // I can move, so I go forward
            if(getContentAhead() == Content.VISITED || getContentAhead() == Content.DOCK){
                return findRotationWithFreeCell();
            }
            return forward();
        } else{ // I can't move, there is wall in front of me or I am doing rotation 180 dgr.
            switch (numberOfSteps){ // I want to rotate by 180 degrees and move again
                case 0: // this will be first rotation
                    addAheadCellToWorld(Content.WALL);
                    numberOfSteps++;
                    if(isFacingNorth){
                        return turnRight();
                    } else{
                        return turnLeft();
                    }
                case 1:
                    if(canMove && !changingDirection){
                        numberOfSteps++;
                        return forward();
                    }else if (canMove){
                        numberOfSteps = 0;
                        changingDirection = false;
                        reverseDirection();
                        return forward();
                    }
                    else{
                        addAheadCellToWorld(Content.WALL);
                        changingDirection = true;
                        if(isFacingNorth){
                            return turnLeft();
                        }else{
                            return turnRight();
                        }

                    }
                case 2:
                    numberOfSteps++;
                    if(canMove){ // I can see what is in front of me, even I don't care. But I can add it to my world
                        if(getContentAhead() == null){
                            addAheadCellToWorld(Content.FREE);
                            }
                    }else{
                        addAheadCellToWorld(Content.WALL);
                    }
                    if(isFacingNorth){
                        return turnRight();
                    }else{
                        return turnLeft();
                    }
                case 3:
                    if(canMove){
                        numberOfSteps = 0;
                        reverseDirection();
                        return forward();
                    } else{ // I am trying to find first free cell
                        addAheadCellToWorld(Content.WALL);
                        if (isFacingNorth){
                            return turnRight();
                        }else{
                            return turnLeft();
                        }
                    }
                default: return Action.TURN_OFF; // this should not happen
            }
        }

    }

    protected void printWorld(){ // print the world as a 2D matrix
        int minX = 0;
        int maxX = 0;
        int minY = 0;
        int maxY = 0;

        //System.out.println(world);

        //getting minimum and maximum x, y
        for (Position pos: world.keySet()){
            if (pos.getX()<minX){
                minX = pos.getX();
            }
            if (pos.getY()<minY){
                minY = pos.getY();
            }
            if (pos.getX()>maxX){
                maxX = pos.getX();
            }
            if (pos.getY()>maxY){
                maxY = pos.getY();
            }
        }
        System.out.print("  "); // for good position of x numbers
        for(int x = minX; x<=maxX;x++){
            System.out.printf("%1d ", abs(x) % 10); // x numbering
        }
        System.out.println();

        for (int y = maxY; y>=minY; y--){
            System.out.printf("%2d ", abs(y)); // y numbering
            for (int x = minX; x<=maxX; x++){
                char c = '?';
                Position pos = new Position(x,y);
                if (world.get(pos) != null){
                    c = world.get(pos).getDesc();
                    if (pos.equals(actualPosition)){
                        switch (worldDirection){
                            case E:c = '→';break;
                            case N:c = '↑';break;
                            case S:c = '↓';break;
                            case W:c = '←';break;
                        }
                    }
                }
                System.out.print(c + " ");
            }
            System.out.println();
        }
    }

    protected void addAheadCellToWorld(Content content){
        Position newPosition = new Position(actualPosition.getX() + worldDirection.getDx(), actualPosition.getY() + worldDirection.getDy());
        if(world.get(newPosition) != Content.DOCK){
            world.put(newPosition, content);
        }
    };

    protected void updateActualPosition(){
        actualPosition = new Position(actualPosition.getX()+worldDirection.getDx(), actualPosition.getY() + worldDirection.getDy());
    }

    protected Action turnRight(){
        switch (worldDirection){
            case N: worldDirection = WorldDirection.E; break;
            case S: worldDirection = WorldDirection.W; break;
            case W: worldDirection = WorldDirection.N; break;
            case E: worldDirection = WorldDirection.S; break;
        }
        actionList.append("R");
        return Action.TURN_RIGHT;
    }

    protected Action turnLeft(){
        switch (worldDirection){
            case N: worldDirection = WorldDirection.W; break;
            case S: worldDirection = WorldDirection.E; break;
            case W: worldDirection = WorldDirection.S; break;
            case E: worldDirection = WorldDirection.N; break;
        }
        actionList.append("L");
        return Action.TURN_LEFT;

    }

    protected Action forward(){
        actionList.append("F");
        addAheadCellToWorld(Content.VISITED);
        updateActualPosition();
        return Action.FORWARD;
    }

    @SuppressWarnings("Duplicates")
    protected Action findRotationWithFreeCell(){ // check if there is better way to go -- i.e. if I face way which I have already gone
        Position pos;
        Content c;
        WorldDirection dir = null;
        //is in right free cell?
        switch (worldDirection){
            case N: dir = WorldDirection.E; break;
            case S: dir = WorldDirection.W; break;
            case W: dir = WorldDirection.N; break;
            case E: dir = WorldDirection.S; break;
        }
        pos = new Position(actualPosition.getX()+dir.getDx(), actualPosition.getY()+dir.getDy());
        c = world.get(pos);
        if(c == Content.FREE || c == null){ //if is it free or undiscovered
            return turnRight();
        }

        //is in left free cell?
        switch (worldDirection){
            case N: dir = WorldDirection.W; break;
            case S: dir = WorldDirection.E; break;
            case W: dir = WorldDirection.S; break;
            case E: dir = WorldDirection.N; break;
        }
        pos = new Position(actualPosition.getX()+dir.getDx(), actualPosition.getY()+dir.getDy());
        c = world.get(pos);
        if(c == Content.FREE || c == null){ //if is it free or undiscovered
            return turnLeft();
        }
        return forward();
    }

    protected Content getContentAhead(){
        Position posAhead = new Position(actualPosition.getX()+worldDirection.getDx(), actualPosition.getY()+worldDirection.getDy());
        return world.get(posAhead);
    }

    // position dest is always position of dock
    protected void AStarFindWay(Position src, Position dest){

        //ArrayList<Position> path = new ArrayList<>();

        Set<Position> explored = new HashSet<>();

        PriorityQueue<Position> queue = new PriorityQueue<>(world.size(), new Comparator<Position>() {
            @Override
            public int compare(Position p1, Position p2) {
                //double fScoreP1 = calculateHScore(p1,dest) + calculateGScore(p1,dest); // dest nebo A?
                //double fScoreP2 = calculateHScore(p2,dest) + calculateGScore(p2,dest); // dest nebo A?

                double fScoreP1 = p1.getfScore();
                double fScoreP2 = p2.getfScore();
                if(fScoreP1>fScoreP2){
                    return 1;
                }else if (fScoreP1<fScoreP2){
                    return -1;
                }else{
                    return 0;
                }
            }
        });

        //novinka
        initFScore();
        calculateHScore(dest);

        queue.add(src);
        double gScore = 0;
        boolean found = false;

        while ((!queue.isEmpty()) && (!found)){

            Position current = queue.poll(); //lowest fscore Position
            explored.add(current);

            if (current.equals(dest)){
                found = true;
            }

            for(Position child : findNeighbors(current)){
                double tempGScore = gScore +1;
                double tempFScore = tempGScore + child.gethScore();
                if ((explored.contains(child)) && (tempFScore >= child.getfScore())){
                    continue;
                }else if (!queue.contains(child) || (tempFScore < child.getfScore())){ //update child
                    //path.add(child);
                    child.setParent(current);
                    child.setfScore(tempFScore);

                    if (queue.contains(child)){
                        queue.remove(child);
                    }
                    queue.add(child);
                }


            }
            gScore++;
        }
        //return path;
    }

    protected void calculateHScore(Position dest){

        for (Position pos : world.keySet()){
            pos.sethScore(sqrt(pow(dest.getX()-pos.getX(), 2) + pow(dest.getY()-pos.getY(), 2)));
            pos.setParent(null);
        }

        //return sqrt(abs(A.getX()-B.getX()) + abs(A.getY()-B.getY()));


    //private double calculateGScore(Position A, Position B){
    //   // asi neco s findNeighbors
    //}
    }
    protected void initFScore(){
        for (Position pos : world.keySet()){
            pos.setfScore(0);
        }
    }

    protected Set<Position> findNeighbors(Position x){
        Set<Position> returningSet = new HashSet<>();
        for (Position pos : world.keySet()){
            if ((!pos.equals(x)) && (world.get(pos) != Content.WALL)){
                if (
                        ((x.getX() == (pos.getX()+1)) && (x.getY() == pos.getY())) ||
                        ((x.getX() == (pos.getX()-1)) && (x.getY() == pos.getY())) ||
                        ((x.getY() == (pos.getY()+1)) && (x.getX() == pos.getX())) ||
                        ((x.getY() == (pos.getY()-1)) && (x.getX() == pos.getX()))
                ){
                    returningSet.add(pos);
                }
            }
        }
        return returningSet;
    }

    protected ArrayList<Position> getPathAsPositionArray(Position dest){
        ArrayList<Position> path = new ArrayList<>();

        for(Position pos = dest; pos!=null; pos = pos.getParent()){
            path.add(pos);
        }
        Collections.reverse(path);
        return path;
    }

    // the result is StringBuilder, which has first steps from end to beginning
    protected StringBuilder makeMovesFromAToB(Position src, Position dest) { //just moves without correct rotation of robot
        StringBuilder moves = new StringBuilder();
        AStarFindWay(src, dest); // it fills up parents in Positions
        Position currPos;
        Position nextPos;
        WorldDirection currPosWorldDirection = getWDToNeighbor(dest, dest.getParent()); //set WD accordng to his parents WD
        WorldDirection nextPosWorldDirection;
        for(currPos = dest; currPos!=null; currPos = currPos.getParent()){
            nextPos = currPos.getParent();

            if(nextPos == null){break;}

            nextPosWorldDirection = getWDToNeighbor(currPos, nextPos);

            if (currPosWorldDirection == nextPosWorldDirection){ //if it is in the same direction, go forward
                moves.append("F");
            }else{ //its not in same direction, we need correct rotations
                switch (nextPosWorldDirection){
                    case N:
                        switch (currPosWorldDirection){
                            case E:moves.append("RF");break;
                            case W:moves.append("LF");break;
                            case S:moves.append("LLF");break;
                        }
                        break;

                    case S:
                        switch (currPosWorldDirection){
                            case W:moves.append("RF");break;
                            case E:moves.append("LF");break;
                            case N:moves.append("LLF");break;
                        }
                        break;

                    case W:
                        switch (currPosWorldDirection){
                            case N:moves.append("RF");break;
                            case S:moves.append("LF");break;
                            case E:moves.append("LLF");break;
                        }
                        break;

                    case E:
                        switch (currPosWorldDirection){
                            case S:moves.append("RF");break;
                            case N:moves.append("LF");break;
                            case W:moves.append("LLF");break;
                        }
                        break;
                }
                currPosWorldDirection = nextPosWorldDirection;
            }
        }
        return moves;
    }

    //comparing two positions and returns world direction from start position to end position
    protected WorldDirection getWDToNeighbor(Position from, Position to){
        if (from == null || to == null || from.equals(to)){
            return null;
        }
        int dx = from.getX() - to.getX();
        int dy = from.getY() - to.getY();
        switch (dx){
            case 1: return WorldDirection.W;  // he is in the left from me
            case -1: return WorldDirection.E; //he is in the right from me
        }
        switch (dy){
            case 1: return WorldDirection.S; //he is down from me
            case -1: return WorldDirection.N; // he is up from me
        }
        return null;
    }

    protected Position getDock(){
        for(Position pos : world.keySet()){
            if (pos.getX() == 0 && pos.getY() == 0){
                return pos;
            }
        }
        return null;
    }
}
//TODO remove actionList