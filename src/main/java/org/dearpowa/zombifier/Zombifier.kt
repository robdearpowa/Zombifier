package org.dearpowa.zombifier

import org.bukkit.plugin.java.JavaPlugin

class Zombifier : JavaPlugin() {
    override fun onEnable() {
        // Plugin startup logic
        logger.info("Hello World from Zombifier!")
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
