package org.kd.app.mamba.common;

/**
 * Created by kuldeep on 27-05-2017.
 */
public enum Env {

   DEV("dev"),PROD("prod");

   private String  name;

   Env(String name) {
      this.name = name;
   }

   public String getName() {
      return name;
   }
}
