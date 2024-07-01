package keystrokesmod.module.impl.ghost;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import org.lwjgl.input.Mouse;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class LeftClicker extends Module {
    public SliderSetting minCPS;
    public SliderSetting maxCPS;
    public SliderSetting jitter;
    public ButtonSetting weaponOnly;
    public ButtonSetting disableOnInventory;
    private Random rand = new Random();
    private long nextClickTime;
    private long lastClickTime;
    private boolean allow;

    public LeftClicker() {
        super("Left Clicker", category.ghost, 0);
        this.registerSetting(minCPS = new SliderSetting("Min CPS", 9.0, 1.0, 20.0, 0.5));
        this.registerSetting(maxCPS = new SliderSetting("Max CPS", 12.0, 1.0, 20.0, 0.5));
        this.registerSetting(jitter = new SliderSetting("Jitter", 0.0, 0.0, 3.0, 0.1));
        this.registerSetting(weaponOnly = new ButtonSetting("Weapon only", false));
        this.registerSetting(disableOnInventory = new ButtonSetting("Disable On Inventory", true));
        this.nextClickTime = System.currentTimeMillis() + getRandomDelay();
    }

    public String getInfo() {
        return String.valueOf((int) minCPS.getInput()) + "cps" + " - " + String.valueOf((int) maxCPS.getInput()) + "cps";

    }

    public void guiUpdate() {
        Utils.correctValue(minCPS, maxCPS);
    }

    private long getRandomDelay() {
        long min = (long) (1000 / minCPS.getInput());
        long max = (long) (1000 / maxCPS.getInput());
        long delay = max > min ? ThreadLocalRandom.current().nextLong(min, max) : min;
        delay += ThreadLocalRandom.current().nextLong(-min / 3, min / 3);
        return delay;
    }

    @SubscribeEvent
    public void onRenderTick(RenderTickEvent e) {
        if (mc.thePlayer == null)
            return;

        if (disableOnInventory.isToggled() && mc.currentScreen != null)
            return;

        if (weaponOnly.isToggled() && !Utils.holdingWeapon())
            return;

        if (System.currentTimeMillis() > nextClickTime) {
            nextClickTime = System.currentTimeMillis() + getRandomDelay();
            allow = true;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (mc.thePlayer == null)
            return;

        if (disableOnInventory.isToggled() && mc.currentScreen != null)
            return;

        if (Mouse.isButtonDown(0)) {
            if (jitter.getInput() > 0.0D) {
                double jitterAmount = jitter.getInput() * 0.45D;
                applyJitter(jitterAmount);
            }

            if (allow && System.currentTimeMillis() > lastClickTime) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), true);
                KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());
                lastClickTime = System.currentTimeMillis();

                if (ThreadLocalRandom.current().nextInt(10) < 2) {
                    nextClickTime += ThreadLocalRandom.current().nextInt(20, 50);
                }

                nextClickTime = System.currentTimeMillis() + getRandomDelay();
                allow = false;

                KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
            }
        }
    }

    private void applyJitter(double jitterAmount) {
        EntityPlayerSP player = mc.thePlayer;
        if (rand.nextBoolean()) {
            player.rotationYaw += rand.nextFloat() * jitterAmount;
        } else {
            player.rotationYaw -= rand.nextFloat() * jitterAmount;
        }

        if (rand.nextBoolean()) {
            player.rotationPitch += rand.nextFloat() * jitterAmount * 0.45D;
        } else {
            player.rotationPitch -= rand.nextFloat() * jitterAmount * 0.45D;
        }
    }
}
