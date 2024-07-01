package keystrokesmod.module.impl.ghost;

import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.RenderUtils;
import keystrokesmod.utility.pasted.TimerUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.network.status.server.S01PacketPong;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import scala.Int;

import java.awt.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Backtrack extends Module {
    private SliderSetting delay;
    private EntityPlayer target = null;
    private TimerUtils timer = new TimerUtils(0);
    private ConcurrentLinkedQueue<Packet> packets = new ConcurrentLinkedQueue<>();
    private double x, y, z;

    public Backtrack() {
        super("Backtrack", category.ghost, 0);
        this.registerSetting(delay = new SliderSetting("Delay MS", 50.0D, 1.0D, 1000.0D, 1.0D));
    }


    public String getInfo() {
        return String.valueOf((int) delay.getInput()) + "ms";
    }

    @Override
    public void onEnable() {
        clearPackets();
        target = null;
    }

    @Override
    public void onDisable() {
        clearPackets();
        target = null;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onTick(TickEvent e) {
        if (mc.thePlayer == null)
            return;

        if (target == null)
            return;

        if (!timer.hasTimeElapsed((long) (delay.getInput()), true))
            return;

        clearPackets();

        target = null;
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent e) {
        if (mc.thePlayer == null)
            return;

        if (target == null)
            return;

        if (e.getPacket() instanceof S01PacketPong)
            return;

        if (e.getPacket() instanceof S19PacketEntityStatus)
            return;

        if (e.getPacket() instanceof S08PacketPlayerPosLook) {
            clearPackets();
            return;
        }

        if (e.getPacket() instanceof S21PacketChunkData) {
            clearPackets();
            return;
        }

        if (e.getPacket() instanceof S26PacketMapChunkBulk) {
            clearPackets();
            return;
        }

        if (e.getPacket() instanceof S02PacketChat)
            return;

        if (e.getPacket() instanceof S40PacketDisconnect) {
            clearPackets();
            return;
        }

        if (e.getPacket() instanceof S13PacketDestroyEntities) {
            S13PacketDestroyEntities s13 = (S13PacketDestroyEntities) e.getPacket();

            for (int id : s13.getEntityIDs()) {
                if (id == target.getEntityId()) {
                    clearPackets();
                    target = null;
                }
            }
        }

        if (e.getPacket() instanceof S14PacketEntity) {
            S14PacketEntity s14 = (S14PacketEntity) e.getPacket();

            if (s14.getEntity(mc.theWorld).getEntityId() == mc.thePlayer.getEntityId())
                return;

            if (s14.getEntity(mc.theWorld).getEntityId() != target.getEntityId()) {
                return;
            } else {
                x += s14.func_149062_c();
                y += s14.func_149061_d();
                z += s14.func_149064_e();
            }
        }

        if (e.getPacket() instanceof S12PacketEntityVelocity) {
            S12PacketEntityVelocity s12 = (S12PacketEntityVelocity) e.getPacket();

            if (s12.getEntityID() == mc.thePlayer.getEntityId())
                return;

            if (s12.getEntityID() != target.getEntityId())
                return;
        }

        if (e.getPacket() instanceof S18PacketEntityTeleport) {
            S18PacketEntityTeleport s18 = (S18PacketEntityTeleport) e.getPacket();

            if (s18.getEntityId() == mc.thePlayer.getEntityId())
                return;

            if (s18.getEntityId() != target.getEntityId()) {
                return;
            } else {
                x = s18.getX();
                y = s18.getY();
                z = s18.getZ();
            }
        }

        packets.add(e.getPacket());
        e.setCanceled(true);
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent e) {
        if (e.target instanceof EntityPlayer) {
            target = (EntityPlayer) e.target;
        }
    }

    @SubscribeEvent
    public void onRenderWorldLastEvent(RenderWorldLastEvent e) {
        if (target == null)
            return;

        RenderUtils.db(new Vec3(target.serverPosX / 32.0, target.serverPosY / 32.0, target.serverPosZ / 32.0), new Color(0, 255, 0).getRGB());
    }

    private void clearPackets() {
        if (!packets.isEmpty()) {
            packets.forEach(PacketUtils::receivePacketNoEvent);
            packets.clear();
        }
    }
}