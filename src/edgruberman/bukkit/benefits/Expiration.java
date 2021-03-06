package edgruberman.bukkit.benefits;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;

import edgruberman.bukkit.benefits.triggers.Delay;

/** benefit undo command with default delay trigger */
public final class Expiration extends Command {

    private static final List<String> DEFAULT_TRIGGER = Arrays.asList(Delay.class.getSimpleName());
    private static final boolean DEFAULT_EXTEND = false;
    private static final String DEFAULT_DISPATCH = "benefits:undo '{'5'}' \"{0}\" \"{1}\""; // 0 = package, 1 = benefit, 2 = command

    private static final String NAME = "(expiration)";

    private final boolean extend;

    /** index of last contribution by player lower case name */
    private final Map<String, Contribution> last = new HashMap<String, Contribution>();

    Expiration(final Benefit benefit, final ConfigurationSection definition) {
        super(benefit, Expiration.NAME, new ArrayList<String>(), new ArrayList<String>(), new ArrayList<Trigger>());

        final List<String> dispatchDefaults = new ArrayList<String>();
        dispatchDefaults.add(MessageFormat.format(Expiration.DEFAULT_DISPATCH, benefit.pkg.name, benefit.name, this.name));
        this.dispatch.addAll(Command.getStringList(definition, "dispatch", dispatchDefaults));

        this.parseTriggers(definition, Expiration.DEFAULT_TRIGGER);

        this.extend = definition.getBoolean("extend", Expiration.DEFAULT_EXTEND);
    }

    @Override
    public void assign(final Contribution contribution) {
        if (this.extend) {
            final String lower = contribution.player.toLowerCase(Locale.ENGLISH);
            final Contribution previous = this.last.get(lower);
            if (previous != null && contribution.contributed >= previous.contributed) this.unassign(previous);
            if (previous == null || contribution.contributed >= previous.contributed) this.last.put(lower, contribution);
        }
        super.assign(contribution);
    }

    @Override
    protected boolean unassign(final Contribution contribution) {
        final String lower = contribution.player.toLowerCase(Locale.ENGLISH);
        if (this.last.get(lower) == contribution) this.last.remove(lower);
        return super.unassign(contribution);
    }

}
