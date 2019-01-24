package cz.mendelu.vui2.agents;

import cz.mendelu.vui2.agents.greenfoot.AbstractAgent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReactionAgent extends AbstractAgent {

    protected boolean firstAction;

    protected StringBuilder actionList;
    //protected int battery;
    protected boolean goBack;
    protected int numberOfSteps; // indicates number of steps in rotation of 180 dgr. or in changing the direction
    protected boolean changingDirection; // indicates if the robot is in changing direction state
    protected boolean isFacingNorth; // it indicates in which direction is moving (because of rotations near walls)

    public ReactionAgent() {
        super();
        //this.battery = timeToSimulation;
        this.firstAction = false;
        this.actionList = new StringBuilder();
        this.goBack = false;
        this.numberOfSteps = 0;
        this.changingDirection = false;
        this.isFacingNorth = true;
    }

    @Override
    public Action doAction(boolean canMove, boolean dirty, boolean dock) {
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
                return Action.TURN_OFF;
            }
            char action = actionList.toString().charAt(actionList.length()-1);
            actionList.deleteCharAt(actionList.length()-1);
            switch (action){
                case 'F': return Action.FORWARD;
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
                actionList.append("F");
                return Action.FORWARD;
            } else{ //if I can't move at the beginning, let rotate right
                actionList.append("R");
                return Action.TURN_RIGHT;
            }
        }

        //I am in the dock again, so I can remove content in action list
        if (dock){
            actionList = new StringBuilder();
        }

        // clean the dirt if there is some
        if(dirty){
            return Action.CLEAN;
        }

        if(canMove && numberOfSteps == 0){ // I can move, so I go forward
            actionList.append("F");
            return Action.FORWARD;
        } else{ // I can't move, there is wall in front of me
            switch (numberOfSteps){ // I want to rotate by 180 degrees and move again
                case 0: // this will be first rotation
                    numberOfSteps++;
                    if(isFacingNorth){
                        actionList.append("R");
                        return Action.TURN_RIGHT;
                    } else{
                        actionList.append("L");
                        return Action.TURN_LEFT;
                    }
                case 1:
                    if(canMove && !changingDirection){
                        numberOfSteps++;
                        actionList.append("F");
                        return Action.FORWARD;
                    }else if (canMove){
                        numberOfSteps = 0;
                        changingDirection = false;
                        reverseDirection();
                        actionList.append("F");
                        return Action.FORWARD;
                    }
                    else{
                        changingDirection = true;
                        if(isFacingNorth){
                            actionList.append("L");
                            return Action.TURN_LEFT;
                        }else{
                            actionList.append("R");
                            return Action.TURN_RIGHT;
                        }

                    }
                case 2:
                    numberOfSteps++;
                    if(isFacingNorth){
                        actionList.append("R");
                        return Action.TURN_RIGHT;
                    }else{
                        actionList.append("L");
                        return Action.TURN_LEFT;
                    }
                case 3:
                    if(canMove){
                        numberOfSteps = 0;
                        reverseDirection();
                        actionList.append("F");
                        return Action.FORWARD;
                    } else{ // I am trying to find first free cell
                        if (isFacingNorth){
                            actionList.append("L");
                            return Action.TURN_LEFT;
                        }else{
                            actionList.append("R");
                            return Action.TURN_RIGHT;
                        }
                    }
                default: return Action.TURN_OFF; // this should not happen
            }
        }

        /*

        if(canMove){ // (canMove && !dirty)
            if(actionList.substring(actionList.length()-1,actionList.length()).equals("F")){ // previous action = forward?
                if(actionList.length()>1 && actionList.substring(actionList.length()-2,actionList.length()-1).equals("R")){ // last rotation = right?
                    if(actionList.substring(actionList.length()-3,actionList.length()-2).equals("R")){
                        //TODO
                    }

                    actionList.append("L");
                    return Action.TURN_LEFT;
                } else{
                    actionList.append("R");
                    return Action.TURN_RIGHT;
                }
            }
            actionList.append("F");
            return Action.FORWARD; // previous action != forward and I can move, so I go forward
        } else { // equals (!canMove && !dirty)
            // if previous rotation = R --> rotate R (I need to rotate the same direction or I can circle in the corner!)
            if(getLastRotation() == 'R'){
                actionList.append("R");
                return Action.TURN_RIGHT;
            }
            // if previous rotation = L --> rotate L (I need to rotate the same direction or I can circle in the corner!)
            actionList.append("L");
            return Action.TURN_LEFT;
        }
     */
    }

    private char getLastRotation(){
        Pattern pattern = Pattern.compile("(R|L)F*$");
        Matcher matcher = pattern.matcher(actionList);
        if(matcher.matches()){
            return matcher.group(1).toCharArray()[0];
        }
        return 'u';

    }


    protected char getLastAction(){
        return actionList.substring(actionList.length()-1,actionList.length()).toCharArray()[0];
    }

    protected void reverseActions(){
        String s = actionList.toString().replaceAll("R","l").replaceAll("L","r").replaceAll("l","L").replaceAll("r","R");
        actionList = new StringBuilder(s);

        //dont make cleaning again!
        s = actionList.toString().replaceAll("C","");
        actionList = new StringBuilder(s);

    }

    protected void reverseDirection(){
        if (isFacingNorth){
            isFacingNorth = false;
        }else{
            isFacingNorth = true;
        }
    }
}
