package lineProcessor;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
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
		
		Object[] goalWidths;
		Object[] goalHeights;
		Object[] goalXs;
		Object[] goalYs;
		Object[] lineAngles;
		Object[] lineLengths;
		Object[] x1Coordinates;
		Object[] y1Coordinates;
		Object[] x2Coordinates;
		Object[] y2Coordinates;
		
		double marginOfError = 8.5; // Difference between detected contour width and points of lines
		List<Point2D.Double> points = new ArrayList<Point2D.Double>();
		Point2D.Double bottomLeftPoint;
		Point2D.Double bottomRightPoint;
		Point2D.Double topLeftPoint;
		Point2D.Double topRightPoint;
		Point2D.Double targetPoint;
		double targetDistance;
		
		List<double[]> goalAngles = new ArrayList<double[]>();
		double angleError = 3;
		
		NetworkTable.setServerMode();
		NetworkTable.setIPAddress("local");
		NetworkTable table = NetworkTable.getTable("GRIP");
		
		findGoal:
		while (true) {
			double timer = System.currentTimeMillis();

			try {

				// Populate angles list
				for (double angle = -45 + angleError/2; angle <= 45; angle += angleError) {
					double[] angleCount = {angle, 0};
					goalAngles.add(angleCount);
				}
				
				// Get contour report
				goalWidths = (Object[]) table.getSubTable("myContoursReport").getValue("width");
				goalHeights = (Object[]) table.getSubTable("myContoursReport").getValue("height");
				goalXs = (Object[]) table.getSubTable("myContoursReport").getValue("centerX");
				goalYs = (Object[]) table.getSubTable("myContoursReport").getValue("centerY");
				
				// Get line report
				lineAngles = (Object[]) table.getSubTable("myLinesReport").getValue("angle");
				lineLengths = (Object[]) table.getSubTable("myLinesReport").getValue("length");
				x1Coordinates = (Object[]) table.getSubTable("myLinesReport").getValue("x1");
				y1Coordinates = (Object[]) table.getSubTable("myLinesReport").getValue("y1");
				x2Coordinates = (Object[]) table.getSubTable("myLinesReport").getValue("x2");
				y2Coordinates = (Object[]) table.getSubTable("myLinesReport").getValue("y2");
				
				
				// Picks the goal with the biggest width for optimal shooting space
				maxWidthIndex = 0;
				for (int i = 0; i < goalWidths.length; i++) {
					if ((double) goalWidths[i] > (double) goalWidths[maxWidthIndex]) maxWidthIndex = i;
				}
				
				// Sets the goal bounds based on contour report
				minX = (double) goalXs[maxWidthIndex] - (double) goalWidths[maxWidthIndex]/2;
				maxX = (double) goalXs[maxWidthIndex] + (double) goalWidths[maxWidthIndex]/2;
				minY = (double) goalYs[maxWidthIndex] - (double) goalHeights[maxWidthIndex]/2;
				maxY = (double) goalYs[maxWidthIndex] + (double) goalHeights[maxWidthIndex]/2;
	
				// Get all the points form the line detection that are inside the contour bounds
				for (int i = 0; i < x1Coordinates.length; i++) {
					
					double x1 = (double) x1Coordinates[i];
					double y1 = (double) y1Coordinates[i];
					double x2 = (double) x2Coordinates[i];
					double y2 = (double) y2Coordinates[i];
					double angle = (double) lineAngles[i];
					double length = (double) lineLengths[i];
					if (x1 >= minX - marginOfError && x1 <= maxX + marginOfError &&
						x2 >= minX - marginOfError && x2 <= maxX + marginOfError &&
						y1 >= minY - marginOfError && y1 <= maxY + marginOfError &&
						y2 >= minY - marginOfError && y2 <= maxY + marginOfError) {

						points.add(new Point2D.Double(x1, y1));
						points.add(new Point2D.Double(x2, y2));
						if (length > 20) {
							angle = Math.abs(angle);
							angle -= 90;
							if (Math.abs(angle) > 45) {
								if (angle > 45) angle -= 45;
								else  {
									angle += 45;
								}
								goalAngles.get((int) Math.round((angle + 43.5) / angleError))[1]++;
							}
						}
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
				if ((double) bottomMostPoint.getX() < (double) goalXs[maxWidthIndex]) {
					for (Point2D.Double point : points) {
						if (point.getX() > bottomRightPoint.getX() && point.getY() > (double) goalYs[maxWidthIndex])
							bottomRightPoint = point;
						if (point.getY() < topRightPoint.getY() && point.getX() > (double) goalXs[maxWidthIndex])
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
						if (point.getX() <= bottomLeftPoint.getX() && point.getY() > (double) goalYs[maxWidthIndex])
							bottomLeftPoint = point;
						if (point.getY() < topLeftPoint.getY() && point.getX() < (double) goalXs[maxWidthIndex])
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
				System.out.print("Points: (");
				int mm = 2;
				for (Point2D.Double point : points) {
					System.out.print((mm / 2.0) + "[" + point.getX() + ", " + point.getY() + "]");
					mm++;
				}
				System.out.println(")");
				System.out.print("Angles: (");
				for (double[] angle : goalAngles) {
					System.out.print(Arrays.toString(angle) + ", ");
				}
				System.out.println(")");
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
				
				// Clear lists for next iteration
				points.clear();
				goalAngles.clear();
				System.out.println("Time in milliseconds: " + (System.currentTimeMillis() - timer));
			} catch (java.lang.ArrayIndexOutOfBoundsException ex) {
				
				// Clear lists for next iteration
				points.clear();
				goalAngles.clear();
				System.out.println("Goal not found.");
				continue findGoal;
			} catch (Exception ex) {
				
				// Clear lists for next iteration
				points.clear();
				goalAngles.clear();
				System.out.println(ex.getMessage());
				continue findGoal;
			}
			
		}
		

	}

}
