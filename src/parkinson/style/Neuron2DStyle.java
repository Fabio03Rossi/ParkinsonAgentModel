package parkinson.style;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import parkinson.Neuron;
import parkinson.NeuronState;
import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.VSpatial;
import saf.v3d.scene.VShape;
import saf.v3d.scene.VImage2D;

public class Neuron2DStyle extends DefaultStyleOGL2D {
	
	@Override
	public VSpatial getVSpatial(Object agent, VSpatial spatial) {
	    if (spatial == null) {
	        /*
	    	try {
				return shapeFactory.createImage("icons/neuron.png");
			} catch (IOException e) {
				e.printStackTrace();
			}*/
	    	return shapeFactory.createCircle(4, 10, true);
	    }
	    return spatial;
	}
	
	@Override
    public Color getColor(Object agent) {
        if (agent instanceof Neuron neuron) {
            float ratio = (float) (neuron.getHealth() / neuron.getMaxHealth());
            ratio = Math.max(0f, Math.min(1f, ratio));
            
            return new Color(1f - ratio, 0f, ratio);
        }
        return Color.GRAY;
    }

	  public Color getBorderColor(Object agent) {
		  if(agent instanceof Neuron neuron) {
			  if(neuron.getState() == NeuronState.DEGENERATED_DEATH) {
				  return Color.RED;
			  }
		  }
		  return Color.BLACK;
	  }

	  public int getBorderSize(Object object) {
	    return 1;
	  }
	  
	  public float getScale(Object object) {
	    return 1f;
	  }
}
