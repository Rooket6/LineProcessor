package lineProcessor;
import java.awt.geom.Point2D;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JComponent;

public class GoalComponent extends JComponent {

	private int radius;
	
	private Rectangle goalBounds;
	
	private Point2D.Double topLeftPoint;
	private Point2D.Double bottomLeftPoint;
	private Point2D.Double topRightPoint;
	private Point2D.Double bottomRightPoint;
	
	public GoalComponent() {
		
		this.radius = 8;

		this.goalBounds = new Rectangle();
		
		this.topLeftPoint = new Point2D.Double(0, 0);
		this.bottomLeftPoint = new Point2D.Double(0, 0);
		this.topRightPoint = new Point2D.Double(0, 0);
		this.bottomRightPoint = new Point2D.Double(0, 0);
		
	}
	
	public void setGoalBounds(double x, double y, double width, double height) {
		goalBounds.setBounds((int) Math.round(x), (int) Math.round(y), (int) Math.round(width), (int) Math.round(height));
	}
	
	public void setTopLeftPoint(Point2D.Double point) {
		this.topLeftPoint = point;
	}
	
	public void setBottomLeftPoint(Point2D.Double point) {
		this.bottomLeftPoint = point;
	}
	
	public void setTopRightPoint(Point2D.Double point) {
		this.topRightPoint = point;
	}
	
	public void setBottomRightPoint(Point2D.Double point) {
		this.bottomRightPoint = point;
	}
	
	public void paintComponent(Graphics g) {
		
		Graphics g2 = (Graphics2D) g;

		g2.setColor(Color.BLUE);
		g2.fillOval((int) Math.round(topLeftPoint.getX() - radius/2), (int) Math.round(topLeftPoint.getY() - radius/2), radius, radius);
		g2.fillOval((int) Math.round(bottomLeftPoint.getX() - radius/2), (int) Math.round(bottomLeftPoint.getY() - radius/2), radius, radius);
		g2.fillOval((int) Math.round(topRightPoint.getX() - radius/2), (int) Math.round(topRightPoint.getY() - radius/2), radius, radius);
		g2.fillOval((int) Math.round(bottomRightPoint.getX() - radius/2), (int) Math.round(bottomRightPoint.getY() - radius/2), radius, radius);
		
		g2.setColor(Color.RED);
		g2.drawRect(goalBounds.x, goalBounds.y, goalBounds.width, goalBounds.height);
		
	}
	
}
