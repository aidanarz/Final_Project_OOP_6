package com.NetRoyale;

import com.badlogic.gdx.Game;
import com.NetRoyale.screens.DeckBuilderScreen;
import com.NetRoyale.managers.RenderManager;

/**
 * Main game class - Entry point for LibGDX
 */
public class NetRoyale extends Game {
    
    @Override
    public void create() {
        // Initialize render manager
        RenderManager.getInstance();
        
        // Start with deck builder screen V2 (HTML style)
        setScreen(new DeckBuilderScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
        RenderManager.getInstance().dispose();
    }
}
