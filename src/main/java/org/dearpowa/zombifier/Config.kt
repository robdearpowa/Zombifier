package org.dearpowa.zombifier

import org.bukkit.plugin.java.JavaPlugin

data class Config(var enabled: Boolean = false, var hardMode: Boolean = false) {
    fun load(plugin: JavaPlugin) {
        val logger = plugin.logger
        val config = plugin.config

        config.apply {
            logger.info("Loading config...")
            load(configPath)
            enabled = getBoolean("enabled", true)
            hardMode = getBoolean("hardMode", false)
            logger.info("Config loaded!")
        }
    }

    fun save(plugin: JavaPlugin) {
        val logger = plugin.logger
        val config = plugin.config

        config.apply {
            logger.info("Saving config...")
            set("enabled", enabled)
            set("hardMode", hardMode)
            save(configPath)
            logger.info("Config saved!")
        }
    }

    companion object {
        const val configPath = "./plugins/Zombifier"
    }
}