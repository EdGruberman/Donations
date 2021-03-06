package edgruberman.bukkit.benefits;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import edgruberman.bukkit.benefits.triggers.Instant;
import edgruberman.bukkit.benefits.util.JoinList;

public class Command {

    private static final List<String> DEFAULT_TRIGGER = Arrays.asList(Instant.class.getSimpleName());

    public final Benefit benefit;
    public final String name;
    public final Collection<String> dispatch;
    public final Collection<String> undo;
    public final Collection<Trigger> triggers;

    Command(final Benefit benefit, final ConfigurationSection definition) {
        this(benefit, definition.getName(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<Trigger>());
        this.dispatch.addAll(Command.getStringList(definition, "dispatch"));
        this.undo.addAll(Command.getStringList(definition, "undo"));
        this.parseTriggers(definition, Command.DEFAULT_TRIGGER);
    }

    protected Command(final Benefit benefit, final String name, final Collection<String> dispatch, final Collection<String> undo, final Collection<Trigger> triggers) {
        this.benefit = benefit;
        this.name = name;
        this.dispatch = dispatch;
        this.undo = undo;
        this.triggers = triggers;
    }

    protected void parseTriggers(final ConfigurationSection definition, final List<String> defaults) {
        for (final String triggerClass : Command.getStringList(definition, "trigger", defaults)) {
            Trigger trigger;
            try {
                trigger = Trigger.create(triggerClass, this, definition);
            } catch (final Exception e) {
                this.getCoordinator().plugin.getLogger().log(Level.WARNING, "Failed to create Trigger: {0}; {1}; Command: {2}", new Object[] { triggerClass, e, this });
                continue;
            }
            this.triggers.add(trigger);
        }
    }

    void clear() {
        for (final Trigger trigger : this.triggers) trigger.clear();
        this.triggers.clear();
    }

    public void assign(final Contribution contribution) {
        this.getCoordinator().plugin.getLogger().log(( this.getCoordinator().isSandbox() ? Level.INFO : Level.FINEST ), "{0,choice,1#|[Sandbox]}  {0,choice,0#Assign|1#Applicable}: {2}"
                , new Object[] { this.getCoordinator().isSandbox()?1:0, this.benefit.pkg, this });

        if (this.getCoordinator().isSandbox()) return;

        for (final Trigger trigger : this.triggers) {
            this.getCoordinator().plugin.getLogger().log(Level.FINEST, "    Trigger added for {0} to {1}", new Object[] { contribution.getKey(), trigger.getPath() });
            trigger.add(contribution);
        }
    }

    public boolean dispatch(final Contribution contribution) {
        if (!this.unassign(contribution)) return false;
        this.execute(contribution, this.dispatch);
        return true;
    }

    public boolean undo(final Contribution contribution) {
        if (!this.unassign(contribution)) return false;
        this.execute(contribution, this.undo);
        return true;
    }

    private void execute(final Contribution contribution, final Collection<String> commands) {
        this.getCoordinator().plugin.getLogger().log(Level.FINEST, "Executing commands for contribution: {0}", contribution);
        for (final String d : commands) {
            String command;
            try {
                command = MessageFormat.format(d, contribution.player, contribution.currency, contribution.amount / 100D, (int) Math.floor(contribution.amount / 100D), new Date(contribution.contributed), contribution.getKey());
            } catch (final Exception e) {
                this.getCoordinator().plugin.getLogger().log(Level.WARNING, "Exception parsing command for {0}; {1}; {2}", new Object[] { this.getPath(), e, d });
                continue;
            }

            this.getCoordinator().plugin.getLogger().log(Level.FINEST, "Dispatching command: {0}", command);
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
        }
        this.getCoordinator().savePending();
    }

    protected boolean unassign(final Contribution contribution) {
        boolean removed = false;
        for (final Trigger trigger : this.triggers) {
            if (trigger.remove(contribution)) removed = true;
        }
        return removed;
    }

    public Coordinator getCoordinator() {
        return this.benefit.pkg.coordinator;
    }

    public String getPath() {
        return this.benefit.getPath() + ">\"" + this.name + "\"";
    }

    @Override
    public String toString() {
        return "Command: [getPath(): " + this.getPath() + "; dispatch:" + JoinList.join(this.dispatch, ", ", "\"{0}\"" + ChatColor.RESET) + "; triggers: " + this.triggers + "]";
    }



    protected static List<String> getStringList(final ConfigurationSection config, final String path) {
        return Command.getStringList(config, path, Collections.<String>emptyList());
    }


    protected static List<String> getStringList(final ConfigurationSection config, final String path, final List<String> defaults) {
        if (config.isList(path))
            return config.getStringList(path);

        if (config.isString(path))
            return Arrays.asList(config.getString(path));

        return defaults;
    }

}
