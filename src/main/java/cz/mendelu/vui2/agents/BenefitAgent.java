package cz.mendelu.vui2.agents;

import java.util.ArrayList;

public class BenefitAgent extends GoalAgent {

    @Override
    @SuppressWarnings("Duplicates")
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

    //now, look at all zeros and calculate the shortest path of all of them and pick cheapest one
    @Override
    protected Position findZero() {
        int minPathCost = Integer.MAX_VALUE;
        Position minPathFreeCell = null;
        for (Position pos : world.keySet()){ //get all zeros
            if (world.get(pos) == Content.FREE){
                //calculate all paths to zeros
                StringBuilder path = makeMovesFromAToB(actualPosition, pos);
                path.append(calculateRotationsToPath(pos).toString()); // I calculate also rotations to it
                if (path.length()<minPathCost){
                    minPathCost = path.length();
                    minPathFreeCell = pos; //pick the shortest one
                }
            }
        }
        return minPathFreeCell;
    }

    //now, look at all unobserved positions and calculate the shortest path of all of them and pick cheapest one
    @SuppressWarnings("Duplicates")
    @Override
    protected Position findUnobservedCell() { //TODO another approach - pick unobserved neighbors of all positions in world

        int minX = 0;
        int maxX = 0;
        int minY = 0;
        int maxY = 0;
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
        //find all unobserved cells according to max
        Position pos;
        //int max = findMaxLevel()+1; //+1 because of borders
        int minPathCost = Integer.MAX_VALUE;
        Position minPathFreeCell = null;
        for (int i = minX-1; i<=maxX+1; i++){ //from min x to max x
            for (int j = minY-1; j<=maxY+1; j++){ //from min y to max y
                pos = new Position(i,j);
                if ((world.get(pos) == null) && hasCellReachableNeighbor(pos)){//if position is not in world and discover if it has reachable neighbor (because if not, it is not necessary to deal with it)
                    //calculate all paths to zeros
                    world.put(pos, Content.UNOBSERVED);
                    StringBuilder path = makeMovesFromAToB(actualPosition, pos);
                    if (path.length()>0){
                        path.append(calculateRotationsToPath(pos).toString()); // I calculate also rotations to it
                        if (path.length()<minPathCost){ //check if it is shortest path
                            minPathCost = path.length();
                            minPathFreeCell = pos; //pick the shortest one
                        }
                    }
                    world.remove(pos);
                }
            }
        }
        return minPathFreeCell;
}

    //method for calculating rotations in the beggining in some path for precise calculation of REAL path cost
    private StringBuilder calculateRotationsToPath(Position dest){
        //append correct rotation to the first neighbor
        StringBuilder appendedRotations = new StringBuilder();
        ArrayList<Position> pathAsArray = getPathAsPositionArray(dest);
        Position firstNeighbor = pathAsArray.remove(1);
        WorldDirection worldDirectionToNeighbor = getWDToNeighbor(actualPosition, firstNeighbor);
        int turns = 0;
        WorldDirection originWD = worldDirection;
        while (worldDirectionToNeighbor != null && worldDirection != worldDirectionToNeighbor) {
            turnRight();
            turns++;
        }
        if (turns == 3) { //if it was needed 3x rotation to get right direction, just rotate left
            appendedRotations.append("L");
        } else {
            for (int i = 0; i < turns; i++) {
                appendedRotations.append("R");
            }
        }
        worldDirection = originWD; //I used worldDirection for calculation of rotations, but I don't want to change it
        return appendedRotations;
    }
}
