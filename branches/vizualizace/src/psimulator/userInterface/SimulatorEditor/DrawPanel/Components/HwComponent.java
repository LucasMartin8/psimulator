package psimulator.userInterface.SimulatorEditor.DrawPanel.Components;

import java.awt.Graphics;
import java.awt.Graphics2D;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.HwTypeEnum;
import psimulator.userInterface.SimulatorEditor.DrawPanel.ZoomManager;
import psimulator.userInterface.imageFactories.AbstractImageFactory;

/**
 *
 * @author Martin
 */
public class HwComponent extends AbstractHwComponent {

    private HwTypeEnum hwComponentType;
    private String imagePath;

    public HwComponent(AbstractImageFactory imageFactory, ZoomManager zoomManager, 
            HwTypeEnum hwComponentType, int interfacesCount, String imagePath, String name) {
        super(imageFactory, zoomManager, interfacesCount);

        this.hwComponentType = hwComponentType;
        this.imagePath = imagePath;
        
        //create image in default zoom
        bi = imageFactory.getImage(hwComponentType, imagePath, zoomManager.getIconWidthDefaultZoom(), false);
        
        // set image width and height in default zoom
        defaultZoomWidth = bi.getWidth();
        defaultZoomHeight = bi.getHeight();
    }
    

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
     
        if (isMarked()) {
            bi = imageFactory.getImage(hwComponentType, imagePath, zoomManager.getIconWidth(), true);
        } else {
            bi = imageFactory.getImage(hwComponentType, imagePath, zoomManager.getIconWidth(), false);
        }

        g2.drawImage(bi, getX(), getY(), null);
    }
}