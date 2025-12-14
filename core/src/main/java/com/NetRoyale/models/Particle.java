package com.NetRoyale.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

// Particle efek visual
public class Particle {
    private Vector2 position;
    private Vector2 velocity;
    private Color color;
    private float life;

    public Particle(float x, float y, Color color) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(
            (float) (Math.random() - 0.5) * 100,
            (float) (Math.random() - 0.5) * 100
        );
        this.color = color;
        this.life = 0.4f;
    }

    public void update(float delta) {
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
        life -= delta;
    }

    public boolean isAlive() {
        return life > 0;
    }

    public Vector2 getPosition() { return position; }
    public Color getColor() { return color; }
    public float getLife() { return life; }
}
