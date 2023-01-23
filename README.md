# LiveDev Plugin
LiveDev is a plugin for PaperMC servers that enables players (or possibly rather developers) to execute basic Java code from inside the game.

This is mostly experimental, but I've decided to release the source code if someone else wants to use it or someone who is more experienced with javassist than me wants to contribute.

As I've already mentioned, this plugin uses javassist to do the runtime compilation and class loading.

## Disclaimer
This Plugin can be a huge security risk, as it allows players to execute any arbitrary piece of code they want (within the plugin's limitations). If used wrongly, this may cause a lot of damage. Thus, I will hold no responsibility for the use of this plugin or any damage it may cause. Use at your own risk.

# How to use
## Permissions
Before you do *anything* else, you should properly configure permissions for this plugin, but that's quite simple. There is only one permission node a player must have to run code using commands or books: ```java.execute```.

Be aware that the commands can also be executed from the console or command blocks.

## The commands
There are two commands to run code.

1) `/java <source code>` is used to run one-liners. You don't have to add a `;` at the end.
2) `/codebook run [async]` can be used to run more complex programs written in a book. The `async` flag can be set to `true` to run an asynchronous Task instead of a synchronous one.

### Notes about books
You can use a regular Book and Quill to write your code. Every line has to end with a `;`. Think of it as if you'd write the body of a function - because you are.
To execute the finished source code, you can use the Book and Quill directly or sign the book beforehand.
Either way, you have to put the book you want to run in your main hand and execute the `/codebook run` command.

## Your environment
When using either of the commands mentioned above, you have the same environment:
the `execute()` method of the `DevClass` class. The code you write will be inserted into this method.

This allows you to access the following variables out of the box:

| **Variable**       | Type                             | Description                                                                                  |
|--------------------|----------------------------------|----------------------------------------------------------------------------------------------|
| **plugin**         | de.kryolisc.livedev.LiveDev      | This variable can be used to access the plugin instance and access things like `getServer()` |
| **me**             | org.bukkit.entity.Player         | CommandSender of the executor as a Player instance, if it is one                             |
| **sender**         | org.bukkit.command.CommandSender | The CommandSender instance regardless of the type                                            |
| **block** *        | org.bukkit.block.Block           | The target Block                                                                             |
| **entity** *       | org.bukkit.entity.Entity         | The target Entity                                                                            |
| **livingEntity** * | org.bukkit.entity.LivingEntity   | The target Entity cast to LivingEntity                                                       |
| **player** *       | org.bukkit.entity.Player         | The target Entity cast to Player                                                             | 

*: Refers to the target the executing player is looking at. Not applicable from console or command blocks. Maximum distance is 120 Blocks as per the APIs limitations

### Useful to know when programming ingame

* You can use most of the basic classes from the Bukkit/Spigot/Paper API
* If you want to interact with other plugins, you will have to add them as a dependency and add their class loader, probably also importing necessary classes
* As there is no `import`, you have to specify the full name when referring to classes. Instead of `Material.STONE`, you would have to use `org.bukkut.Material.STONE`
* For class names, refer to [the Javadocs of PaperMC](https://jd.papermc.io/paper/1.19/)

## Some last notes

As I have already mentioned, this is an experiment and my first contact with javassist and bytecode manipulation during runtime.
Do not use this in production.

As I couldn't get the official javassist Maven dependency to work somehow, I used my own repository to host the dependency jar.

Any pull requests improving this plugin are greatly appreciated. You don't have to follow any specific guidelines to contributing, just make sure to use common sense.
