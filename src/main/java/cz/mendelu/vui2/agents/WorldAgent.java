package cz.mendelu.vui2.agents;

import cz.mendelu.vui2.agents.greenfoot.AbstractAgent;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class WorldAgent extends AbstractAgent {

    static char WALL = 'X';
    static char DIRT = '*';
    static char DOCK = '_';
    //static EnumSet<Character> GROUND;

    ArrayList<ArrayList<Character>> world;

    @Override
    public Action doAction(boolean canMove, boolean dirty, boolean dock) {

        world = new ArrayList<>();

        return null;
    }
}
