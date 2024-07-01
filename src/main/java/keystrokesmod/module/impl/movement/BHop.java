package keystrokesmod.module.impl.movement;

import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;


public class BHop extends Module {
    private SliderSetting mode;
    public static SliderSetting speed;
    private ButtonSetting autoJump;
    private ButtonSetting liquidDisable;
    private ButtonSetting sneakDisable;
    private ButtonSetting stopMotion;
    private ButtonSetting damageBoost;
    private SliderSetting boostMultiplier;
    private String[] modes = new String[]{"Strafe", "Ground", "MushMC", "Verus & NCP", "MushMCYPort"};
    public boolean hopping;

    public BHop() {
        super("Bhop", category.movement);
        this.registerSetting(mode = new SliderSetting("Mode", modes, 0));
        this.registerSetting(speed = new SliderSetting("Speed", 2.0, 0.5, 8.0, 0.1));

        this.registerSetting(autoJump = new ButtonSetting("Auto jump", true));
        this.registerSetting(liquidDisable = new ButtonSetting("Disable in liquid", true));
        this.registerSetting(sneakDisable = new ButtonSetting("Disable while sneaking", true));
        this.registerSetting(stopMotion = new ButtonSetting("Stop motion", false));
    }

    @Override
    public String getInfo() {
        return modes[(int) mode.getInput()];
    }

    public void onUpdate() {
        if (((mc.thePlayer.isInWater() || mc.thePlayer.isInLava()) && liquidDisable.isToggled()) || (mc.thePlayer.isSneaking() && sneakDisable.isToggled())) {
            return;
        }
        switch ((int) mode.getInput()) {
            case 0:
                if (Utils.isMoving()) {
                    if (mc.thePlayer.onGround && autoJump.isToggled()) {
                        mc.thePlayer.jump();
                    }
                    mc.thePlayer.setSprinting(true);
                    Utils.setSpeed(Utils.getHorizontalSpeed() + 0.005 * speed.getInput());
                    hopping = true;
                    break;
                }
                break;
            case 1:
                if (!Utils.jumpDown() && Utils.isMoving() && mc.currentScreen == null) {
                    if (!mc.thePlayer.onGround) {
                        break;
                    }
                    if (autoJump.isToggled()) {
                        mc.thePlayer.jump();
                    }
                    else if (!Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) && !autoJump.isToggled()) {
                        return;
                    }
                    mc.thePlayer.setSprinting(true);
                    double horizontalSpeed = Utils.getHorizontalSpeed();
                    double additionalSpeed = 0.4847 * ((speed.getInput() - 1.0) / 3.0 + 1.0);
                    if (horizontalSpeed < additionalSpeed) {
                        horizontalSpeed = additionalSpeed;
                    }
                    Utils.setSpeed(horizontalSpeed);
                    hopping = true;
                }
                break;
            case 2:
                if (Utils.isMoving()) {
                    if (mc.thePlayer.onGround && autoJump.isToggled()) {
                        mc.thePlayer.jump();

                        if (mc.thePlayer.isCollidedHorizontally) {

                        }
                        else{
                            mc.thePlayer.motionY *= 0.1;

                        }
                    }
                    mc.thePlayer.setSprinting(true);
                    Utils.setSpeed(Utils.getHorizontalSpeed() + 0.005 * speed.getInput());
                    hopping = true;
                }
                break;
                case 3:
                    if (Utils.isMoving()) {
                        if (mc.thePlayer.onGround && autoJump.isToggled()) {
                            mc.thePlayer.jump();
                        }


                        mc.thePlayer.setSprinting(true);
                        double currentSpeed = Utils.getHorizontalSpeed();
                        double additionalSpeed = 0.005 * speed.getInput();


                        if (currentSpeed < additionalSpeed) {
                            currentSpeed = additionalSpeed;
                        }

                        Utils.setSpeed(currentSpeed);
                        hopping = true;
                    }
                break;
            case 4:
                double csp = Utils.getHorizontalSpeed();
                if (csp != 0.0D) {
                    if (mc.thePlayer.onGround && !mc.thePlayer.capabilities.isFlying) {
                            if (mc.thePlayer.hurtTime != mc.thePlayer.maxHurtTime || mc.thePlayer.maxHurtTime <= 0) {
                                if (!Utils.jumpDown()) {
                                    double val = 1.9 - (1.9 - 1.0D) * 0.5D;
                                    Utils.ss(csp * val, true);
                                }
                            }

                    }
                }
                break;
        }
    }



    public void onDisable() {
        if (stopMotion.isToggled()) {
            mc.thePlayer.motionZ = 0;
            mc.thePlayer.motionY = 0;
            mc.thePlayer.motionX = 0;
        }
        hopping = false;
    }
}
