package keystrokesmod.module.impl.ghost;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MovingObjectPosition;
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
    public SliderSetting delay;
    public ButtonSetting weaponOnly;
    public ButtonSetting disableOnInventory;
    public ButtonSetting breakBlocks;
    private Random rand = new Random();
    private long nextClickTime;
    private long lastClickTime;
    private boolean allow;
    private boolean alternate;

    public LeftClicker() {
        super("Left Clicker", category.ghost, 0);
        this.registerSetting(minCPS = new SliderSetting("Min CPS", 9.0, 1.0, 20.0, 0.5));
        this.registerSetting(maxCPS = new SliderSetting("Max CPS", 12.0, 1.0, 20.0, 0.5));
        this.registerSetting(jitter = new SliderSetting("Jitter", 0.0, 0.0, 3.0, 0.1));
        this.registerSetting(delay = new SliderSetting("Delay", 1.0, 1.0, 10.0, 1.0));
        this.registerSetting(weaponOnly = new ButtonSetting("Weapon only", false));
        this.registerSetting(disableOnInventory = new ButtonSetting("Disable On Inventory", true));
        this.registerSetting(breakBlocks = new ButtonSetting("Break Blocks", true));
        this.nextClickTime = System.currentTimeMillis() + getRandomDelay();
        this.alternate = false;
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

    public String getInfo() {
        return String.valueOf((int) delay.getInput());
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
                boolean shouldClick = true;

                if (breakBlocks.isToggled() && isLookingAtBreakableBlock()) {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), true);
                    KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());
                    lastClickTime = System.currentTimeMillis();
                    nextClickTime = System.currentTimeMillis() + getRandomDelay();
                    allow = false;
                } else if (!breakBlocks.isToggled() || !isLookingAtBreakableBlock()) {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), true);
                    KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());
                    lastClickTime = System.currentTimeMillis();

                    long randomDelay = getRandomDelay();
                    nextClickTime = System.currentTimeMillis() + (alternate ? randomDelay / 2 : randomDelay);
                    alternate = !alternate;

                    if (ThreadLocalRandom.current().nextInt(10) < 2) {
                        nextClickTime += ThreadLocalRandom.current().nextInt(20, 50);
                    }

                    allow = false;
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
                }
            }
        } else {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
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

    private boolean isLookingAtBreakableBlock() {
        MovingObjectPosition mouseOver = mc.objectMouseOver;

        if (mouseOver != null && mouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            Block block = mc.theWorld.getBlockState(mouseOver.getBlockPos()).getBlock();

            if (block.getBlockHardness(mc.theWorld, mouseOver.getBlockPos()) >= 0) {
                return true;
            }
        }

        return false;
    }
}
