package com.NetRoyale.patterns;

import java.util.ArrayList;
import java.util.List;

/**
 * Command Pattern - Command Invoker
 * Manages command history for undo and replay
 */
public class CommandHistory {
    private static CommandHistory instance;
    
    private List<Command> history;
    private int currentIndex;
    
    private CommandHistory() {
        history = new ArrayList<>();
        currentIndex = -1;
    }
    
    public static CommandHistory getInstance() {
        if (instance == null) {
            instance = new CommandHistory();
        }
        return instance;
    }
    
    /**
     * Execute command and add to history
     */
    public void executeCommand(Command command) {
        // Remove any commands after current index (for redo support)
        while (history.size() > currentIndex + 1) {
            history.remove(history.size() - 1);
        }
        
        command.execute();
        history.add(command);
        currentIndex++;
    }
    
    /**
     * Undo last command
     */
    public void undo() {
        if (currentIndex < 0) return;
        
        Command command = history.get(currentIndex);
        command.undo();
        currentIndex--;
    }
    
    /**
     * Redo command
     */
    public void redo() {
        if (currentIndex >= history.size() - 1) return;
        
        currentIndex++;
        Command command = history.get(currentIndex);
        command.execute();
    }
    
    /**
     * Replay all commands from beginning
     */
    public void replay() {
        // Undo all
        while (currentIndex >= 0) {
            undo();
        }
        
        // Execute all again
        for (Command command : history) {
            command.execute();
            currentIndex++;
        }
    }
    
    /**
     * Get command history for display
     */
    public List<String> getHistoryDescriptions() {
        List<String> descriptions = new ArrayList<>();
        for (Command cmd : history) {
            descriptions.add(cmd.getDescription());
        }
        return descriptions;
    }
    
    /**
     * Clear history
     */
    public void clear() {
        history.clear();
        currentIndex = -1;
    }
    
    public boolean canUndo() {
        return currentIndex >= 0;
    }
    
    public boolean canRedo() {
        return currentIndex < history.size() - 1;
    }
    
    public int getHistorySize() {
        return history.size();
    }
}
