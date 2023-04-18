package com.lezurex.smarthome.telegram;

import java.util.List;

public enum Mode {
  OFF("off"), ON("on"), FLASHING("flashing"), PARTY("party");

  private String id;

  private Mode(String id) {
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

  public static Mode fromId(String text) {
    if (text != null) {
      for (Mode m : Mode.values()) {
        if (text.equalsIgnoreCase(m.id)) {
          return m;
        }
      }
    }
    return null;
  }

  public static List<Mode> asList() {
    return List.of(Mode.values());
  }
}
