package parkinson.style;

import java.awt.Color;

import repast.simphony.valueLayer.ValueLayer;
import repast.simphony.visualizationOGL2D.ValueLayerStyleOGL;

public class CytoLayer2DStyle implements ValueLayerStyleOGL {

    private ValueLayer layer;
    
    private final double MAX_VALUE = 1.0;
    
    public CytoLayer2DStyle() {}

    @Override
    public void init(ValueLayer layer) {
        this.layer = layer;
    }

    @Override
    public float getCellSize() {
        return 15.0f; 
    }

    @Override
    public Color getColor(double... coordinates) {
    	double val = layer.get(coordinates);
        double normalizedVal = Math.max(0.0, Math.min(MAX_VALUE, val)) / MAX_VALUE;
        
        int blue = (int) (normalizedVal * 255);
        
        return new Color(255 - blue, 255 - blue, 255, 255);
    }
}
