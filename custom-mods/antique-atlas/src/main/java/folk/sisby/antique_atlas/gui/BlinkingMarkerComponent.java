package folk.sisby.antique_atlas.gui;

import folk.sisby.antique_atlas.MarkerTexture;
import folk.sisby.antique_atlas.gui.core.BlinkingTextureComponent;

public class BlinkingMarkerComponent extends BlinkingTextureComponent implements MarkerModal.IMarkerTypeSelectListener {
   @Override
   public void onSelectMarkerType(MarkerTexture markerTexture) {
      this.setTexture(markerTexture.id(), 32, 32);
   }
}
