package org.dearpowa.zombifier

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import java.io.Serializable
import java.util.*

class UndeadInventory(val uniqueId: UUID, plugin: Zombifier, size: Int) : InventoryHolder, Serializable {


    private val inventory: Inventory = plugin.server.createInventory(this, size)

    override fun getInventory(): Inventory = inventory


    companion object {

        private const val undeadInventoriesKey = "UNDEAD-INVENTORIES"
        private const val undeadInventoriesPath = "./plugins/Zombifier/undeadInvetories.yaml"

        fun loadAll(plugin: Zombifier) {
            plugin.apply {

                try {
                    logger.info("Loading started...")

                    logger.info("Loading undeadInventories from $undeadInventoriesPath")

                    config.load(undeadInventoriesPath)


                    logger.info("Getting the map with key: $undeadInventoriesKey, from config")
                    val map = config.get(undeadInventoriesKey) as? Map<UUID, List<ItemStack>>


                    logger.info("Converting the map into UndeadInvetory list")
                    val loaded = map?.mapNotNull {
                        return@mapNotNull UndeadInventory(it.key, plugin, it.value.size).apply {
                            for (item in it.value) {
                                getInventory().addItem(item.clone())
                            }
                        }
                    } ?: mutableListOf()

                    logger.info("Updating the current undeadList")
                    undeadList.addAll(loaded)
                    logger.info("Loading done!")
                } catch (e: Exception) {
                    logger.warning("Could not load undeadInvetories from $undeadInventoriesPath")
                }
            }
        }


        fun saveAll(plugin: Zombifier) {
            plugin.apply {

                logger.info("Saving started...")


                logger.info("Converting UndeadInvetory list to Map<UUID, List of ItemStack> ")
                val map = mutableMapOf<UUID, List<ItemStack>>()

                undeadList.forEach {
                    map[it.uniqueId] = it.inventory.toList()
                }

                logger.info("Setting the map with key: $undeadInventoriesKey, to config")
                config.set(undeadInventoriesKey, map)

                logger.info("Saving undeadInvetories to $undeadInventoriesPath")
                config.save(undeadInventoriesPath)
                logger.info("Saving completed!")
            }

        }


    }
}