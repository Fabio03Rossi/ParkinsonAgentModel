package parkinson.style;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import parkinson.Neuron;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.VSpatial;
import saf.v3d.scene.VShape;

public class Neuron2DStyle extends DefaultStyleOGL2D {
	
	@Override
	public VSpatial getVSpatial(Object agent, VSpatial spatial) {
	    if (spatial == null) {
			try {
				return shapeFactory.createImage("icons/neuron.png");
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    return spatial;
	}
	
	public float getScale(Object object) {
		return .01f;
	}
	
	@Override
    public Color getColor(Object agent) {
        if (!(agent instanceof Neuron neuron)) return Color.GRAY;
        
        float ratio = (float) (neuron.getHealth() / neuron.getMaxHealth());
        ratio = Math.max(0f, Math.min(1f, ratio));
        
        return new Color(1f - ratio, ratio, 0f);
    }

	  public int getBorderSize(Object object) {
	    return 1;
	  }
}
