package com.NetRoyale.patterns;

/**
 * Command Pattern - Interface for all commands
 */
public interface Command {
    void execute();
    void undo();
    String getDescription();
}
