package org.openRealmOfStars.game;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;

import org.openRealmOfStars.player.SpaceRace;
import org.openRealmOfStars.starMap.StarMap;

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
 * Saved game information
 * 
 */
public class SavedGame {

  /**
   * Player's race
   */
  private SpaceRace playerRace;
  
  /**
   * Which turn 
   */
  private int turnNumber;
  
  /**
   * Galaxy size as string
   */
  private String galaxySize;
  
  /**
   * Filename of saved game file
   */
  private String filename;

  /**
   * First player's empire name
   */
  private String empireName;
  
  /**
   * File's creation time
   */
  private String creationTime;
  /**
   * Load game from certain file name and get all information from saved
   * game
   * @param filename File name
   * @throws IOException if reading fails
   */
  public SavedGame(String filename) throws IOException {
    String folderName = "saves";
    File file = new File(folderName+"/"+filename);
    BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    creationTime = dateFormat.format(attr.creationTime().toMillis());
    try (FileInputStream is = new FileInputStream(file);){
      
      BufferedInputStream bis = new BufferedInputStream(is);
      DataInputStream dis = new DataInputStream(bis);
      StarMap starMap = new StarMap(dis);
      this.filename = filename;
      turnNumber = starMap.getTurn();
      galaxySize = starMap.getMaxX()+" X "+starMap.getMaxY();
      playerRace = starMap.getPlayerList().getPlayerInfoByIndex(0).getRace();
      empireName = starMap.getPlayerList().getPlayerInfoByIndex(0).getEmpireName();
    } catch (IOException e) {
      System.out.println(e.getMessage());
      throw e;
    }
  }
  
  public SpaceRace getPlayerRace() {
    return playerRace;
  }

  public int getTurnNumber() {
    return turnNumber;
  }

  public String getGalaxySize() {
    return galaxySize;
  }

  public String getFilename() {
    return filename;
  }
  
  public String getEmpireName() {
    return empireName;
  }
  
  /**
   * Get save file's creation time
   */
  public String getTime() {
    return creationTime;
  }
  
}