package keystrokesmod.module.impl.other;

import com.sun.jna.WString;
import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoRegister extends Module {
    private SliderSetting characters;

    String paswor = "pwpwpw";

    public AutoRegister() {
        super("AutoRegister", category.other);
        this.registerSetting(new DescriptionSetting("Your Password is:" + paswor));
        this.registerSetting(characters = new SliderSetting("Password Lenght", 6.0, 6.0, 10.0, 2));
    }

    public void onUpdate() {
        if(characters.getInput() == 6.0){
            String paswor = "pwpwpw";
        }

        if(characters.getInput() == 8.0){
            String paswor = "pwpwpwpw";
        }
        if(characters.getInput() == 10.0){
            String paswor = "pwpwpwpwpw";
        }
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if(characters.getInput() == 6.0){
            String paswor = "pwpwpw";
        }

        if(characters.getInput() == 8.0){
            String paswor = "pwpwpwpw";
        }
        if(characters.getInput() == 10.0){
            String paswor = "pwpwpwpwpw";
        }

        sendChatMessage("/register " + paswor + " " + paswor);
        this.disable();

    }

    private void sendChatMessage(String message) {
        Minecraft.getMinecraft().thePlayer.sendChatMessage(message);
    }

    private void sendPrivateMessage(String message) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§u[RAVEN BFUN]§r " + message));
        this.disable();
    }
}