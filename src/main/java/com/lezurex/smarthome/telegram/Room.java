package com.lezurex.smarthome.telegram;

import java.util.List;

public enum Room {
  LIVINGROOM("livingroom"), HALLWAY("hallway"), KITCHEN("kitchen"), BEDROOM("bedroom"), WARDROBE(
      "wardrobe");

  private String id;

  private Room(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public static List<String> getIds() {
    String[] ids = new String[Room.values().length];
    for (int i = 0; i < Room.values().length; i++) {
      ids[i] = Room.values()[i].getId();
    }
    return List.of(ids);
  }

  public static boolean isValid(String room) {
    for (Room r : Room.values()) {
      if (r.getId().equals(room)) {
        return true;
      }
    }
    return false;
  }
  
  public static Room fromId(String text) {
    if (text != null) {
      for (Room r : Room.values()) {
        if (text.equalsIgnoreCase(r.id)) {
          return r;
        }
      }
    }
    return null;
  }

  public static List<Room> asList() {
    return List.of(Room.values());
  }
}
