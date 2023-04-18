package com.lezurex.smarthome.telegram;

import java.util.List;

public enum Property {
  MODE("mode"), BRIGHTNESS("brightness"), COLOR("color");

  private String id;

  private Property(String id) {
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

  public static Property fromId(String text) {
    if (text != null) {
      for (Property p : Property.values()) {
        if (text.equalsIgnoreCase(p.id)) {
          return p;
        }
      }
    }
    return null;
  }

  public static List<Property> asList() {
    return List.of(Property.values());
  }

}
