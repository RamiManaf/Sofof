/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sofof.bean;

import org.sofof.Session;
import org.sofof.SofofException;
import org.sofof.annotation.Particle;
import org.sofof.command.Capture;
import java.io.Serializable;

/**
 *
 * @author LENOVO PC
 */
public class Game implements Serializable {

    private static final long serialVersionUID = 87234702347l;
    
    @Particle
    private int score = 0;
    @Particle
    private String playerName;

    public Game() {
    }
    
    public Game(String playerName) {
        this.playerName = playerName;
    }

    public void killEnemy() {
        this.score++;
    }

    public int getScore() {
        return score;
    }

    public String getPlayerName() {
        return playerName;
    }
    
    public static Game loadGame(Session session) throws SofofException{
        Game game = new Game();
        Capture.load(session, Game.class).copyTo(game);
        return game;
    }
    
    public void saveGame(Session session) throws SofofException{
        Capture.capture(session, this);
    }
}
