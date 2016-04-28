package graph.editor;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.RectangularShape;

public class Vertex {
	RectangularShape shape;
	String label;
	private static final Point2D DELTA_LABEL = new Point(1, -1);

	Vertex(RectangularShape rs, String label) {
		this.shape = rs;
		this.label = label;
	}

	public RectangularShape getShape() {
		return shape;
	}

	public void setShape(RectangularShape shape) {
		this.shape = shape;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean contains(int x, int y) {
		return shape.contains(x, y);
	}

	void draw(Graphics2D g2) {
		g2.fill(shape);
		if (label != null)
			g2.drawString(label, (int) (shape.getMaxX() + DELTA_LABEL.getX()),
					(int) (shape.getMinY() + DELTA_LABEL.getY()));
	}
}
