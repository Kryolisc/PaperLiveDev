package de.kryolisc.livedev;

import de.kryolisc.livedev.ct.DevClass;
import javassist.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class LiveDev extends JavaPlugin implements CommandExecutor {

    private static LiveDev instance;

    private ClassPool classPool;
    private CtClass baseDevClass;

    @Override
    public void onEnable() {
        instance = this;

        // Loading classes for the plugin. This might not be the best way, but it works
        this.getLogger().info("Loading classes...");
        System.out.println(getClass().getClassLoader());
        System.out.println(Bukkit.getServer().getClass().getClassLoader());
        System.out.println(DevClass.class.getClassLoader());
        System.out.println(this.getServer().getPluginManager().getClass().getClassLoader());

        // Initialize javassist Class Pool and add the plugin class loader and default class loader
        this.classPool = ClassPool.getDefault();
        this.classPool.appendClassPath(new LoaderClassPath(getClass().getClassLoader()));
        this.classPool.appendClassPath(new LoaderClassPath(this.getServer().getPluginManager().getClass().getClassLoader()));

        try {
            // Create a single DevClass CtClass instance
            this.baseDevClass = this.classPool.get("de.kryolisc.livedev.ct.DevClass");
            this.baseDevClass.toClass();
        } catch (Exception e) {
            e.printStackTrace();
            this.getServer().getPluginManager().disablePlugin(this);
        }

        // commands
        this.getCommand("java").setExecutor(this);
        this.getCommand("codebook").setExecutor(this);

        this.getLogger().info("Plugin ready");

    }

    /**
     * Handles commands for this plugin. Beware of moving this to another class
     * as this would add additional confusion with the class loaders
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("java.execute")) return true; // Check permission
        boolean async = false;
        String code = "";
        // Determine source code by which command has been executed
        if (command.getName().equalsIgnoreCase("java")) {
            code = String.join(" ", args) + ";";
        } else if (command.getName().equalsIgnoreCase("codebook")) {
            if (args.length < 1) return false;
            if (!(sender instanceof Player)) return false;
            Player p = (Player) sender;
            if (args.length > 1)
             if (String.join(" ", Arrays.copyOfRange(args, 1, args.length)).contains("async")) async = true;
            switch (args[0]) {
                case "run":
                    // Get source code from book meta
                    ItemStack itemInHand = p.getItemInHand();
                    if (itemInHand.getType() == Material.WRITABLE_BOOK || itemInHand.getType() == Material.WRITTEN_BOOK) {
                        BookMeta bookMeta = (BookMeta) itemInHand.getItemMeta();
                        for (Component component : bookMeta.pages()) {
                            code += ((TextComponent) component).content();
                        }
                    }
                    break;
                default:
                    return false;
            }
        }

        // Prepare variables for the execute() method
        Player player = null;
        if (sender instanceof Player) player = (Player) sender;
        Block lookingAtBlock = null;
        Entity lookingAtEntity = null;
        LivingEntity lookingAtLivingEntity = null;
        Player lookingAtPlayer = null;
        if (player != null) {
            lookingAtBlock = player.getTargetBlock(120);
            lookingAtEntity = player.getTargetEntity(120);
            if (lookingAtEntity instanceof LivingEntity) lookingAtLivingEntity = (LivingEntity) lookingAtEntity;
            if (lookingAtLivingEntity instanceof Player) lookingAtPlayer = (Player) lookingAtLivingEntity;
        }

        // Try to compile and run the code
        try {
            // Create a new class with a random name (same class can't be modified twice) that extends
            // the DevClass class to access the execute() method
            String instanceName = System.currentTimeMillis() + "";
            CtClass devClass = this.classPool.getAndRename("de.kryolisc.livedev.ct.DevClass", "de.kryolisc.livedev.ct.DevClass" + instanceName);
            devClass.setSuperclass(this.baseDevClass);
            // Get the execute() method and put the source code in it
            CtMethod execute = devClass.getDeclaredMethod("execute");
            execute.insertBefore("{ " + code + " }");
            // Create an instance of the class and invoke the execute() method using previously prepared parameters
            Class<?> clazz = devClass.toClass(getClassLoader(), getClass().getProtectionDomain());
            Object[] params = new Object[]{
                    this,
                    player,
                    sender,
                    lookingAtBlock,
                    lookingAtEntity,
                    lookingAtLivingEntity,
                    lookingAtPlayer
            };
            // There is some weird stuff with method signatures going on, so this searches for the execute() method
            for (Method m : clazz.getMethods()) {
                if (m.getName().equals("execute")) {
                    // Run code in another task
                    if (async) {
                        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                            try {
                                m.invoke(clazz.newInstance(), params);
                            } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                                sender.sendMessage("§4" + e.getClass().getSimpleName() + ": §c" + e.getMessage());
                                e.printStackTrace();
                            }
                        });
                    } else {
                        Bukkit.getScheduler().runTask(this, () -> {
                            try {
                                m.invoke(clazz.newInstance(), params);
                            } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                                sender.sendMessage("§4" + e.getClass().getSimpleName() + ": §c" + e.getMessage());
                                e.printStackTrace();
                            }
                        });
                    }
                    break;
                }
            }
            devClass.prune();
        } catch (Exception e) {
            sender.sendMessage("§4" + e.getClass().getSimpleName() + ": §c" + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    public ClassPool getClassPool() {
        return classPool;
    }

    public static LiveDev getInstance() {
        return instance;
    }
}
