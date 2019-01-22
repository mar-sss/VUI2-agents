package cz.mendelu.vui2.agents;

import java.util.HashMap;

public class WorldAgent extends ReactionAgent {

    private HashMap<Position, Content> world;

    Position actualPosition;

    private WorldDirection worldDirection;

    public WorldAgent() {
        super();
        this.world = new HashMap<>();
        this.actualPosition = new Position(0,0);
        this.worldDirection = WorldDirection.N;
        this.world.put(actualPosition, Content.DOCK);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public Action doAction(boolean canMove, boolean dirty, boolean dock) {
        printWorld();
        if(canMove){ // just for better interpretation
            canMove = false;
        } else{
            canMove = true;
        }
        timeToSimulation--; // I will do some move
        if(!goBack && timeToSimulation <= ((actionList.length())+4)){ //+4 because of rotation and turning off!
            goBack = true;
            reverseActions();

            // if last action was rotation, then continue with the same rotation again to be in the correcct direction
            // why replace = because we have reversed actions!
            if(getLastAction() == 'R'){
                actionList.setCharAt(actionList.length()-1, 'L');
                //actionList.replace(actionList.length()-1, actionList.length(),"L");
            } else if(getLastAction() == 'L'){
                actionList.setCharAt(actionList.length()-1, 'R');
                //actionList.replace(actionList.length()-1, actionList.length(),"R");
            } else if(getLastAction() == 'F'){ //if yes, make a rotation = 2 rotations
                actionList.append('L');
                actionList.append('L');
            }
        }
        // if I have order to go back, I will do all moves from actionList
        if (goBack){
            if (actionList.length() == 0){
                printWorld();
                return Action.TURN_OFF;
            }
            char action = actionList.toString().charAt(actionList.length()-1);
            actionList.deleteCharAt(actionList.length()-1);
            switch (action){
                case 'F':
                    updateActualPosition();
                    return Action.FORWARD;
                case 'R': return Action.TURN_RIGHT;
                case 'L': return Action.TURN_LEFT;
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

        //exit the task, I am in the dock again
        //if (dock){
        //    actionList.append("D");
        //    return Action.TURN_OFF;
        //}

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

    private void printWorld(){ // print the world as a 2D matrix
        int minX = 0;
        int maxX = 0;
        int minY = 0;
        int maxY = 0;

        System.out.println(world);

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
        for (int y = maxY; y>=minY; y--){
            for (int x = minX; x<=maxX; x++){
                char c = '?';
                Position pos = new Position(x,y);
                if (world.get(pos) != null){
                    c = world.get(pos).getDesc();
                }
                System.out.print(c + " ");
            }
            System.out.println();
        }
    }

    private void addAheadCellToWorld(Content content){
        Position newPosition = new Position(actualPosition.getX() + worldDirection.getDx(), actualPosition.getY() + worldDirection.getDy());
        if(world.get(newPosition) != Content.DOCK){
            world.put(newPosition, content);
        }
    };

    private void updateActualPosition(){
        actualPosition = new Position(actualPosition.getX()+worldDirection.getDx(), actualPosition.getY() + worldDirection.getDy());
    }

    private Action turnRight(){
        switch (worldDirection){
            case N: worldDirection = WorldDirection.E; break;
            case S: worldDirection = WorldDirection.W; break;
            case W: worldDirection = WorldDirection.N; break;
            case E: worldDirection = WorldDirection.S; break;
        }
        actionList.append("R");
        return Action.TURN_RIGHT;
    }

    private Action turnLeft(){
        switch (worldDirection){
            case N: worldDirection = WorldDirection.W; break;
            case S: worldDirection = WorldDirection.E; break;
            case W: worldDirection = WorldDirection.S; break;
            case E: worldDirection = WorldDirection.N; break;
        }
        actionList.append("L");
        return Action.TURN_LEFT;

    }

    private Action forward(){
        actionList.append("F");
        addAheadCellToWorld(Content.VISITED);
        updateActualPosition();
        return Action.FORWARD;
    }

    @SuppressWarnings("Duplicates")
    private Action findRotationWithFreeCell(){ // check if there is better way to go -- i.e. if I face way which I have already gone
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

    private Content getContentAhead(){
        Position posAhead = new Position(actualPosition.getX()+worldDirection.getDx(), actualPosition.getY()+worldDirection.getDy());
        return world.get(posAhead);
    }


    // position B is always position of dock
    private StringBuilder findWayFromAtoB(Position A, Position B){
        //TODO A*
    }


    //TODO A* algorithm (from somewhere to somewhere with world available
}
