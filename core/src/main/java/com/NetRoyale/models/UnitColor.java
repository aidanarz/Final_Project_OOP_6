package com.NetRoyale.models;

import com.badlogic.gdx.graphics.Color;

public enum UnitColor {
    KNIGHT(new Color(0.867f, 0.867f, 0.867f, 1)),
    ARCHER(new Color(0.553f, 0.431f, 0.388f, 1)),
    GIANT(new Color(0.965f, 0.733f, 0.259f, 1)),
    GOBLIN(new Color(0.282f, 0.733f, 0.471f, 1)),
    SKELETON(Color.WHITE),
    WIZARD(new Color(0.259f, 0.6f, 0.882f, 1)),
    GOLEM(new Color(0.443f, 0.502f, 0.588f, 1)),
    BOMBER(new Color(0.176f, 0.208f, 0.282f, 1)),
    MUSKETEER(new Color(0.624f, 0.478f, 0.918f, 1)),
    VALKYRIE(new Color(0.929f, 0.522f, 0.212f, 1)),
    PLAYER_TOWER(new Color(0.443f, 0.502f, 0.588f, 1)),
    ENEMY_TOWER(new Color(0.443f, 0.502f, 0.588f, 1));

    private final Color color;

    UnitColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
