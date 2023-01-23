package de.kryolisc.livedev.ct;

public class DevClass{

    /**
     * This method will be used to insert source code and execute it
     *
     * @param plugin the plugin instance, possible to use getServer(), for example
     * @param me the CommandSender of the executor as a Player instance, if it is one
     * @param sender the CommandSender instance regardless of the type
     * @param block if the CommandSender is a Player, the Block that player is looking at
     * @param entity if the CommandSender is a Player, the Entity that player is looking at
     * @param livingEntity if entity is an instance of LivingEntity, the cast LivingEntity instance (for ease of use)
     * @param player if entity is an instance of Player, the cast Player instance (for ease of use)
     */
    public void execute(
            de.kryolisc.livedev.LiveDev plugin,
            org.bukkit.entity.Player me,
            org.bukkit.command.CommandSender sender,
            org.bukkit.block.Block block,
            org.bukkit.entity.Entity entity,
            org.bukkit.entity.LivingEntity livingEntity,
            org.bukkit.entity.Player player
    ) {

    }
}
