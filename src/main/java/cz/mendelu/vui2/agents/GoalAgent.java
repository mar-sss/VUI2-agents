package cz.mendelu.vui2.agents;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static java.lang.Math.abs;

public class GoalAgent extends WorldAgent {

    protected StringBuilder pathToFreeSpace;

    protected boolean tryNew;
    protected boolean goingToSomePath;

    public GoalAgent() {
        super();
        pathToFreeSpace = new StringBuilder();
        tryNew = false;
        goingToSomePath = false;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public Action doAction(boolean canMove, boolean dirty, boolean dock) {
        printWorld();
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
            return goHome();
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

        if (pathToFreeSpace.length()>0 || goingToSomePath){
            if (canMove && pathToFreeSpace.length() == 0){//I am in front of it -- what is it?
                addAheadCellToWorld(Content.VISITED);
                goingToSomePath = false;
                return forward();
            }else if (!canMove && pathToFreeSpace.length() == 0){
                addAheadCellToWorld(Content.WALL);
                tryNew = true;
            }else {
                System.out.println("Path to new free space: " + pathToFreeSpace);
                char action = pathToFreeSpace.toString().charAt(pathToFreeSpace.length() - 1);
                pathToFreeSpace.deleteCharAt(pathToFreeSpace.length() - 1);
                switch (action) { //just make actions as are in pathToHome
                    case 'F': return forward();
                    case 'R': return turnRight();
                    case 'L': return turnLeft();
                    default:  return Action.TURN_OFF;
                }
            }
        }

        //if (!canMove){ // I cant move and all neighbors are observed
        //    addAheadCellToWorld(Content.WALL);
        //    if (findRotationWithFreeCell() == null){
        //        tryNew = true;
        //    }
        //}

        if((canMove && numberOfSteps == 0) || tryNew){ // I can move, so I go forward
            if(getContentAhead() == Content.VISITED || getContentAhead() == Content.DOCK || tryNew){
                tryNew = false;
                Action foundAction = findRotationWithFreeCell();
                if (foundAction == null){ //there is no other free cell next to me, so I will try to find first FREE
                    Position pos = findZero(); //find FIRST free space in world (FREE = unvisited, but observed)
                    if (pos != null){
                        pathToFreeSpace = makeMovesFromAToB(actualPosition, pos);
                        //append correct rotation to the first neighbor
                        ArrayList<Position> pathToFreeSpaceAsArray = getPathAsPositionArray(pos);
                        Position firstNeighbor = pathToFreeSpaceAsArray.remove(1);
                        WorldDirection worldDirectionToNeighbor = getWDToNeighbor(actualPosition, firstNeighbor);
                        int turns = 0;
                        WorldDirection originWD = worldDirection;
                        while (worldDirectionToNeighbor != null && worldDirection != worldDirectionToNeighbor) {
                            turnRight();
                            turns++;
                        }
                        if (turns == 3) { //if it was needed 3x rotation to get right direction, just rotate left
                            pathToFreeSpace.append("L");
                        } else {
                            for (int i = 0; i < turns; i++) {
                                pathToFreeSpace.append("R");
                            }
                        }
                        worldDirection = originWD; //I used worldDirection for calculation of rotations, but I dont want to change it
                        System.out.println("Path to new free space: " + pathToFreeSpace);
                        goingToSomePath = true;
                        char action = pathToFreeSpace.toString().charAt(pathToFreeSpace.length()-1);
                        pathToFreeSpace.deleteCharAt(pathToFreeSpace.length()-1);
                        switch (action) { //just make actions as are in pathToFreeSpace
                            case 'F': return forward();
                            case 'R': return turnRight();
                            case 'L': return turnLeft();
                            default:  return Action.TURN_OFF;
                        }
                    }else{
                        //I didnt find any FREE space - so I try to find UNOBSERVED space
                        Position newPosition = findUnobservedCell();
                        if (newPosition != null){
                            pathToFreeSpace = makeMovesFromAToB(actualPosition, newPosition);
                            //append correct rotation to the first neighbor
                            ArrayList<Position> pathToFreeSpaceAsArray = getPathAsPositionArray(newPosition);
                            Position firstNeighbor = pathToFreeSpaceAsArray.remove(1);
                            WorldDirection worldDirectionToNeighbor = getWDToNeighbor(actualPosition, firstNeighbor);
                            int turns = 0;
                            WorldDirection originWD = worldDirection;
                            while (worldDirectionToNeighbor != null && worldDirection != worldDirectionToNeighbor) {
                                turnRight();
                                turns++;
                            }
                            if (turns == 3) { //if it was needed 3x rotation to get right direction, just rotate left
                                pathToFreeSpace.append("L");
                            } else {
                                for (int i = 0; i < turns; i++) {
                                    pathToFreeSpace.append("R");
                                }
                            }
                            worldDirection = originWD; //I used worldDirection for calculation of rotations, but I dont want to change it
                            System.out.println("Path to new UNOBSERVED space: " + pathToFreeSpace);
                            goingToSomePath = true;
                            char action = pathToFreeSpace.toString().charAt(pathToFreeSpace.length()-1);
                            pathToFreeSpace.deleteCharAt(0); //delete last FORWARD, because I don't know if it WALL or not!
                            world.remove(newPosition); //remove the new position as I don't know what is there!
                            pathToFreeSpace.deleteCharAt(pathToFreeSpace.length()-1);
                            switch (action) { //just make actions as are in pathToFreeSpace
                                case 'F': return forward();
                                case 'R': return turnRight();
                                case 'L': return turnLeft();
                                default:  return Action.TURN_OFF;
                            }
                        }
                        //there is nothing that I can observe, so I go HOME!
                        goBack = true;
                        return goHome();
                    }
                }
                return foundAction;
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
                            return turnLeft();
                        }else{
                            return turnRight();
                        }
                    }
                default: return Action.TURN_OFF; // this should not happen
            }
        }


    }

    protected Position findZero() {
        for (Position pos : world.keySet()){
            if (world.get(pos) == Content.FREE){
                return pos;
            }
        }
        return null;

    }

    protected Position findUnobservedCell(){
        int level = 1;
        int max = findMaxLevel();
        Position newPosition;
        StringBuilder path;
        while (level <= max){ //till the level is in maximum observation
            //for (int i = 1; i<=level;i++){ //I need to look at all cells that have distance based on level from me
            for (int i = -level; i<=level; i++){//y expansion from - to + level
                for (int j = -level; j<=level; j++){//x expansion to the left
                    newPosition = new Position(actualPosition.getX()+j, actualPosition.getY()+i);
                    if (world.get(newPosition) == null && hasCellReachableNeighbor(newPosition)){//if position is not in world and discover if it has reachable neighbor (because if not, it is not necessary to deal with it)
                        //I FOUND CELL THAT IS NOT IN WORLD -- find a road to it, if there is no road, try to find new
                        world.put(newPosition,Content.UNOBSERVED); //just for purposes of finding a way
                        path = makeMovesFromAToB(actualPosition, newPosition);
                        if (path.length()>0){ //path to it exists!
                            return newPosition;
                        }
                        world.remove(newPosition);
                    }
                }
            }
            level++;
        }
        return null;
    }

    protected int findMaxLevel(){
        int max = 0;
        //getting minimum and maximum x, y
        for (Position pos: world.keySet()){
            if (abs(pos.getX())>max){
                max = abs(pos.getX());
            }
            if (abs(pos.getY())>max){
                max = abs(pos.getY());
            }
        }
        return max;
    }

    @SuppressWarnings("Duplicates")
    @Override
    protected Action findRotationWithFreeCell() {

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
        //there is no free cell, so I return null instead of forward as in previous agent - so I can start searching for undiscovered places
        return null;

    }

    protected Action goHome(){
        if (pathToHome.length() == 0){
            return Action.TURN_OFF;
        }
        char action = pathToHome.toString().charAt(pathToHome.length()-1);
        pathToHome.deleteCharAt(pathToHome.length()-1);
        switch (action){ //just make actions as are in pathToHome
            case 'F': return forward();
            case 'R': return turnRight();
            case 'L': return turnLeft();
            default:  return Action.TURN_OFF;
        }
    }

    // for position, look on the world if I can reach it somehow - it means if it has some neighbor which is FREE/VISITED
    protected boolean hasCellReachableNeighbor(Position pos){
        Set<Content> reachableContent = new HashSet<>();
        reachableContent.add(Content.FREE);
        reachableContent.add(Content.VISITED);
        reachableContent.add(Content.DOCK);
        Position north = new Position(pos.getX(),pos.getY()+1);
        Position south = new Position(pos.getX(), pos.getY()-1);
        Position east = new Position(pos.getX()+1, pos.getY());
        Position west = new Position(pos.getX()-1, pos.getY());

        return  reachableContent.contains(world.get(north)) ||
                reachableContent.contains(world.get(south)) ||
                reachableContent.contains(world.get(east)) ||
                reachableContent.contains(world.get(west));
    }
}
