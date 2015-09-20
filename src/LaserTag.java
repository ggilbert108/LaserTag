import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;

public class LaserTag extends JPanel
{
    private double angle;
    private ArrayList<Line2D> mirrors;

    public LaserTag()
    {
        angle = 0;
        mirrors = new ArrayList<>();
        Line2D mirror1 = new Line2D.Double(0, -200, 200, 0);
        Line2D mirror2 = new Line2D.Double(-100, 200, 100, 200);
        mirrors.add(mirror1);
        mirrors.add(mirror2);
    }

    public void run()
    {
        while(true)
        {
            update();
            try
            {
                Thread.sleep(10);
            }
            catch(Exception e) {}
        }
    }

    private void update()
    {
        angle += 1;
        repaint();
    }

    @Override
    public void paintComponent(Graphics graphics)
    {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D)graphics;
        g.setStroke(new BasicStroke(2));
        g.translate(250, 250);


        ArrayList<Line2D> lasers = getLasers();

        drawLines(g, mirrors);
        g.setColor(Color.RED);
        drawLines(g, lasers);

    }

    private void drawLines(Graphics2D g, ArrayList<Line2D> lines)
    {
        for(Line2D line : lines)
        {
            drawLine(g, line);
        }
    }

    private void drawLine(Graphics2D g, Line2D line)
    {
        g.drawLine((int) line.getX1(), (int) line.getY1(), (int) line.getX2(), (int) line.getY2());
    }

    private void translateLine(Line2D line, double tx, double ty)
    {
        double x1 = line.getX1() + tx;
        double x2 = line.getX2() + tx;
        double y1 = line.getY1() + ty;
        double y2 = line.getY2() + ty;

        line.setLine(x1, y1, x2, y2);
    }

    private void lengthenLine(Line2D line)
    {
        double dx = line.getX2() - line.getX1();
        double dy = line.getY2() - line.getY1();

        line.setLine(line.getX1(), line.getY1(), line.getX2() + 100 * dx, line.getY2() + 100 * dy);
    }


    private void rotateLine(Line2D line, double angle, double tx, double ty)
    {
        translateLine(line, -tx, -ty);

        angle = Math.toRadians(angle);
        AffineTransform transformer = new AffineTransform();
        transformer = transformer.getRotateInstance(angle);

        Point2D p1 = new Point2D.Double(0, 0);
        Point2D p2 = new Point2D.Double(0, 0);
        transformer.transform(line.getP1(), p1);
        transformer.transform(line.getP2(), p2);

        line.setLine(p1, p2);

        translateLine(line, tx, ty);
    }

    private double getLineSlope(Line2D line)
    {
        double dx = line.getX2() - line.getX1();
        double dy = line.getY2() - line.getY1();

        return dy/dx;
    }

    private Point2D lineIntersection(Line2D a, Line2D b)
    {
        double m1 = getLineSlope(a);
        double c1 = a.getY1() - m1 * a.getX1();

        double m2 = getLineSlope(b);
        double c2 = b.getY1() - m2 * b.getX1();

        double intersectionX = (c2 - c1) / (m1 - m2);
        double intersectionY = m1 * intersectionX + c1;

        Point2D result = new Point2D.Double(intersectionX, intersectionY);
        return result;
    }

    private double getAngleBetweenLines(Line2D a, Line2D b)
    {
        double m1 = getLineSlope(a);
        double m2 = getLineSlope(b);

        double result = Math.atan((m1 - m2)/(1 + m1*m2));
        return Math.toDegrees(result);
    }

    private ArrayList<Line2D> getLasers()
    {
        ArrayList<Line2D> lasers = new ArrayList<>();

        Line2D initial = new Line2D.Double(0, 0, 0, 1000);
        rotateLine(initial, angle, 0, 0);

        int numReflections = 0;
        while(numReflections < 7)
        {
            lasers.add(initial);

            boolean intersected = false;
            for(Line2D mirror : mirrors)
            {
                if(mirror.intersectsLine(initial))
                {
                    if(mirror.ptSegDist(initial.getP1()) < 0.1)
                        continue;

                    Point2D intersection = lineIntersection(initial, mirror);
                    initial.setLine(initial.getP1(), intersection);

                    double angle = getAngleBetweenLines(mirror, initial);
                    angle = 180 + 2 * angle;
                    Line2D nextLine = new Line2D.Double(intersection, initial.getP1());
                    rotateLine(nextLine, angle, intersection.getX(), intersection.getY());
                    lengthenLine(nextLine);

                    initial = nextLine;
                    intersected = true;
                    break;
                }
            }
            if(!intersected)
                break;
            numReflections++;
        }

        return lasers;
    }


    public static void main(String[] args)
    {
        LaserTag laserTag = new LaserTag();
        JFrame frame = new JFrame();
        frame.setSize(500, 500);
        frame.add(laserTag);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        laserTag.run();
    }
}
