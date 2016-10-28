package org.openRealmOfStars.game.States;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;

import org.openRealmOfStars.AI.Mission.Mission;
import org.openRealmOfStars.AI.Mission.MissionHandling;
import org.openRealmOfStars.AI.Mission.MissionPhase;
import org.openRealmOfStars.AI.Mission.MissionType;
import org.openRealmOfStars.game.Game;
import org.openRealmOfStars.game.GameCommands;
import org.openRealmOfStars.game.GameState;
import org.openRealmOfStars.gui.icons.Icons;
import org.openRealmOfStars.gui.labels.TransparentLabel;
import org.openRealmOfStars.gui.panels.BigImagePanel;
import org.openRealmOfStars.gui.panels.BlackPanel;
import org.openRealmOfStars.gui.panels.InvisiblePanel;
import org.openRealmOfStars.player.PlayerInfo;
import org.openRealmOfStars.player.fleet.Fleet;
import org.openRealmOfStars.player.message.Message;
import org.openRealmOfStars.player.message.MessageType;
import org.openRealmOfStars.player.ship.Ship;
import org.openRealmOfStars.starMap.Route;
import org.openRealmOfStars.starMap.Sun;
import org.openRealmOfStars.starMap.planet.Planet;
import org.openRealmOfStars.utilities.DiceGenerator;

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
 * AI turn view for Open Realm of Stars.
 * This class also handles what to do before ending the turn.
 * So all planet resources, scanning and research are done here.
 * 
 */
public class AITurnView extends BlackPanel {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  /**
   * Text for showing human player
   */
  private TransparentLabel label;
  
  /**
   * Reference to the game
   */
  private Game game;
  
  /**
   * Text animation
   */
  private int textAnim; 
  
  /**
   * Target coordinate for mission
   */
  private int cx;
  /**
   * Target coordinate for mission
   */
  private int cy;

  /**
   * Constructor for main menu
   * @param game Game used to get access starmap and planet lists
   * @param listener ActionListener
   */
  public AITurnView(Game game, ActionListener listener) {
    this.game = game;
    Planet planet = new Planet(1, 1, "Random Planet",1, false);
    if (DiceGenerator.getRandom(100)<10) {
      planet.setPlanetImageIndex(DiceGenerator.getRandom(1));
      planet.setGasGiant(true);
    } else {
      planet.setPlanetType(DiceGenerator.getRandom(Planet.PLANET_IMAGE_INDEX.length-1));
    }
    // Background image
    BigImagePanel imgBase = new BigImagePanel(planet, true,"Enemy turn");
    this.setLayout(new BorderLayout());
    
    InvisiblePanel invis = new InvisiblePanel(imgBase);    
    invis.setLayout(new BoxLayout(invis, BoxLayout.Y_AXIS));
    invis.add(Box.createRigidArea(new Dimension(500, 500)));

    label = new TransparentLabel(invis, "Please wait.....");
    label.setAlignmentX(JComponent.CENTER_ALIGNMENT);

    invis.add(label);
    imgBase.add(invis);
    this.add(imgBase,BorderLayout.CENTER);
    textAnim = 0;

    
  }
  
  public void setText(String text) {
    label.setText(text);
  }
  
  public void updateText() {
    switch (textAnim) {
    case 0: setText("Please wait....."); break;
    case 1: setText("Please wait*...."); break;
    case 2: setText("Please wait.*..."); break;
    case 3: setText("Please wait..*.."); break;
    case 4: setText("Please wait...*."); break;
    case 5: setText("Please wait....*"); break;
    }
    textAnim++;
    if (textAnim > 5) {
      textAnim = 0;
    }
  }

  
  
  /**
   * Handle missions for AI player and single fleet
   * @param fleet Fleet for doing the missing
   * @param info PlayerInfo
   */
  private void handleMissions(Fleet fleet, PlayerInfo info) {
    Mission mission = info.getMissions().getMissionForFleet(fleet.getName());
    if (mission != null) {
      switch (mission.getType()) {
      case COLONIZE: MissionHandling.handleColonize(mission, fleet, info, game); break;
      case EXPLORE: MissionHandling.handleExploring(mission, fleet, info, game); break;
      case DEFEND: MissionHandling.handleDefend(mission, fleet, info, game); break;
      case ATTACK: MissionHandling.handleAttack(mission, fleet, info, game);break;
      }
    } else {
      // No mission for fleet yet
      if (fleet.isScoutFleet()) {
        // Scout fleet should go to explore
        Sun sun = game.getStarMap().getNearestSolarSystem(fleet.getX(), 
            fleet.getY(),info,fleet,null);
        mission = new Mission(MissionType.EXPLORE, 
            MissionPhase.TREKKING, sun.getCenterX(), sun.getCenterY());
        mission.setFleetName(fleet.getName());
        mission.setSunName(sun.getName());
        info.getMissions().add(mission);
        fleet.setRoute(new Route(fleet.getX(), fleet.getY(), 
            mission.getX(), mission.getY(), fleet.getFleetFtlSpeed()));
      }
      if (fleet.isColonyFleet()) {
        mission = info.getMissions().getMission(MissionType.COLONIZE, MissionPhase.PLANNING);
        if (mission != null) {
          mission.setPhase(MissionPhase.LOADING);
          mission.setFleetName(fleet.getName());
          Planet planet = game.getStarMap().getPlanetByCoordinate(fleet.getX(), fleet.getY());
          if (planet.getPlanetPlayerInfo() == info) {
            Ship[] ships = fleet.getShips();
            int colony = 0; 
            for (int i=0;i<ships.length;i++) {
              if (ships[i].isColonyModule()) {
                colony = i;
                break;
              }
            }
            if (planet.getTotalPopulation() > 2 && planet.takeColonist() && ships[colony].getFreeCargoColonists() > 0) {
              ships[colony].setColonist(ships[colony].getColonist()+1);
              mission.setPhase(MissionPhase.TREKKING);
              Route route = new Route(fleet.getX(), fleet.getY(), mission.getX(),
                  mission.getY(), fleet.getFleetFtlSpeed());
              fleet.setRoute(route);
            }
            if (planet.getTotalPopulation() > 3 && planet.takeColonist() && ships[colony].getFreeCargoColonists() > 0) {
              ships[colony].setColonist(ships[colony].getColonist()+1);
            }
          }
        }
      }
    }
    
  }



  /**
   * Calculate Attack rendevue sector coordinate
   * @param info Player who is going to attack
   * @param x Target planet x coordinate
   * @param y Target planet y coordinate
   */
  public void calculateAttackRendevuezSector(PlayerInfo info,int x, int y) {
    int amount = 1;
    cx = x;
    cy = y;
    for (Planet planet :game.getStarMap().getPlanetList()) {
      if (planet.getPlanetPlayerInfo() == info) {
        cx = cx +planet.getX();
        cy = cy +planet.getY();
        if (planet.getHomeWorldIndex() != -1) {
          // Double the effect of homeworld
          cx = cx +planet.getX();
          cy = cy +planet.getY();
        }
        amount++;
      }
    }
    cx = cx /amount;
    cy = cy /amount;
    if (game.getStarMap().isBlocked(cx, cy)) {
      int loop = 100;
      while (loop>0) {
        int ax = DiceGenerator.getRandom(-5, 5);
        int ay = DiceGenerator.getRandom(-5, 5);
        loop--;
        if (!game.getStarMap().isBlocked(cx+ax, cy+ay)) {
          loop = 0;
          cx = cx +ax;
          cy = cy +ay;
        }
        
      }
    }
  }
  
  /**
   * Search newly found uncolonized planets
   */
  public void searchForColonizablePlanets() {
    PlayerInfo info = game.players.getPlayerInfoByIndex(game.getStarMap()
        .getAiTurnNumber());
    if (info != null && !info.isHuman()) {
      ArrayList<Planet> planets = game.getStarMap().getPlanetList();
      for (Planet planet : planets) {
        if (planet.getRadiationLevel() <= info.getRace().getMaxRad()
            && planet.getPlanetPlayerInfo() == null && !planet.isGasGiant()
            && info.getSectorVisibility(planet.getX(), planet.getY())==PlayerInfo.VISIBLE) {
          // New planet to colonize, adding it to mission list
          Mission mission = new Mission(MissionType.COLONIZE, 
              MissionPhase.PLANNING, planet.getX(), planet.getY());
          if (info.getMissions().getColonizeMission(mission.getX(), mission.getY())==null) {
            // No colonize mission for this planet found, so adding it.
            info.getMissions().add(mission);
          }
          
        }
        if (planet.getRadiationLevel() <= info.getRace().getMaxRad()
            && planet.getPlanetPlayerInfo() != null 
            && planet.getPlanetPlayerInfo() != info && !planet.isGasGiant()
            && info.getSectorVisibility(planet.getX(), planet.getY())==PlayerInfo.VISIBLE) {
          // New planet to conquer, adding it to mission list
          Mission mission = new Mission(MissionType.ATTACK, 
              MissionPhase.PLANNING, planet.getX(), planet.getY());
          calculateAttackRendevuezSector(info, planet.getX(), planet.getY());
          mission.setTarget(cx, cy);
          mission.setTargetPlanet(planet.getName());
          if (info.getMissions().getAttackMission(planet.getName())==null) {
            // No colonize mission for this planet found, so adding it.
            info.getMissions().add(mission);
          }
          
        }

      }
    }

  }
  
  /**
   * Handle single AI Fleet. If fleet was last then increase AI turn number
   * and set aiFleet to null.
   */
  public void handleAIFleet() {
    PlayerInfo info = game.players.getPlayerInfoByIndex(game.getStarMap()
        .getAiTurnNumber());
    if (info != null && !info.isHuman() && game.getStarMap().getAIFleet() != null) {
      // Handle fleet
      
      MissionHandling.mergeFleets(game.getStarMap().getAIFleet(), info);
      handleMissions(game.getStarMap().getAIFleet(), info);
      game.getStarMap().setAIFleet(info.Fleets().getNext());
      if (info.Fleets().getIndex()==0) {
        // All fleets have moved. Checking the new possible planet
        searchForColonizablePlanets();
        game.getStarMap().setAIFleet(null);
        game.getStarMap().setAiTurnNumber(game.getStarMap().getAiTurnNumber()+1);
      }
      
    }

  }

  /**
   * Update whole star map to next turn
   */
  public void updateStarMapToNextTurn() {
    game.getStarMap().resetCulture();
    for (int i=0;i<game.players.getCurrentMaxPlayers();i++) {
      PlayerInfo info = game.players.getPlayerInfoByIndex(i);
      if (info != null) {
        info.resetVisibilityDataAfterTurn();
        info.getMsgList().clearMessages();
        for (int j = 0;j<info.Fleets().getNumberOfFleets();j++) {
          Fleet fleet = info.Fleets().getByIndex(j);
          if (fleet.getRoute() != null) {
            if (fleet.movesLeft > 0) {
              // Make sure fleet can actually move
              fleet.getRoute().makeNextMove();
              if (!game.getStarMap().isBlocked(fleet.getRoute().getX(), 
                  fleet.getRoute().getY())) {
                // Not blocked so fleet is moving
                game.fleetMakeMove(info, fleet, fleet.getRoute().getX(), fleet.getRoute().getY());
                if (fleet.getRoute().isEndReached()) {
                  // End is reached giving a message
                  fleet.setRoute(null);
                  Message msg = new Message(MessageType.FLEET,
                    fleet.getName()+" has reached it's target and waiting for orders.",
                    Icons.getIconByName(Icons.ICON_HULL_TECH));
                  msg.setMatchByString(fleet.getName());
                  msg.setCoordinate(fleet.getX(), fleet.getY());
                  info.getMsgList().addNewMessage(msg);
                }
              } else {
                // Movement was blocked, giving a message
                fleet.setRoute(null);
                Message msg = new Message(MessageType.FLEET,
                  fleet.getName()+" has encouter obstacle and waiting for more orders.",
                  Icons.getIconByName(Icons.ICON_HULL_TECH));
                msg.setMatchByString(fleet.getName());
                msg.setCoordinate(fleet.getX(), fleet.getY());
                info.getMsgList().addNewMessage(msg);
              }
            }
            
          } else {
            Message msg = new Message(MessageType.FLEET,
                fleet.getName()+" is waiting for orders.",
                Icons.getIconByName(Icons.ICON_HULL_TECH));
            msg.setMatchByString(fleet.getName());
            msg.setCoordinate(fleet.getX(), fleet.getY());
            info.getMsgList().addNewMessage(msg);
          }
          fleet.movesLeft = fleet.getFleetSpeed();
          game.getStarMap().doFleetScanUpdate(info, fleet,null);
        }
      }
    }
    for (int i=0;i<game.getStarMap().getPlanetList().size();i++) {
      Planet planet = game.getStarMap().getPlanetList().get(i);
      if (planet.getPlanetPlayerInfo() != null) {
        PlayerInfo info = planet.getPlanetPlayerInfo();
        // Update each planet one by one
        planet.updateOneTurn();
        int index =game.players.getIndex(info);
        if (index > -1) {
          // Recalculate culture for the map for each player
          game.getStarMap().calculateCulture(planet.getX(), planet.getY(), 
              planet.getCulture(), index);
        }
        // Fleets and planet do the scan
        game.getStarMap().doFleetScanUpdate(info, null,planet);
      }
    }
    game.getStarMap().setTurn(game.getStarMap().getTurn()+1);
  }

  
  /**
   * Handle actions for AI Turn view
   * Since AI Turn View can be null while handling the all the AI. AI handling
   * variables are stored in StarMap. These variables are AIFleet and AITurnNumber.
   * @param arg0 ActionEvent
   */
  public void handleActions(ActionEvent arg0) {
    if (arg0.getActionCommand().equalsIgnoreCase(
        GameCommands.COMMAND_ANIMATION_TIMER) ) {
      updateText();
      // Just update fleet tiles.
      game.getStarMap().getFleetTiles();
      if (game.getStarMap().getAIFleet() == null) {
        game.getStarMap().handleAIResearchAndPlanets();
      } else {
        handleAIFleet();
      }
      if (game.getStarMap().isAllAIsHandled()) {
        updateStarMapToNextTurn();
        for (int i=0;i<game.players.getCurrentMaxPlayers();i++) {
          // Handle player research at end of turn
          PlayerInfo info = game.players.getPlayerInfoByIndex(i);
          info.getTechList().updateResearchPointByTurn(game.getStarMap().
             getTotalProductionByPlayerPerTurn(Planet.PRODUCTION_RESEARCH, i),info);
        }
        game.getStarMap().clearAITurn();
        game.changeGameState(GameState.STARMAP);

      }
    }

  }


}