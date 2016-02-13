import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

public class LineProcessor {

	public static void main(String[] args) {

		int maxWidthIndex;
		double minX;
		double maxX;
		double minY;
		double maxY;
		
		double marginOfError = 5; // Difference between detected contour width and points of lines
		List<Point2D.Double> points = new ArrayList<Point2D.Double>();
		Point2D.Double bottomLeftPoint;
		Point2D.Double bottomRightPoint;
		Point2D.Double topLeftPoint;
		Point2D.Double topRightPoint;
		
		
		NetworkTable.setServerMode();
		NetworkTable.setIPAddress("local");
		NetworkTable table = NetworkTable.getTable("GRIP");
				
		while (true) {
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				System.out.println(ex);
			}

			// Get contour report
			Object[] goalWidths = (Object[]) table.getSubTable("myContoursReport").getValue("width");
			Object[] goalHeights = (Object[]) table.getSubTable("myContoursReport").getValue("height");
			Object[] goalXs = (Object[]) table.getSubTable("myContoursReport").getValue("centerX");
			Object[] goalYs = (Object[]) table.getSubTable("myContoursReport").getValue("centerY");
			
			// Get line report
			Object[] x1Coordinates = (Object[]) table.getSubTable("myLinesReport").getValue("x1");
			Object[] y1Coordinates = (Object[]) table.getSubTable("myLinesReport").getValue("y1");
			Object[] x2Coordinates = (Object[]) table.getSubTable("myLinesReport").getValue("x2");
			Object[] y2Coordinates = (Object[]) table.getSubTable("myLinesReport").getValue("y2");
			
			// Picks the goal with the biggest width for optimal shooting space
			maxWidthIndex = 0;
			for (int i = 0; i < goalWidths.length; i++) {
				if ((double)goalWidths[i] > (double)goalWidths[maxWidthIndex]) maxWidthIndex = i;
			}
			
			// Sets the goal bounds based on contour report
			minX = (double)goalXs[maxWidthIndex] - (double)goalWidths[maxWidthIndex]/2;
			maxX = (double)goalXs[maxWidthIndex] + (double)goalWidths[maxWidthIndex]/2;
			minY = (double)goalYs[maxWidthIndex] - (double)goalHeights[maxWidthIndex]/2;
			maxY = (double)goalYs[maxWidthIndex] + (double)goalHeights[maxWidthIndex]/2;

			// Get all the points form the line detection that are inside the contour bounds
			for (int i = 0; i < x1Coordinates.length; i++) {
				
				double x1 = (double)x1Coordinates[i];
				double y1 = (double)y1Coordinates[i];
				double x2 = (double)x2Coordinates[i];
				double y2 = (double)y2Coordinates[i];
				if (x1 >= minX - marginOfError && x1 <= maxX + marginOfError &&
					x2 >= minX - marginOfError && x2 <= maxX + marginOfError &&
					y1 >= minY - marginOfError && y1 <= maxY + marginOfError &&
					y1 >= minY - marginOfError && y1 <= maxY + marginOfError) {
					
					points.add(new Point2D.Double(x1, y1));
					points.add(new Point2D.Double(x2, y2));
				}
				
			}

			// Save all of the corner points
			topLeftPoint = new Point2D.Double((double)goalXs[maxWidthIndex], (double)goalYs[maxWidthIndex]);
			bottomLeftPoint = new Point2D.Double((double)goalXs[maxWidthIndex], (double)goalYs[maxWidthIndex]);
			topRightPoint = new Point2D.Double((double)goalXs[maxWidthIndex], (double)goalYs[maxWidthIndex]);
			bottomRightPoint = new Point2D.Double((double)goalXs[maxWidthIndex], (double)goalYs[maxWidthIndex]);
			for (Point2D.Double point : points) {
				if (point.getX() < minX + marginOfError) {
					if (point.getY() < topLeftPoint.getY()) topLeftPoint = point;
					else if (point.getY() > bottomLeftPoint.getY()) bottomLeftPoint = point;
				} else if (point.getX() > maxX - marginOfError) {
					if (point.getY() < topRightPoint.getY()) topRightPoint = point;
					else if (point.getY() > bottomRightPoint.getY()) bottomRightPoint = point;
				}
			}

			// Print results for testing
			System.out.println("Center: (" + (double)goalXs[maxWidthIndex] + ", " + (double)goalYs[maxWidthIndex] + ")");
			System.out.println("MinX: " + minX);
			System.out.println("MaxX: " + maxX);
			System.out.println("MinY: " + minY);
			System.out.println("MaxY: " + maxY);
			System.out.println("TopLeftPoint: (" + topLeftPoint.getX() + ", " + topLeftPoint.getY() + ")");
			System.out.println("BottomLeftPoint: (" + bottomLeftPoint.getX() + ", " + bottomLeftPoint.getY() + ")");
			System.out.println("TopRightPoint: (" + topRightPoint.getX() + ", " + topRightPoint.getY() + ")");
			System.out.println("BottomRightPoint: (" + bottomRightPoint.getX() + ", " + bottomRightPoint.getY() + ")");
			System.out.println();
			
			// Clear list for next iteration
			points.clear();
			
		}
		

	}

}
