package com.NetRoyale.patterns.command;

import com.badlogic.gdx.utils.Array;

/**
 * Command Pattern - CommandHistory
 * 
 * Purpose: Menyimpan history command untuk undo/redo/replay
 * - Undo: Batalkan command terakhir
 * - Redo: Ulangi command yang di-undo
 * - Replay: Execute ulang semua command
 */
public class CommandHistory {
    private Array<Command> history;
    private Array<Command> undoneCommands;
    
    public CommandHistory() {
        history = new Array<>();
        undoneCommands = new Array<>();
    }
    
    public void executeCommand(Command command) {
        command.execute();
        history.add(command);
        undoneCommands.clear(); // Clear redo stack
    }
    
    public void undo() {
        if (history.size > 0) {
            Command lastCommand = history.pop();
            lastCommand.undo();
            undoneCommands.add(lastCommand);
        }
    }
    
    public void redo() {
        if (undoneCommands.size > 0) {
            Command command = undoneCommands.pop();
            command.execute();
            history.add(command);
        }
    }
    
    public void replay() {
        // Store current history
        Array<Command> commandsToReplay = new Array<>(history);
        
        // Clear and re-execute all
        clear();
        for (Command cmd : commandsToReplay) {
            executeCommand(cmd);
        }
    }
    
    public void clear() {
        history.clear();
        undoneCommands.clear();
    }
    
    public int getHistorySize() {
        return history.size;
    }
    
    public boolean canUndo() {
        return history.size > 0;
    }
    
    public boolean canRedo() {
        return undoneCommands.size > 0;
    }
}
