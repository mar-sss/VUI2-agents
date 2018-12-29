package cz.mendelu.vui2.agents;

import cz.mendelu.vui2.agents.greenfoot.AbstractAgent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReactionAgent extends AbstractAgent {

    private boolean firstAction = false;

    private StringBuilder actionList = new StringBuilder();
    private int initBattery = 1000;
    private int battery = initBattery;
    private boolean goBack = false;

    @Override
    public Action doAction(boolean canMove, boolean dirty, boolean dock) {
        if(canMove){
            canMove = false;
        } else{
            canMove = true;
        }
        battery--;
        if(!goBack && battery <= ((initBattery/2)+2)){ //+2 because of rotation!
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

        // exit the task, I am in the dock again
        //if (firstAction && dock){
        //    actionList.append("D");
        //    return Action.TURN_OFF;
        //}

        // clean the dirt if there is some
        if(dirty){
            //actionList.append("C");
            return Action.CLEAN;
        }

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
    }

    private char getLastRotation(){
        Pattern pattern = Pattern.compile("(R|L)F*$");
        Matcher matcher = pattern.matcher(actionList);
        if(matcher.matches()){
            return matcher.group(1).toCharArray()[0];
        }
        return 'u';

    }


    private char getLastAction(){
        return actionList.substring(actionList.length()-1,actionList.length()).toCharArray()[0];
    }

    private void reverseActions(){
        String s = actionList.toString().replaceAll("R","l").replaceAll("L","r").replaceAll("l","L").replaceAll("r","R");
        actionList = new StringBuilder(s);

        //dont make cleaning again!
        s = actionList.toString().replaceAll("C","");
        actionList = new StringBuilder(s);

    }

    public static void main(String [ ] args)
    {
        ReactionAgent reactionAgent =  new ReactionAgent();
        reactionAgent.doAction(true, false, false);
        reactionAgent.doAction(true, false, false);
        reactionAgent.doAction(true, false, false);
        reactionAgent.doAction(true, false, false);
        reactionAgent.doAction(true, false, false);
        reactionAgent.doAction(true, false, false);
        reactionAgent.doAction(true, false, false);
        reactionAgent.doAction(true, false, false);
        reactionAgent.doAction(true, false, false);
        reactionAgent.doAction(true, false, false);
        reactionAgent.doAction(false, false, false);
        reactionAgent.doAction(false, false, false);
        reactionAgent.doAction(false, false, false);
        reactionAgent.doAction(false, false, false);
        reactionAgent.doAction(false, false, false);
        reactionAgent.doAction(true, false, false);
    }

}
