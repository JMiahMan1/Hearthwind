package folk.sisby.antique_atlas.gui.core;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.client.input.MouseButtonEvent;

public class ButtonComponent extends Component {
   protected final List<IButtonListener> listeners = new ArrayList<>();
   protected SoundEvent clickSound = SoundEvents.UI_BUTTON_CLICK.value();

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
      if (!this.isClipped && event.button() == 0 && this.isMouseOver(event.x(), event.y())) {
         this.onClick();
         return true;
      } else {
         return super.mouseClicked(event, bl);
      }
   }

   public void onClick() {
      if (this.clickSound != null) {
         Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(this.clickSound, 1.0F));
      }

      for (IButtonListener listener : this.listeners) {
         listener.onClick(this);
      }
   }

   public void addListener(IButtonListener listener) {
      this.listeners.add(listener);
   }
}
