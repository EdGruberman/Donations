package edgruberman.bukkit.benefits.triggers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import edgruberman.bukkit.benefits.Command;

public class Quit extends PlayerEvent {

    public Quit(final Command command, final ConfigurationSection definition) {
        super(command, definition);
    }

    @EventHandler
    public void onEvent(final PlayerQuitEvent quit) {
        this.apply(quit.getPlayer());
    }

}