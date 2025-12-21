package com.NetRoyale.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import java.util.HashMap;
import java.util.Map;

/**
 * Tile Manager (Singleton Pattern)
 * Manages isometric tile rendering like Clash Royale
 */
public class TileManager {
    private static TileManager instance;
    
    private Map<String, Texture> tileTextures;
    private int[][] tileMap;
    
    // Isometric tile configuration
    private static final int TILE_WIDTH = 32;
    private static final int TILE_HEIGHT = 32;
    private static final float TILE_SCALE = 2.0f; // Scale untuk lebih jelas
    
    // Game grid dimensions (Clash Royale style: 18x32 tiles)
    private static final int GRID_ROWS = 15;    // Vertical tiles
    private static final int GRID_COLS = 8;     // Horizontal tiles
    
    private TileManager() {
        tileTextures = new HashMap<>();
        loadTileTextures();
        initializeTileMap();
    }
    
    public static TileManager getInstance() {
        if (instance == null) {
            instance = new TileManager();
        }
        return instance;
    }
    
    /**
     * Load tile textures from "1 Tiles" folder
     */
    private void loadTileTextures() {
        try {
            // Load tiles sesuai request: FieldsTile_38 untuk rumput, 31 untuk archer-bridge, 32 untuk king-archer
            tileTextures.put("grass", new Texture(Gdx.files.internal("1 Tiles/FieldsTile_38.png")));
            tileTextures.put("path1", new Texture(Gdx.files.internal("1 Tiles/FieldsTile_31.png")));
            tileTextures.put("path2", new Texture(Gdx.files.internal("1 Tiles/FieldsTile_32.png")));
            
            System.out.println("Tile textures loaded: " + tileTextures.size() + " tiles");
        } catch (Exception e) {
            System.err.println("Failed to load tile textures: " + e.getMessage());
        }
    }
    
    /**
     * Initialize tile map with paths from archer towers to bridge
     * Pattern: Path dari archer tower -> bridge -> archer tower
     */
    private void initializeTileMap() {
        tileMap = new int[GRID_ROWS][GRID_COLS];
        
        // Initialize semua dengan grass dulu
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                tileMap[row][col] = 0; // grass
            }
        }
        
        // Path vertikal tengah (bridge area) - dari atas ke bawah
        for (int row = 0; row < GRID_ROWS; row++) {
            tileMap[row][3] = (row % 2 == 0) ? 1 : 2; // alternating path tiles
            tileMap[row][4] = (row % 2 == 0) ? 2 : 1;
        }
        
        // Path horizontal bawah: dari kiri ke bridge (archer tower bawah player ke bridge)
        // Row sekitar 2-3 (archer tower y=100)
        for (int col = 1; col <= 3; col++) {
            tileMap[2][col] = 1;
            tileMap[3][col] = 2;
        }
        
        // Path horizontal atas: dari kiri ke bridge (archer tower atas player ke bridge)  
        // Row sekitar 11-12 (archer tower y=380)
        for (int col = 1; col <= 3; col++) {
            tileMap[11][col] = 2;
            tileMap[12][col] = 1;
        }
        
        // Path horizontal bawah: dari bridge ke kanan (bridge ke archer tower bawah enemy)
        for (int col = 4; col < 7; col++) {
            tileMap[2][col] = 2;
            tileMap[3][col] = 1;
        }
        
        // Path horizontal atas: dari bridge ke kanan (bridge ke archer tower atas enemy)
        for (int col = 4; col < 7; col++) {
            tileMap[11][col] = 1;
            tileMap[12][col] = 2;
        }
    }
    
    /**
     * Get tile texture by tile ID
     */
    private Texture getTileTexture(int tileId) {
        switch (tileId) {
            case 0: return tileTextures.get("grass");   // FieldsTile_38
            case 1: return tileTextures.get("path1");   // FieldsTile_18
            case 2: return tileTextures.get("path2");   // FieldsTile_20
            default: return tileTextures.get("grass");
        }
    }
    
    /**
     * Convert grid coordinates to isometric screen position
     * Isometric formula:
     * screenX = (gridX - gridY) * tileWidth/2
     * screenY = (gridX + gridY) * tileHeight/2
     */
    private Vector2 gridToIsometric(int row, int col, float offsetX, float offsetY) {
        float scaledWidth = TILE_WIDTH * TILE_SCALE;
        float scaledHeight = TILE_HEIGHT * TILE_SCALE;
        
        // Isometric projection (diamond orientation seperti Clash Royale)
        float screenX = (col - row) * (scaledWidth / 2) + offsetX;
        float screenY = (col + row) * (scaledHeight / 4) + offsetY; // Divide by 4 untuk flatten sedikit
        
        return new Vector2(screenX, screenY);
    }
    
    /**
     * Render all tiles in isometric view
     */
    public void render(SpriteBatch batch, float gameWidth, float gameHeight) {
        // Calculate offset to center the tile grid
        float offsetX = gameWidth / 2;
        float offsetY = 50; // Start from bottom
        
        float scaledWidth = TILE_WIDTH * TILE_SCALE;
        float scaledHeight = TILE_HEIGHT * TILE_SCALE;
        
        // Render tiles back to front (row by row, then column)
        // untuk proper depth sorting dalam isometric view
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int tileId = tileMap[row][col];
                Texture tile = getTileTexture(tileId);
                
                if (tile != null) {
                    Vector2 pos = gridToIsometric(row, col, offsetX, offsetY);
                    
                    // Draw tile dengan scale
                    batch.draw(tile, 
                              pos.x - scaledWidth / 2, 
                              pos.y, 
                              scaledWidth, 
                              scaledHeight);
                }
            }
        }
    }
    
    /**
     * Render all tiles with default dimensions (850x480)
     */
    public void render(SpriteBatch batch) {
        render(batch, 850f, 480f);
    }
    
    /**
     * Get grid dimensions
     */
    public int getGridRows() {
        return GRID_ROWS;
    }
    
    public int getGridCols() {
        return GRID_COLS;
    }
    
    /**
     * Convert world position to grid coordinates (for unit placement)
     */
    public Vector2 worldToGrid(float worldX, float worldY, float gameWidth, float gameHeight) {
        float offsetX = gameWidth / 2;
        float offsetY = 50;
        
        float scaledWidth = TILE_WIDTH * TILE_SCALE;
        float scaledHeight = TILE_HEIGHT * TILE_SCALE;
        
        // Inverse isometric transformation
        float relX = worldX - offsetX;
        float relY = worldY - offsetY;
        
        // Solve for row and col
        float col = (relX / (scaledWidth / 2) + relY / (scaledHeight / 4)) / 2;
        float row = (relY / (scaledHeight / 4) - relX / (scaledWidth / 2)) / 2;
        
        return new Vector2((int)row, (int)col);
    }
    
    /**
     * Get tile texture by name
     */
    public Texture getTileTexture(String name) {
        return tileTextures.get(name);
    }
    
    /**
     * Dispose all textures
     */
    public void dispose() {
        for (Texture texture : tileTextures.values()) {
            texture.dispose();
        }
        tileTextures.clear();
    }
}

