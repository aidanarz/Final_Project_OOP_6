package com.NetRoyale.models;

// Tower entity
public class Tower extends Entity {
    private boolean isKingTower;

    public Tower(String type, Team team, float x, float y, boolean isKing) {
        super(type, createTowerData(type), team, x, y);
        this.isKingTower = isKing;
    }

    private static UnitData createTowerData(String type) {
        if (type.equals("king")) {
            return new UnitData("King Tower", "[KT]", 0, 1200, 25, 0f, 200f, 1.0f, 40f, "building", UnitColor.KNIGHT);
        } else {
            return new UnitData("Princess Tower", "[PT]", 0, 700, 18, 0f, 170f, 0.8f, 25f, "building", UnitColor.KNIGHT);
        }
    }

    @Override
    public void moveTowardsTarget(float delta) {
        // Tower tidak bergerak
    }

    @Override
    public boolean isKing() {
        return isKingTower;
    }
}
