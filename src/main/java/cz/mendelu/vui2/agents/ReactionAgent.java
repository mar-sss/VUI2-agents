package cz.mendelu.vui2.agents;

import cz.mendelu.vui2.agents.greenfoot.AbstractAgent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReactionAgent extends AbstractAgent {

    boolean firstAction = false;

    StringBuilder actionList = new StringBuilder();

    @Override
    public Action doAction(boolean canMove, boolean dirty, boolean dock) {

        // if it is the first action of the agent, let leave the dock
        if(!firstAction){
            if(dirty){ // if there is dirt in the dock, clean it at first
                actionList.append("C");
                return Action.CLEAN;
            }
            if(canMove) { // if I can move from dock, go forward
                firstAction = true; // this was my first move
                actionList.append("F");
                return Action.FORWARD;
            } else{ //if I can't move at the beginning, let rotate right
                firstAction = true; // this was my first move
                actionList.append("R");
                return Action.TURN_RIGHT;
            }
        }

        // exit the task, I am in the dock again
        if (dock){
                actionList.append("D");
                return Action.DONE;
        }

        // clean the dirt if there is some
        if(dirty){
            actionList.append("C");
            return Action.CLEAN;
        }

        if(canMove){ // (canMove && !dirty)
            if(actionList.substring(actionList.length()-1,actionList.length()).equals("F")){ // previous action = forward?
                if(actionList.length()>1 && actionList.substring(actionList.length()-2,actionList.length()-1).equals("R")){ // last rotation = right?
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
