package graph.editor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.MouseInputListener;

public class GraphComponent extends JComponent implements MouseInputListener, KeyListener {

	private static final long serialVersionUID = 1L;

	private static int n = 0;
	List<Vertex> vertices = new ArrayList<Vertex>();
	Vertex currentVertex = null;
	private List<Color> colors = new ArrayList<>();
	private int dx = 0;
	private int dy = 0;
	private static final Color[] colorList = new Color[] { Color.BLACK, Color.BLUE, Color.CYAN, Color.GREEN,
			Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, Color.YELLOW };

	private RectangularShape shapeSample = new Ellipse2D.Double(0, 0, 10, 10);

	List<Edge> edges = new ArrayList<Edge>();
	Edge currentEdge = null;
	private RectangularShape currentJointPoint = null;

	public GraphComponent() {
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (isOpaque()) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
		}
		Graphics2D g2 = (Graphics2D) g;
		for (int i = 0; i < vertices.size(); i++) {
			Vertex v = vertices.get(i);
			g.setColor(colors.get(i));
			v.draw(g2);
		}
		for (Edge e : edges) {
			g.setColor(getForeground());
			e.draw(g2);
		}
	}

	private Vertex getVertex(int x, int y) {
		for (int i = vertices.size() - 1; i >= 0; i--) {
			Vertex v = vertices.get(i);
			if (v.contains(x, y)) {
				dx = (int) (x - v.getShape().getCenterX());
				dy = (int) (y - v.getShape().getCenterY());
				System.out.println(dx);
				System.out.println(dy);
				return v;
			}
		}
		return null;
	}

	private RectangularShape getJointPoint(int x, int y) {
		for (Edge e : edges) {
			RectangularShape jp = e.getJointPoint(x, y);
			if (jp != null)
				return jp;
		}
		return null;
	}

	private static final double EDGE_EPSILON = 2.0;

	private Edge getEdge(int x, int y) {
		for (Edge e : edges)
			if (e.contains(x, y, EDGE_EPSILON))
				return e;
		return null;
	}

	public void setShapeType(RectangularShape sample) {
		shapeSample = sample;
	}

	private void removeVertex(Vertex v) {
		List<Edge> toRemove = new ArrayList<Edge>();
		for (Edge e : edges) {
			if (e.v1 == v || e.v2 == v)
				toRemove.add(e);
		}
		for (Edge e : toRemove)
			removeEdge(e);
		vertices.remove(v);
	}

	private void removeEdge(Edge e) {
		edges.remove(e);
	}

	private Vertex createVertex(int x, int y) {
		RectangularShape rs = newShape(x, y);
		Vertex v = new Vertex(rs, Integer.toString(n++));
		vertices.add(v);
		return v;
	}

	private void moveShape(RectangularShape rs, int x, int y) {
		rs.setFrameFromCenter(x, y, x + rs.getHeight() / 2, y + rs.getWidth() / 2);
	}

	private RectangularShape newShape(int x, int y) {
		RectangularShape rs = (RectangularShape) shapeSample.clone();
		moveShape(rs, x, y);
		Random r = new Random();
		colors.add(colorList[r.nextInt(9)]);
		return rs;
	}

	private Edge startEdge(Vertex v) {
		RectangularShape rs2 = newShape(0, 0);
		RectangularShape rs = v.shape;
		rs2.setFrameFromCenter((int) rs.getCenterX(), (int) rs.getCenterY(), (int) rs.getCenterX(),
				(int) rs.getCenterY());
		Edge e = new Edge(v, new Vertex(rs2, null));
		edges.add(e);
		return e;
	}

	private void endEdge(Edge e, int x, int y) {
		Vertex v = getVertex(x, y);
		if (v == null) {
			e.v2.shape.setFrameFromCenter(x, y, x + shapeSample.getHeight() / 2, y + shapeSample.getWidth() / 2);
			e.v2.label = Integer.toString(n++);
			vertices.add(e.v2);
		} else
			e.v2 = v;
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		// System.out.println("mouseClicked");
		if (arg0.getButton() == MouseEvent.BUTTON3) {
			int x = arg0.getX();
			int y = arg0.getY();
			Vertex v = getVertex(x, y);
			if (v != null) {
				removeVertex(v);
				repaint();
				return;
			}
			for (Edge edge : edges) {
				RectangularShape jp = edge.getJointPoint(x, y);
				if (jp != null) {
					edge.removeJointPoint(jp);
					repaint();
					return;
				}
			}
			Edge edge = getEdge(x, y);
			if (edge != null) {
				removeEdge(edge);
				repaint();
				return;
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		// System.out.println("mouseEntered");
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		// System.out.println("mouseExited");
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		requestFocusInWindow();
		if ((arg0.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) == InputEvent.BUTTON3_DOWN_MASK)
			return;
		int x = arg0.getX();
		int y = arg0.getY();
		Vertex v = getVertex(x, y);
		if (v == null) {
			currentJointPoint = getJointPoint(x, y);
		}
		if (v == null && currentJointPoint == null)
			v = createVertex(x, y);
		if (v != null && arg0.isAltDown())
			currentEdge = startEdge(v);
		else
			currentVertex = v;
		repaint();
	}

	public void addEdgeLabel(final Edge e) {
		final JTextField textField = new JTextField();
		textField.setSize(100, 20);
		textField.setLocation(e.labelPosition());
		e.textField = textField;
		this.add(textField);
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		// System.out.println("mouseReleased");
		if (currentEdge != null) {
			endEdge(currentEdge, arg0.getX(), arg0.getY());
			addEdgeLabel(currentEdge);
			currentEdge = null;
			repaint();
		}
		currentVertex = null;
		currentJointPoint = null;
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		// System.out.println("mouseDragged");
		if (currentVertex != null) {
			moveShape(currentVertex.getShape(), arg0.getX() - dx, arg0.getY() - dy);
			repaint();
		} else if (currentEdge != null) {
			moveShape(currentEdge.v2.getShape(), arg0.getX(), arg0.getY());
			repaint();
		} else if (currentJointPoint != null) {
			moveShape(currentJointPoint, arg0.getX(), arg0.getY());
			repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		// System.out.println("mouseMoved");
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		if (arg0.getKeyCode() == 32 && currentEdge != null) {
			currentEdge.addJointPoint();
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

}
