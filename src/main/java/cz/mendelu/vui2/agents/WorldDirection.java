package cz.mendelu.vui2.agents;

enum WorldDirection {

    N(0, 1),
    E(1, 0),
    S(0, -1),
    W(-1, 0);

    int dx, dy;

    WorldDirection(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public WorldDirection onLeft() {
        WorldDirection[] values = values();
        int ordinal = (values.length + ordinal() - 1) % values.length;
        return values[ordinal];
    }

    public WorldDirection onRight() {
        WorldDirection[] values = values();
        int ordinal = (ordinal() + 1) % values.length;
        return values[ordinal];
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }
}
