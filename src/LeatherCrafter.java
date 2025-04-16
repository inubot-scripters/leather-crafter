import com.google.inject.Inject;
import org.rspeer.commons.ArrayUtils;
import org.rspeer.commons.StopWatch;
import org.rspeer.commons.logging.Log;
import org.rspeer.game.Game;
import org.rspeer.game.adapter.component.inventory.Backpack;
import org.rspeer.game.adapter.component.inventory.Bank;
import org.rspeer.game.adapter.scene.Player;
import org.rspeer.game.component.*;
import org.rspeer.game.component.tdi.Skill;
import org.rspeer.game.config.item.entry.builder.ItemEntryBuilder;
import org.rspeer.game.config.item.loadout.BackpackLoadout;
import org.rspeer.game.scene.Players;
import org.rspeer.game.script.*;
import org.rspeer.game.script.meta.ScriptMeta;
import org.rspeer.game.script.meta.paint.PaintBinding;
import org.rspeer.game.script.meta.paint.PaintScheme;

@ScriptMeta(name = "Leather Crafter", paint = PaintScheme.class, model = Config.class)
public class LeatherCrafter extends TaskScript {

  @PaintBinding("Runtime")
  private final StopWatch runtime = StopWatch.start();

  @PaintBinding("Skill")
  private final Skill skill = Skill.CRAFTING;

  @Override
  public Class<? extends Task>[] tasks() {
    return ArrayUtils.getTypeSafeArray(
        Craft.class
    );
  }

  @TaskDescriptor(name = "Craft", stoppable = true)
  public static class Craft extends Task {

    private final Config config;

    private boolean stop = false;
    private int animation = Integer.MIN_VALUE;

    @Inject
    public Craft(Config config) {
      this.config = config;
    }

    @Override
    public boolean execute() {
      Player self = Players.self();
      if (self == null) {
        return false;
      }

      if (!config.isBound()) {
        return false;
      }

      Backpack inv = Inventories.backpack();
      if (inv.getItems(config.getLeatherName()).size() < config.getLeatherPerProduct() || inv.getItems("Needle").isEmpty() || inv.getItems("Thread").isEmpty()) {
        if (!Bank.isOpen()) {
          Bank.open();
          return false;
        }

        BackpackLoadout loadout = new BackpackLoadout("Crafting");
        loadout.add(new ItemEntryBuilder()
            .key("Thread")
            .quantity(100)
            .stackable(true)
            .build());

        loadout.add(new ItemEntryBuilder()
            .key("Needle")
            .quantity(1)
            .stackable(true)
            .build());

        loadout.add(new ItemEntryBuilder()
            .key("Teak logs")
            .quantity(1)
            .build());

        loadout.add(new ItemEntryBuilder()
            .key("Knife")
            .quantity(1)
            .build());

        loadout.add(new ItemEntryBuilder()
            .key(config.getLeatherName())
            .quantity(28 - loadout.getAllocated())
            .build());

        loadout.setOutOfItemListener(e -> {
          String key = e.getKey();
          if (key.equals("Knife") || key.equals("Teak logs")) {
            return;
          }

          Log.warn("Stopping script: out of " + key);
          stop = true;
        }); //stop when out
        loadout.withdraw(Inventories.bank());
        Interfaces.closeSubs();
        animation = Integer.MIN_VALUE;
        return stop;
      }

      if (animation != Integer.MIN_VALUE && self.isAnimating()) {
        animation = Game.getTick();
      }

      if (animation != Integer.MIN_VALUE && Game.getTick() - animation <= 2) {
        return false;
      }

      if (Bank.isOpen()) {
        Interfaces.closeSubs();
      }

      Production production = Production.getActive();
      if (production != null && production.isOpen()) {
        production.initiate(iq -> iq.nameContains(config.getProductName()).results().first());
        sleepUntil(() -> {
          if (self.isAnimating()) {
            animation = Game.getTick();
            return true;
          }
          return false;
        }, 3);
        return false;
      }

      inv.use(e -> e.names("Knife").results().first(), e -> e.nameContains("Teak logs").results().first());
      inv.use(e -> e.names("Needle").results().first(), e -> e.nameContains("Leather").results().first());
      return false;
    }
  }
}
