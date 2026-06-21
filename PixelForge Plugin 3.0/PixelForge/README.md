# StrengthLifesteal

A Paper/Spigot plugin combining Strength SMP and Lifesteal SMP mechanics into one server.

## What it does

**Curse / Blessing loop**
- Die → you get a random "bad" effect (curse), permanent until cured.
- Get a kill → one of your own curses is removed. If you had none, you instead get a random "good" effect (blessing).
- Curses: Weakness, Slowness, Mining Fatigue, Hunger, Unluck, Blindness, Nausea
- Blessings: Strength, Speed, Haste, Regeneration, Luck, Jump Boost, Fire Resistance, Saturation

**Hearts**
- Every death costs you 1 heart (max health -2), and that heart drops on the ground as a head wearing your face, named "YourName's Heart."
- Anyone who picks it up and right-clicks gains +1 heart, capped at 20 hearts (40 HP) total.
- If your hearts hit 0, you're eliminated for the season — banned from the server until revived.

**Revival**
- Craft a Resurrection Totem from 4 Wither Skeleton Skulls, 2 Totems of Undying, 4 Diamonds, and a Nether Star (recipe shape below).
- Right-click it to open a GUI of every player banned this season, shown as their own player heads — hover to see their name.
- Click a head to revive that player (10 hearts / 20 HP) and consume the totem.

```
Crafting grid:
[Wither Skull] [Totem] [Wither Skull]
[Diamond]      [Star]  [Diamond]
[Wither Skull] [Totem] [Wither Skull]
```

## Design assumption to know about

Elimination/ban only triggers when your hearts hit **0**, not on every death — that's how real Lifesteal SMP works, and it's what makes the heart-pickup and revival systems meaningful. If you actually wanted every single death to be an instant ban (more hardcore), tell me and I'll change `DeathListener.java` — it's a small tweak.

## Building it

This was written for **Paper 1.20.4** and **Java 17+**. I can't compile it in this sandbox (no access to Maven/PaperMC repositories from here), so build it on your own machine:

```bash
mvn clean package
```

The finished jar lands in `target/StrengthLifesteal.jar`. Drop it in your server's `plugins/` folder and restart.

If you hit a compile error, paste it back to me and I'll fix it — there's always a small chance of an API mismatch since I wrote this without being able to test-compile against the real Paper API.

## Admin commands (for testing without real PvP)

- `/ssmp ban <player>` — manually eliminate someone
- `/ssmp unban <player>` — manually revive someone (skips the totem/GUI)
- `/ssmp listbanned` — see who's currently out

## Easy things to customize

- **Heart amount per death / max hearts cap** — `HEART` and `MAX_HEALTH` constants in `DeathListener.java` and `HeartItemListener.java`
- **Curse/blessing pools** — `BAD_EFFECTS` / `GOOD_EFFECTS` lists in `EffectManager.java`
- **Revival recipe ingredients** — `RevivalRecipe.java`
- **Revival heart amount** — currently hardcoded to 20.0 (10 hearts) in `DeathListener.java`'s elimination branch

## File structure

```
src/main/java/com/strengthsmp/plugin/
├── StrengthSMP.java          (main plugin class)
├── listeners/
│   ├── DeathListener.java    (curses, heart loss, elimination)
│   ├── HeartItemListener.java (picking up hearts)
│   ├── RevivalListener.java  (using the totem)
│   └── JoinListener.java     (re-applying effects on join)
├── managers/
│   ├── EffectManager.java    (curse/blessing tracking + persistence)
│   └── BanManager.java       (season ban tracking + persistence)
├── gui/
│   └── RevivalGUI.java       (the banned-players picker)
├── recipes/
│   └── RevivalRecipe.java    (totem crafting recipe)
├── util/
│   └── HeartItemFactory.java (builds the heart drop item)
└── commands/
    └── SSMPCommand.java      (admin testing commands)
```
