package com.example;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class Main implements ModInitializer {
  public static final Logger LOGGER = LoggerFactory.getLogger("example");

  @Override
  public void onInitialize() {
    LOGGER.info("Hello Fabric world!");
  }

}
