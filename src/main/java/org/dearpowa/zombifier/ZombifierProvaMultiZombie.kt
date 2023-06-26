package org.dearpowa.zombifier

import org.bukkit.block.Biome
import org.bukkit.entity.EntityType
import org.bukkit.entity.Husk
import org.bukkit.entity.Zombie
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.plugin.java.JavaPlugin

class Zombifier : JavaPlugin(), Listener {

    val undeadList = mutableListOf<UndeadInventory>()

    override fun onEnable() {
        // Plugin startup logic
        logger.info("Hello World from Zombifier!")
        UndeadInventory.loadAll(this)
        server.pluginManager.registerEvents(this, this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
        UndeadInventory.saveAll(this)
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.player

        val biome = playerLocation.block.biome

        val zombieType = when (biome) {
            Biome.DESERT, Biome.DESERT_HILLS -> EntityType.HUSK // Zombie del deserto (Husk)
            Biome.OCEAN, Biome.DEEP_OCEAN -> EntityType.DROWNED // Zombie affogato (Drowned)
            else -> EntityType.ZOMBIE // Zombie normale
        }

        val undead = player.world.spawnEntity(player.location, zombieType)

        if (undead is Zombie || undead is Drowned || undead is Husk) {

            val mainHand = player.inventory.itemInMainHand
            val offHand = player.inventory.itemInOffHand
            val helmet = player.inventory.helmet
            val chestplate = player.inventory.chestplate
            val leggings = player.inventory.leggings
            val boots = player.inventory.boots


            // Copia l'inventario del giocatore all'entità zombie
            undead.equipment.setItemInMainHand(mainHand)
            undead.equipment.setItemInOffHand(offHand)
            undead.equipment.helmet = helmet
            undead.equipment.chestplate = chestplate
            undead.equipment.leggings = leggings
            undead.equipment.boots = boots

            // Cambio nome allo zombie
            undead.customName(player.name())
            undead.isCustomNameVisible = true

            // Prevengo il despawn dello zombie
            undead.removeWhenFarAway = false

            // Imposto lo zombie sempre adulto
            undead.setAdult()

            //Creo l'invetario dello zombie
            val undeadInventory = UndeadInventory(undead.uniqueId, this, 54)

            //Copio l'inventario del player nello zombie
            for (item in player.inventory) {
                if (item == null) continue
                undeadInventory.inventory.addItem(item.clone())
            }

            undeadList.add(undeadInventory)

            UndeadInventory.saveAll(this)


            // Rimuovi l'inventario del giocatore
            player.inventory.clear()
            event.drops.clear()
        }
    }

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val entity = event.entity

        val undeadInventory = undeadList.firstOrNull {
            it.uniqueId == entity.uniqueId
        }

        undeadInventory?.let {
            event.drops.clear()

            for (item in it.inventory) {
                if (item == null) continue
                event.drops.add(item.clone())
            }

            it.inventory.clear()

            undeadList.remove(it)
        }
    }
}
