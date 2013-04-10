import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.esri.arcgis.addins.desktop.Tool;
import com.esri.arcgis.arcmapui.IMxDocument;
import com.esri.arcgis.carto.IActiveView;
import com.esri.arcgis.carto.IFeatureLayer;
import com.esri.arcgis.carto.IFeatureSelection;
import com.esri.arcgis.carto.ILayer;
import com.esri.arcgis.carto.IMap;
import com.esri.arcgis.display.IScreenDisplay;
import com.esri.arcgis.framework.IApplication;
import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geodatabase.IFeatureClass;
import com.esri.arcgis.geodatabase.ISelectionSet;
import com.esri.arcgis.geodatabase.ITable;
import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.geometry.IPolyline;
import com.esri.arcgis.interop.AutomationException;

public class upStreamAccumulation extends Tool {
	
	//TODO Clear selection before tool execution
	//TODO Create wait icon

	/**
	 * Called when the tool is activated by clicking it.
	 * 
	 * @exception java.io.IOException if there are interop problems.
	 * @exception com.esri.arcgis.interop.AutomationException if the component throws an ArcObjects exception.
	 */
	
	IActiveView activeView;
	IApplication app;
	IMap activeMap;
	IMxDocument mxDoc;
	IScreenDisplay screenDisplay;
	ISelectionSet selectionSet;
	IFeatureSelection featureSelection;
	ITable selectionTable;
	ILayer activeLayer;
	IFeatureClass activeFC;
	IFeatureLayer activeFeatureLayer;
	IPoint mouseLocation;
	
	int numOfFeats;
	
	ArrayList<IPoint> pnts = new ArrayList<IPoint>();
		
	@Override
	public void activate() throws IOException, AutomationException {

	}
	
	@Override
	public void mousePressed(MouseEvent me) {

		try {
			// get the document object
			mxDoc = (IMxDocument) app.getDocument();
			// grab the layer that is selected in the TOC
			activeFeatureLayer = (IFeatureLayer) mxDoc.getSelectedItem();
			activeLayer = (ILayer) mxDoc.getSelectedLayer();
			// grab the feature class of the layer
			activeFC = (IFeatureClass) activeFeatureLayer.getFeatureClass();
			// get map with focus
			activeMap = (IMap) mxDoc.getFocusMap();
			// the the active view of the map
			activeView = (IActiveView) mxDoc.getActivatedView();
			// grab selection from active view and clear the selection
			featureSelection = (IFeatureSelection) activeFeatureLayer;
			selectionSet = featureSelection.getSelectionSet();
			//featureSelection.clear();
			//activeView.refresh();
			
			// find the number of features in the FC for loop
			numOfFeats = (int) activeFC.featureCount(null);
						
			// get X,Y of mouse location and add to arraylist
			mouseLocation = (IPoint) mxDoc.getCurrentLocation();
			pnts.add(mouseLocation);
			
			while (true) {
				if (pnts.isEmpty()) {
					break;
				} else {
					findConnection(pnts);
				}
				
			}
			
		} catch(Exception ex) {
			JOptionPane.showMessageDialog(null, "ERROR: Make sure you've selected a line feature in the TOC");
		}
	}
	
	public void findConnection(ArrayList<IPoint> startPnts) {
		// create and ensure temporary variable is empty
		ArrayList<IPoint> pntsTemp = new ArrayList<IPoint>();
		
		try {

			// iterate through points
			for (IPoint pnt : startPnts) {
				// loop through lines
				for(int i = 0; i < numOfFeats; i++) {
					
					// grab geometry for selected feature
					IFeature activeFeature = (IFeature) activeFC.getFeature(i);
					IPolyline activeLine = (IPolyline) activeFeature.getShape();
					
					// get endPt x and y
					IPoint endPt = (IPoint) activeLine.getToPoint();
					double endX = (double) endPt.getX();
					double endY = (double) endPt.getY();
					
					// get startPt
					IPoint startPt = (IPoint) activeLine.getFromPoint();
					
					//get argument point x and y
					double argX = (double) pnt.getX();
					double argY = (double) pnt.getY();
					
					double deltaX = Math.abs(argX - endX);	
					double deltaY = Math.abs(argY - endY);
					double delta = Math.sqrt(deltaX + deltaY);
					
					if (delta < 5) {
						selectionSet.add(i);
						activeView.refresh();
						pntsTemp.add(startPt);
					}
				}
			}
			// create new start points list
			pnts.clear();
			for (IPoint i : pntsTemp) {
				pnts.add(i);
			}
			
			pntsTemp.clear();
		    
		} catch(Exception ex) {
			JOptionPane.showMessageDialog(null, "ERROR in findConnection()");
		}
	}
	
	@Override
	public void init(IApplication app) throws IOException, AutomationException{
		this.app = app;
	}
	

}
