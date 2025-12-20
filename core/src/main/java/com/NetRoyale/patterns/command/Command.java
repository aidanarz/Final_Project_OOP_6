package com.NetRoyale.patterns.command;

/**
 * Command Pattern - Command Interface
 * 
 * Purpose: Encapsulate action sebagai object untuk undo/redo
 * - Memungkinkan undo operation
 * - Memungkinkan replay sequence
 */
public interface Command {
    void execute();
    void undo();
}
