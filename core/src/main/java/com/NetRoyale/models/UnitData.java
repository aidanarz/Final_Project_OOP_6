package com.NetRoyale.models;

// Data class untuk statistik unit
public class UnitData {
    private final String name;
    private final String icon;
    private final int cost;
    private final int hp;
    private final int damage;
    private final float speed;
    private final float range;
    private final float cooldown;
    private final float size;
    private final String type;
    private final UnitColor color;

    public UnitData(String name, String icon, int cost, int hp, int damage, 
                    float speed, float range, float cooldown, float size, 
                    String type, UnitColor color) {
        this.name = name;
        this.icon = icon;
        this.cost = cost;
        this.hp = hp;
        this.damage = damage;
        this.speed = speed;
        this.range = range;
        this.cooldown = cooldown;
        this.size = size;
        this.type = type;
        this.color = color;
    }

    public String getName() { return name; }
    public String getIcon() { return icon; }
    public int getCost() { return cost; }
    public int getHp() { return hp; }
    public int getDamage() { return damage; }
    public float getSpeed() { return speed; }
    public float getRange() { return range; }
    public float getCooldown() { return cooldown; }
    public float getSize() { return size; }
    public String getType() { return type; }
    public UnitColor getColor() { return color; }
}
