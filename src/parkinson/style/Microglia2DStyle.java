package parkinson.style;

import java.io.IOException;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.VSpatial;

public class Microglia2DStyle extends DefaultStyleOGL2D {
	
	@Override
	public VSpatial getVSpatial(Object agent, VSpatial spatial) {
		try {
			return shapeFactory.createImage("icons/gliaCell.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
	    return spatial;
	}

	  public int getBorderSize(Object object) {
	    return 1;
	  }
}
