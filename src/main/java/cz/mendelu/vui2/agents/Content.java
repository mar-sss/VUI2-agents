package cz.mendelu.vui2.agents;

/**
 * Created by Martin on 22.01.2019.
 */
enum Content {

    WALL('X'), DOCK('_'), VISITED('*'), FREE('0');

    char desc;
    Content(char c){
        this.desc = c;
    }

    @Override
    public String toString() {
        return Character.toString(this.desc);
    }

    public char getDesc() {
        return desc;
    }
}
