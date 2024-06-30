package keystrokesmod.module.impl.movement;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.RenderUtils;
import keystrokesmod.utility.Utils;
import keystrokesmod.event.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.RandomUtils;
import org.lwjgl.input.Keyboard;

public class Fly extends Module {
    private SliderSetting mode;
    public static SliderSetting horizontalSpeed;
    private SliderSetting verticalSpeed;
    private ButtonSetting showBPS;
    private ButtonSetting stopMotion;
    private boolean d;
    private boolean a = false;
    private double startY;
    private String[] modes = new String[]{"Vanilla", "Fast", "MushBlock", "MushGlide"};

    public Fly() {
        super("Fly", category.movement);
        this.registerSetting(mode = new SliderSetting("Fly", modes, 0));
        this.registerSetting(horizontalSpeed = new SliderSetting("Horizontal speed", 2.0, 1.0, 9.0, 0.1));
        this.registerSetting(verticalSpeed = new SliderSetting("Vertical speed", 2.0, 1.0, 9.0, 0.1));
        this.registerSetting(showBPS = new ButtonSetting("Show BPS", false));
        this.registerSetting(stopMotion = new ButtonSetting("Stop motion", false));
    }

    public void onEnable() {
        this.d = mc.thePlayer.capabilities.isFlying;
        startY = Minecraft.getMinecraft().thePlayer.posY;
    }

    public void onUpdate() {
        switch ((int) mode.getInput()) {
            case 0:
                mc.thePlayer.motionY = 0.0;
                mc.thePlayer.capabilities.setFlySpeed((float)(0.05000000074505806 * horizontalSpeed.getInput()));
                mc.thePlayer.capabilities.isFlying = true;
                break;
            case 1:
                mc.thePlayer.onGround = true;
                if (mc.currentScreen == null) {
                    if (Utils.jumpDown()) {
                        mc.thePlayer.motionY = 0.3 * verticalSpeed.getInput();
                    }
                    else if (Utils.jumpDown()) {
                        mc.thePlayer.motionY = -0.3 * verticalSpeed.getInput();
                    }
                    else {
                        mc.thePlayer.motionY = 0.0;
                    }
                }
                else {
                    mc.thePlayer.motionY = 0.0;
                }
                mc.thePlayer.capabilities.setFlySpeed(0.2f);
                mc.thePlayer.capabilities.isFlying = true;
                setSpeed(0.85 * horizontalSpeed.getInput());
                break;
            case 2:
                if (mc.thePlayer.onGround) {
                    placeBlockUnderPlayer();
                    mc.thePlayer.jump();
                } else if (mc.thePlayer.posY >= startY) {
                    placeBlockUnderPlayer();
                }
        }
    }

    private void placeBlockUnderPlayer() {
        Minecraft mc = Minecraft.getMinecraft();
        BlockPos blockPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ);

        if (mc.theWorld.getBlockState(blockPos).getBlock().isReplaceable(mc.theWorld, blockPos)) {
            mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, null, blockPos, EnumFacing.UP, new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
        }
    }
	
	@SubscribeEvent
	public void onPreMotion(PreMotionEvent e) {
		if (mode.getInput() == 3) {
            if (mc.thePlayer.ticksExisted % 13 == 0)
                return;

            mc.thePlayer.motionY *= 0.8;
        }
	}

    public void onDisable() {
        if (mc.thePlayer.capabilities.allowFlying) {
            mc.thePlayer.capabilities.isFlying = this.d;
        }
        else {
            mc.thePlayer.capabilities.isFlying = false;
        }
        this.d = false;
        switch ((int) mode.getInput()) {
            case 0:
            case 1: {
                mc.thePlayer.capabilities.setFlySpeed(0.05F);
                break;
            }
            case 2: {
                a = false;
                break;
            }
        }
        if (stopMotion.isToggled()) {
            mc.thePlayer.motionZ = 0;
            mc.thePlayer.motionY = 0;
            mc.thePlayer.motionX = 0;
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent e) {
        if (!showBPS.isToggled() || e.phase != TickEvent.Phase.END || !Utils.nullCheck()) {
            return;
        }
        if (mc.currentScreen != null || mc.gameSettings.showDebugInfo) {
            return;
        }
        RenderUtils.renderBPS(true, false);
    }

    public static void setSpeed(final double n) {
        if (n == 0.0) {
            mc.thePlayer.motionZ = 0;
            mc.thePlayer.motionX = 0;
            return;
        }
        double n3 = mc.thePlayer.movementInput.moveForward;
        double n4 = mc.thePlayer.movementInput.moveStrafe;
        float rotationYaw = mc.thePlayer.rotationYaw;
        if (n3 == 0.0 && n4 == 0.0) {
            mc.thePlayer.motionZ = 0;
            mc.thePlayer.motionX = 0;
        }
        else {
            if (n3 != 0.0) {
                if (n4 > 0.0) {
                    rotationYaw += ((n3 > 0.0) ? -45 : 45);
                }
                else if (n4 < 0.0) {
                    rotationYaw += ((n3 > 0.0) ? 45 : -45);
                }
                n4 = 0.0;
                if (n3 > 0.0) {
                    n3 = 1.0;
                }
                else if (n3 < 0.0) {
                    n3 = -1.0;
                }
            }
            final double radians = Math.toRadians(rotationYaw + 90.0f);
            final double sin = Math.sin(radians);
            final double cos = Math.cos(radians);
            mc.thePlayer.motionX = n3 * n * cos + n4 * n * sin;
            mc.thePlayer.motionZ = n3 * n * sin - n4 * n * cos;
        }
    }
}
