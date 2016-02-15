package lineProcessor;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

public class LineProcessor {

	public static void main(String[] args) {

		JFrame frame = new JFrame();
		frame.setSize(1000, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GoalComponent goalComponent = new GoalComponent();
		
		int maxWidthIndex;
		double minX;
		double maxX;
		double minY;
		double maxY;
		
		double[] goalWidths;
		double[] goalHeights;
		double[] goalXs;
		double[] goalYs;
		double[] x1Coordinates;
		double[] y1Coordinates;
		double[] x2Coordinates;
		double[] y2Coordinates;
		double[] goalArea;
		
		double marginOfError = 8.5; // Difference between detected contour width and points of lines
		List<Point2D.Double> points = new ArrayList<Point2D.Double>();
		Point2D.Double bottomLeftPoint;
		Point2D.Double bottomRightPoint;
		Point2D.Double topLeftPoint;
		Point2D.Double topRightPoint;
		Point2D.Double targetPoint;
		double targetDistance;
		
		NetworkTable.setServerMode();
		NetworkTable.setIPAddress("local");
		NetworkTable table = NetworkTable.getTable("GRIP");
		
		findGoal:
		while (true) {
			double timer = System.currentTimeMillis();
			// Waits a second before updating
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException ex) {
//				System.out.println(ex);
//			}

			try {
				// Get contour report
				goalWidths = table.getSubTable("myContoursReport").getNumberArray("width");
				goalHeights = table.getSubTable("myContoursReport").getNumberArray("height");
				goalArea = table.getSubTable("myContoursReport").getNumberArray("area");
				goalXs = table.getSubTable("myContoursReport").getNumberArray("centerX");
				goalYs = table.getSubTable("myContoursReport").getNumberArray("centerY");
				
				// Get line report
				
				x1Coordinates = table.getSubTable("myLinesReport").getNumberArray("x1");
				y1Coordinates = table.getSubTable("myLinesReport").getNumberArray("y1");
				x2Coordinates = table.getSubTable("myLinesReport").getNumberArray("x2");
				y2Coordinates = table.getSubTable("myLinesReport").getNumberArray("y2");
				
//				System.out.println(goalWidths);
				
				// Picks the goal with the biggest width for optimal shooting space
				maxWidthIndex = 0;
				for (int i = 0; i < goalWidths.length; i++) {
					if (goalWidths[i] > goalWidths[maxWidthIndex]) maxWidthIndex = i;
				}
				
				// Sets the goal bounds based on contour report
				minX = goalXs[maxWidthIndex] - goalWidths[maxWidthIndex]/2;
				maxX = goalXs[maxWidthIndex] + goalWidths[maxWidthIndex]/2;
				minY = goalYs[maxWidthIndex] - goalHeights[maxWidthIndex]/2;
				maxY = goalYs[maxWidthIndex] + goalHeights[maxWidthIndex]/2;
	
				// Get all the points form the line detection that are inside the contour bounds
				for (int i = 0; i < x1Coordinates.length; i++) {
					
					double x1 = x1Coordinates[i];
					double y1 = y1Coordinates[i];
					double x2 = x2Coordinates[i];
					double y2 = y2Coordinates[i];
					if (x1 >= minX - marginOfError && x1 <= maxX + marginOfError &&
						x2 >= minX - marginOfError && x2 <= maxX + marginOfError &&
						y1 >= minY - marginOfError && y1 <= maxY + marginOfError &&
						y2 >= minY - marginOfError && y2 <= maxY + marginOfError) {
						
						points.add(new Point2D.Double(x1, y1));
						points.add(new Point2D.Double(x2, y2));
					}
					
				}

				// Save all of the corner points
				Point2D.Double bottomMostPoint = points.get(0);
				for (Point2D.Double point : points) {
					if (point.getY() > bottomMostPoint.getY())
						bottomMostPoint = point;
				}
				bottomLeftPoint = bottomMostPoint;
				bottomRightPoint = bottomMostPoint;
				topLeftPoint = bottomMostPoint;
				topRightPoint = bottomMostPoint;
				if (bottomMostPoint.getX() < goalXs[maxWidthIndex]) {
					for (Point2D.Double point : points) {
						if (point.getX() > bottomRightPoint.getX() && point.getY() > goalYs[maxWidthIndex])
							bottomRightPoint = point;
						if (point.getY() < topRightPoint.getY() && point.getX() > goalXs[maxWidthIndex])
							topRightPoint = point;
					}

					targetPoint = new Point2D.Double(topRightPoint.getX() - (bottomRightPoint.getX() - bottomLeftPoint.getX()), topRightPoint.getY() + (bottomLeftPoint.getY() - bottomRightPoint.getY()));
					targetDistance = 10000;
					for (Point2D.Double point : points) {
						double currentDistance = Math.sqrt(Math.pow(targetPoint.getX() - point.getX(), 2) + Math.pow(targetPoint.getY() - point.getY(), 2));
						if (currentDistance < targetDistance) {
							topLeftPoint = point;
							targetDistance = currentDistance;
						}
					}
				}
				else {
					for (Point2D.Double point : points) {
						if (point.getX() <= bottomLeftPoint.getX() && point.getY() > goalYs[maxWidthIndex])
							bottomLeftPoint = point;
						if (point.getY() < topLeftPoint.getY() && point.getX() < goalXs[maxWidthIndex])
							topLeftPoint = point;
					}
					
					targetPoint = new Point2D.Double(topLeftPoint.getX() + (bottomRightPoint.getX() - bottomLeftPoint.getX()), topLeftPoint.getY() + (bottomRightPoint.getY() - bottomLeftPoint.getY()));
					targetDistance = 10000;
					for (Point2D.Double point : points) {
						double currentDistance = Math.sqrt(Math.pow(targetPoint.getX() - point.getX(), 2) + Math.pow(targetPoint.getY() - point.getY(), 2));
						if (currentDistance < targetDistance) {
							topRightPoint = point;
							targetDistance = currentDistance;
						}
					}
				}
	
				// Print results for testing
				System.out.println("Center: (" + goalXs[maxWidthIndex] + ", " + goalYs[maxWidthIndex] + ")");
				System.out.println("MinX: " + minX);
				System.out.println("MaxX: " + maxX);
				System.out.println("MinY: " + minY);
				System.out.println("MaxY: " + maxY);
				System.out.println("TopLeftPoint: (" + topLeftPoint.getX() + ", " + topLeftPoint.getY() + ")");
				System.out.println("BottomLeftPoint: (" + bottomLeftPoint.getX() + ", " + bottomLeftPoint.getY() + ")");
				System.out.println("TopRightPoint: (" + topRightPoint.getX() + ", " + topRightPoint.getY() + ")");
				System.out.println("BottomRightPoint: (" + bottomRightPoint.getX() + ", " + bottomRightPoint.getY() + ")");
				System.out.println();
				
				// Update display values
				goalComponent.setGoalBounds(minX, minY, maxX - minX, maxY - minY);
				goalComponent.setTopLeftPoint(topLeftPoint);
				goalComponent.setBottomLeftPoint(bottomLeftPoint);
				goalComponent.setTopRightPoint(topRightPoint);
				goalComponent.setBottomRightPoint(bottomRightPoint);
				goalComponent.setTargetPoint(targetPoint);
				
				// Display results for testing
				frame.remove(goalComponent);
				frame.add(goalComponent);
				frame.revalidate();
				frame.repaint();
				frame.setVisible(true);
				
				// Clear list for next iteration
				points.clear();
				System.out.println("Time in milliseconds: " + (System.currentTimeMillis() - timer));
			} catch (java.lang.ArrayIndexOutOfBoundsException ex) {
				System.out.println("Goal not found.");
				continue findGoal;
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
				continue findGoal;
			}
			
		}
		

	}

}
