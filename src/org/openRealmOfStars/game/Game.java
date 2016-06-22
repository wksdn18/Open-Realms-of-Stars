package org.openRealmOfStars.game;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.UIManager;

import org.openRealmOfStars.game.States.CreditsView;
import org.openRealmOfStars.game.States.MainMenu;
import org.openRealmOfStars.game.States.PlanetView;
import org.openRealmOfStars.gui.BlackPanel;
import org.openRealmOfStars.gui.MapPanel;
import org.openRealmOfStars.gui.buttons.SpaceButton;
import org.openRealmOfStars.gui.icons.Icons;
import org.openRealmOfStars.gui.infopanel.EmptyInfoPanel;
import org.openRealmOfStars.gui.infopanel.InfoPanel;
import org.openRealmOfStars.gui.infopanel.MapInfoPanel;
import org.openRealmOfStars.gui.labels.IconLabel;
import org.openRealmOfStars.gui.panels.InvisiblePanel;
import org.openRealmOfStars.player.PlayerInfo;
import org.openRealmOfStars.player.PlayerList;
import org.openRealmOfStars.player.SpaceRace;
import org.openRealmOfStars.starMap.StarMap;
import org.openRealmOfStars.starMap.StarMapMouseListener;
import org.openRealmOfStars.starMap.planet.Planet;

/**
 * 
 * Open Realm of Stars game project
 * Copyright (C) 2016  Tuomo Untinen
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see http://www.gnu.org/licenses/
 * 
 * 
 * Contains runnable main method which runs the Game class.
 * 
 */

public class Game extends JFrame implements ActionListener {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * Game Title show in various places
   */
  public static final String GAME_TITLE = "Open Realm of Stars";

  /**
   * Game version number
   */
  public static final String GAME_VERSION = "0.0.1Alpha";

  /**
   * Animation timer used for animation
   */
  private Timer animationTimer;

  /**
   * MapPanel for drawing the star map
   */
  private MapPanel mapPanel;

  /**
   * Infopanel next to starMap
   */
  private MapInfoPanel infoPanel;
  
  /**
   * Star map for the game
   */
  private StarMap starMap = null;
  
  /**
   * List of players
   */
  private PlayerList players;
  
  
  /**
   * Mouse listener aka mouse handler for star map.
   */
  private StarMapMouseListener starMapMouseListener;
  
  /**
   * Current Game state
   */
  public GameState gameState;
  
  
  /**
   * Planet view Panel and handling planet
   */
  public PlanetView planetView;

  /**
   * Main menu for the game
   */
  public MainMenu mainMenu;

  /**
   * Credits for the game
   */
  public CreditsView creditsView;
  
  public SpaceButton endTurnButton;
  
  /**
   * Contructor of Game class
   */
  public Game() {
    // Set look and feel match on CrossPlatform Look and feel
    try {
      UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName() );
    } catch (Exception e) {
              e.printStackTrace();
    }
    setTitle(GAME_TITLE+" "+GAME_VERSION);
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);         
    addWindowListener(new GameWindowListener());
    setSize(1024, 768);
    setLocationRelativeTo(null)    ;
    animationTimer = new Timer(75,this);
    animationTimer.setActionCommand(GameCommands.COMMAND_ANIMATION_TIMER);
    animationTimer.start();

    changeGameState(GameState.MAIN_MENU);
    
    setResizable(false);

    this.setVisible(true);

  }
  
  /**
   * Show Star Map panels
   */
  public void showStarMap() {
    BlackPanel base = new BlackPanel();
    mapPanel = new MapPanel(this);
    infoPanel = new MapInfoPanel(this);
    mapPanel.drawMap(starMap);
    starMapMouseListener = new StarMapMouseListener(starMap,mapPanel,infoPanel);
    mapPanel.addMouseListener(starMapMouseListener);
    mapPanel.addMouseMotionListener(starMapMouseListener);
    
    InfoPanel bottomPanel = new EmptyInfoPanel();
    bottomPanel.setTitle(players.getCurrentPlayerInfo().getEmpireName());
    bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
    InvisiblePanel invis = new InvisiblePanel(bottomPanel);
    invis.setLayout(new BoxLayout(invis, BoxLayout.Y_AXIS));
    IconLabel credProd = new IconLabel(invis,Icons.getIconByName(Icons.ICON_CREDIT), 
        ": "+players.getCurrentPlayerInfo().getTotalCredits()+
        "("+starMap.getTotalProductionByPlayerPerTurn(Planet.PRODUCTION_CREDITS,
            players.getCurrentPlayer())+")");
    invis.add(credProd);
    IconLabel reseProd = new IconLabel(invis,Icons.getIconByName(Icons.ICON_RESEARCH), 
        ": "+starMap.getTotalProductionByPlayerPerTurn(Planet.PRODUCTION_RESEARCH,
            players.getCurrentPlayer()));
    invis.add(reseProd);
    bottomPanel.add(invis);

    endTurnButton = new SpaceButton("End Turn "+starMap.getTurn(), GameCommands.COMMAND_END_TURN);
    endTurnButton.addActionListener(this);
    bottomPanel.add(endTurnButton);
    base.setLayout(new BorderLayout());
    base.add(mapPanel,BorderLayout.CENTER);
    base.add(infoPanel, BorderLayout.EAST);
    base.add(bottomPanel, BorderLayout.SOUTH);
    
    this.getContentPane().removeAll();
    this.add(base);
    this.validate();
  }

  /**
   * Show planet view panel
   * @param planet Planet to show
   */
  public void showPlanetView(Planet planet) {
    planetView = new PlanetView(planet, this);
    this.getContentPane().removeAll();
    this.add(planetView);
    this.validate();
  }

  /**
   * Show main menu panel
   */
  public void showMainMenu() {
    mainMenu = new MainMenu(this);
    this.getContentPane().removeAll();
    this.add(mainMenu);
    this.validate();
  }

  /**
   * Show credits panel
   */
  public void showCredits() {
    try {
      creditsView = new CreditsView(this, GAME_TITLE, GAME_VERSION);
    } catch (IOException e) {
      System.out.println("Could not show credits: "+e.getMessage());
      System.exit(0);
    }
    this.getContentPane().removeAll();
    this.add(creditsView);
    this.validate();
  }

  /**
   * Change game state and show new panel/screen
   * @param newState Game State where to change
   */
  public void changeGameState(GameState newState) {
    gameState = newState;
    switch (gameState) {
    case MAIN_MENU: showMainMenu(); break;
    case NEW_GAME: { 
      players = new PlayerList();
      for (int i=0;i<PlayerList.MAX_PLAYERS;i++) {
        PlayerInfo info = new PlayerInfo();
        info.setRace(SpaceRace.getRandomRace());
        info.setEmpireName("Empire of "+info.getRace().getName());
        players.addPlayer(info);
      }
      starMap = new StarMap(75, 75,players);
      players.setCurrentPlayer(0);
      changeGameState(GameState.STARMAP);
      break;
    }
    case CREDITS: showCredits(); break;
    case STARMAP: showStarMap(); break;
    case PLANETVIEW: { 
      if (starMapMouseListener.getLastClickedPlanet()!=null) {
       showPlanetView(starMapMouseListener.getLastClickedPlanet());
       break;
      }
    }
    }
  }
  
  /**
   * Main method to run the game
   * @param args from Command line
   */
  public static void main(String[] args) {
    new Game();

  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    if (arg0.getActionCommand().equalsIgnoreCase(
        GameCommands.COMMAND_ANIMATION_TIMER) && gameState == GameState.STARMAP) {
      if (starMapMouseListener != null) {
        starMapMouseListener.updateScrollingIfOnBorder();
      }
      mapPanel.drawMap(starMap);
      mapPanel.repaint();
    }
    if (gameState == GameState.CREDITS) {
      if (arg0.getActionCommand().equalsIgnoreCase(
          GameCommands.COMMAND_ANIMATION_TIMER)) {
        creditsView.updateTextArea();
      }
      if (arg0.getActionCommand().equalsIgnoreCase(
          GameCommands.COMMAND_OK)) {
        changeGameState(GameState.MAIN_MENU);
      }
      return;
    }
    if (arg0.getActionCommand().equalsIgnoreCase(
        GameCommands.COMMAND_VIEW_PLANET) &&
        starMapMouseListener.getLastClickedPlanet() != null) {
      changeGameState(GameState.PLANETVIEW);
    }
    if (arg0.getActionCommand().equalsIgnoreCase(
        GameCommands.COMMAND_VIEW_STARMAP)) {
      changeGameState(GameState.STARMAP);
    }
    if (gameState == GameState.STARMAP) {
      // Starmap
      if (arg0.getActionCommand().equalsIgnoreCase(GameCommands.COMMAND_END_TURN)) {
        starMap.updateStarMapToNextTurn();
        endTurnButton.setText("End turn "+starMap.getTurn());
      }
      
    }
    if (gameState == GameState.PLANETVIEW) {
      // Planet view
      planetView.handleAction(arg0);
    }
    if (gameState == GameState.MAIN_MENU) {
      // Main menu
      if (arg0.getActionCommand().equalsIgnoreCase(GameCommands.COMMAND_NEW_GAME)) {
        changeGameState(GameState.NEW_GAME);
      }
      if (arg0.getActionCommand().equalsIgnoreCase(GameCommands.COMMAND_CREDITS)) {
        changeGameState(GameState.CREDITS);
      }
      if (arg0.getActionCommand().equalsIgnoreCase(GameCommands.COMMAND_QUIT_GAME)) {
        System.exit(0);
      }
    }
  }

}
