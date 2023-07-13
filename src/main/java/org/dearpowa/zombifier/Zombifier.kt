package org.dearpowa.zombifier

import org.bukkit.block.Biome
import org.bukkit.entity.Ageable
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.plugin.java.JavaPlugin

class Zombifier : JavaPlugin(), Listener {

    val undeadList = mutableListOf<UndeadInventory>()
    val config = Config()

    override fun onEnable() {
        // Plugin startup logic
        logger.info("Hello World from Zombifier!")
        //UndeadInventory.loadAll(this)
        config.load(this)
        server.pluginManager.registerEvents(this, this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
        // UndeadInventory.saveAll(this)
        config.save(this)
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.player

        val playerLocation = player.location

        logger.info("Player ${player.name} died")

        val undeadType = when (player.lastDamageCause?.cause) {
            EntityDamageEvent.DamageCause.FIRE,
            EntityDamageEvent.DamageCause.FIRE_TICK,
            EntityDamageEvent.DamageCause.LAVA,
            EntityDamageEvent.DamageCause.HOT_FLOOR,
            EntityDamageEvent.DamageCause.LIGHTNING -> EntityType.WITHER_SKELETON // Scheletro Nero

            EntityDamageEvent.DamageCause.DROWNING -> EntityType.DROWNED // Zombie affogato

            EntityDamageEvent.DamageCause.FREEZE -> EntityType.STRAY // Scheletro delle nevi

            else -> when (playerLocation.block.biome) {
                Biome.DESERT -> EntityType.HUSK // Zombie del deserto
                Biome.OCEAN, Biome.DEEP_OCEAN -> EntityType.DROWNED // Zombie affogato
                Biome.NETHER_WASTES -> EntityType.SKELETON // Scheletro normale
                else -> EntityType.ZOMBIE // Zombie normale
            }
        }

        logger.info("Spawing undead of type: ${undeadType.name} at ${playerLocation.toString()}")

        val undead = player.world.spawnEntity(playerLocation, undeadType)

        // Se il mob che ho creato è una livingEntity (quindi può avere un equipment)
        // allora gli imposto tutto il necessario per "copiare" il player
        (undead as? LivingEntity)?.apply {
            val name = player.name()
            val mainHand = player.inventory.itemInMainHand
            val offHand = player.inventory.itemInOffHand
            val helmet = player.inventory.helmet
            val chestplate = player.inventory.chestplate
            val leggings = player.inventory.leggings
            val boots = player.inventory.boots


            // Copia l'inventario del giocatore all'entità zombie
            equipment?.setItemInMainHand(mainHand)
            equipment?.setItemInOffHand(offHand)
            equipment?.helmet = helmet
            equipment?.chestplate = chestplate
            equipment?.leggings = leggings
            equipment?.boots = boots

            // Cambio nome al non morto
            customName(name)
            isCustomNameVisible = true

            // Prevengo il despawn del non morto
            removeWhenFarAway = false

            // Imposto il non morto come adulto adulto (se può avere un età)
            (this as? Ageable)?.apply {
                setAdult()
            }

        }

        // Creo l'invetario del non morto
        val undeadInventory = UndeadInventory(undead.uniqueId, this, 54)

        // Copio l'inventario del player nel non morto
        for (item in player.inventory) {
            if (item == null) continue
            undeadInventory.inventory.addItem(item.clone())
        }

        // Salvo la copia dell'inventario nella mia mappa
        undeadList.add(undeadInventory)

        // Salvo tutti gli inventari su disco
        // UndeadInventory.saveAll(this)

        // Rimuovi l'inventario del player
        player.inventory.clear()
        event.drops.clear()
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
