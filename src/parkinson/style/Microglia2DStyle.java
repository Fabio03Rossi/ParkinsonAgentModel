package parkinson.style;

import java.io.IOException;

import parkinson.Microglia;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.VSpatial;

public class Microglia2DStyle extends DefaultStyleOGL2D {
	
	@Override
	public VSpatial getVSpatial(Object agent, VSpatial spatial) {
		var microglia = (Microglia) agent;
		try {
		    return switch(microglia.getState()) {
				case RESTING, PHAGOCITATION -> shapeFactory.createImage("icons/glialCell.png");
				case DAMAGE_PERCEIVED -> shapeFactory.createImage("icons/movingGlialCell.png");
			};
		} catch (IOException e) {
			e.printStackTrace();
		}
		return spatial;
	}
	
	public float getScale(Object object) {
		return .1f;
	}
	
	public int getBorderSize(Object object) {
	    return 1;
	}
}
