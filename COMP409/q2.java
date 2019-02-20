import java.security.InvalidParameterException;
import java.util.*;

class Point {
    double x, y;

    // Set of edges attached to this point
    public ArrayList<Edge> edges;

    public Point(double bx, double by) {
        x = bx;
        y = by;
    }

    // Returns true if the given point is the same as this one.
    // nb: should use machine epsilon.
    public boolean same(Point b) {
        return (x == b.x && y == b.y);
    }

    // Add an edge connection if not present; lazily creates the edge array/
    public void addEdge(Edge e) {
        if (edges == null) edges = new ArrayList<>();
        if (!edges.contains(e))
            edges.add(e);
    }

    public String toString() {
        return "(" + x + "," + y + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Point) {
            return ((Point) obj).x == x && ((Point) obj).y == y;
        } else {
            return false;
        }
    }
}

class Edge {
    Point p, q;

    public Edge(Point p1, Point p2) {
        p = p1;
        q = p2;
    }

    // Utility routine -- 2d cross-product (signed area of a triangle) test for orientation.
    public int sat(double p0x, double p0y, double p1x, double p1y, double p2x, double p2y) {
        double d = (p1x - p0x) * (p2y - p0y) - (p2x - p0x) * (p1y - p0y);
        if (d < 0) return -1;
        if (d > 0) return 1;
        return 0;
    }

    // Returns true if the given edge intersects this edge.
    public boolean intersects(Edge e) {
        int s1 = sat(p.x, p.y, q.x, q.y, e.p.x, e.p.y);
        int s2 = sat(p.x, p.y, q.x, q.y, e.q.x, e.q.y);
        if (s1 == s2 || (s1 == 0 && s2 != 0) || (s2 == 0 && s1 != 0)) return false;
        s1 = sat(e.p.x, e.p.y, e.q.x, e.q.y, p.x, p.y);
        s2 = sat(e.p.x, e.p.y, e.q.x, e.q.y, q.x, q.y);
        if (s1 == s2 || (s1 == 0 && s2 != 0) || (s2 == 0 && s1 != 0)) return false;
        return true;
    }

    public double GetLength() {
        return Math.sqrt((p.x - q.x) * (p.x - q.x) + (p.y - q.y) * (p.y - q.y));
    }

    public String toString() {
        return "<" + p + "," + q + ">";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Edge) {
            return (((Edge) obj).p.equals(p) && ((Edge) obj).q.equals(q)) || (((Edge) obj).p.equals(q) && ((Edge) obj).q.equals(p));
        }
        return false;
    }
}

class Triangle {

    public Triangle(Point a, Point b, Point c) {
        vertex.add(a);
        vertex.add(b);
        vertex.add(c);
        edges.add(new Edge(a, b));
        edges.add(new Edge(b, c));
        edges.add(new Edge(a, c));
    }

    ArrayList<Point> vertex = new ArrayList<>();
    ArrayList<Edge> edges = new ArrayList<>();
    public boolean isOccupied = false;

    /**
     * @param t another triangle
     * @return the adjacent edge with t
     */
    public Edge GetAdjacentEdge(Triangle t) {
        if (!this.equals(t)) {
            for (Edge edge : this.edges) {
                if (t.edges.contains(edge)) {
                    assert q2.edges.contains(edge);
                    return edge;
                }
            }
            return null;
        }
        return null;
    }

    /**
     * @param adjacentEdge the adjacent edge
     * @return the point opposite the adjacent edge in the given triangle
     */
    public Point GetOppositePoint(Edge adjacentEdge) {
        ArrayList<Point> possiblePoints = new ArrayList<>(vertex);
        possiblePoints.remove(adjacentEdge.p);
        possiblePoints.remove(adjacentEdge.q);
        return possiblePoints.get(0);
    }

    /**
     * Use the cosine theorem to compute the angle opposite the adjacent edge in the given triangle
     *
     * @param adjacentEdge the adjacent edge
     * @return the opposite angle in radians
     */

    public double GetOppositeAngle(Edge adjacentEdge) {
        if (edges.contains(adjacentEdge)) {
            ArrayList<Edge> limb = new ArrayList<>(edges);
            limb.remove(adjacentEdge);
            double a = limb.get(0).GetLength();
            double b = limb.get(1).GetLength();
            double c = adjacentEdge.GetLength();

            // Use cosine theorem
            return Math.acos((a * a + b * b - c * c) / (2 * a * b));
        } else {
            throw new InvalidParameterException("adjacent edge must be one of the triangle's edges");
        }
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Triangle && ((Triangle) obj).vertex.size() == vertex.size()) {
            return ((Triangle) obj).vertex.containsAll(vertex);
        }
        return false;
    }
}

class TrianglePair {
    public Triangle t1;
    public Triangle t2;
    public final Edge adjacentEdge;
    public final Edge newEdge;

    public TrianglePair(Triangle t1, Triangle t2) {
        this.t1 = t1;
        this.t2 = t2;
        adjacentEdge = t1.GetAdjacentEdge(t2);
        newEdge = new Edge(t1.GetOppositePoint(adjacentEdge), t2.GetOppositePoint(adjacentEdge));
    }

    public TrianglePair(Triangle t1, Triangle t2, Edge newEdge, Edge adjacentEdge) {
        this.t1 = t1;
        this.t2 = t2;
        this.newEdge = newEdge;
        this.adjacentEdge = adjacentEdge;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TrianglePair) {
            return (((TrianglePair) obj).t1.equals(t1) && ((TrianglePair) obj).t2.equals(t2))
                    || (((TrianglePair) obj).t1.equals(t2) && ((TrianglePair) obj).t2.equals(t1));
        }
        return false;
    }
}


public class q2 {

    public static int n, t; // constants
    volatile public static boolean isDone = false;
    volatile public static Point[] points;
    volatile public static int totalFlip = 0;
    volatile public static ArrayList<Edge> edges = new ArrayList<>();
    volatile public static ArrayList<Triangle> triangles = new ArrayList<>();
    volatile public static Queue<TrianglePair> trianglePairs = new LinkedList<>();
    public static int[] numberOfFlip;

    // Returns true if any existing edge intersects this one
    public static boolean intersection(Edge f) {
        for (Edge e : edges) {
            if (f.intersects(e)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get all the triangles based on the edges and points
     */
    public synchronized static void GetAllTriangles() {
        triangles.clear();
        for (Edge edge : edges) {
            for (Point point : points) {
                if (point.equals(edge.p) || point.equals(edge.q)) {
                    // skip myself
                    continue;
                } else {
                    Edge a = new Edge(edge.p, point);
                    Edge b = new Edge(edge.q, point);
                    if (edges.contains(a) && edges.contains(b)) {
                        Triangle triangle = new Triangle(point, edge.p, edge.q);
                        if (!triangles.contains(triangle)) {
                            triangles.add(triangle);
                        }
                    }
                }
            }
        }
    }

    /**
     * Randomly choose two triangles from the given list, and add the pair to list.
     * This methods avoid adding duplicate pairs
     * (same triangle with different order are also considered as same pair)
     */
    public static void GetTrianglePairsList() {
        synchronized (q2.edges) {
            trianglePairs.clear();
            for (Triangle t1 : q2.triangles) {
                for (Triangle t2 : q2.triangles) {
                    if (t1.equals(t2)) {
                        continue;
                    } else {
                        Edge adjacentEdge = t1.GetAdjacentEdge(t2);
                        if (adjacentEdge != null) {
                            Edge newEdge = new Edge(t1.GetOppositePoint(adjacentEdge), t2.GetOppositePoint(adjacentEdge));

                            // check intersect
                            boolean isIntersect = false;
                            for (Edge otherEdge : edges) {
                                if (otherEdge.equals(newEdge) || otherEdge.equals(adjacentEdge)) {
                                    continue;
                                } else if (newEdge.intersects(otherEdge)) {
                                    isIntersect = true;
                                    break;
                                }
                            }

                            if (!isIntersect && adjacentEdge.intersects(newEdge) && edges.contains(adjacentEdge)) {
                                TrianglePair newPair = new TrianglePair(t1, t2, newEdge, adjacentEdge);
                                if (!trianglePairs.contains(newPair)) {
                                    if (!trianglePairs.offer(newPair)) {
                                        System.out.println("add failed");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {

        try {
            Random r;
            // number of points
            n = Integer.parseInt(args[0]);
            // number of threads
            t = Integer.parseInt(args[1]);
            if (args.length > 2) {
                r = new Random(Integer.parseInt(args[2]));
            } else {
                r = new Random();
            }
            points = new Point[n];

            // First, create a set of unique points
            // Our first 4 points are the outer corners.  This is not really necessary, but is
            // intended to give us a fixed convex hull so it's easier to see if the alg is working.
            points[0] = new Point(0.0, 0.0);
            points[1] = new Point(0.0, 1.0);
            points[2] = new Point(1.0, 1.0);
            points[3] = new Point(1.0, 0.0);


            for (int i = 4; i < n; i++) {
                boolean repeat;
                Point np = null;
                do {
                    repeat = false;
                    np = new Point(r.nextDouble(), r.nextDouble());
                    // Verify it is a distinct point.
                    for (int j = 0; j < i; j++) {
                        if (np.same(points[j])) {
                            repeat = true;
                            break;
                        }
                    }
                } while (repeat);
                points[i] = np;
            }


            System.out.println("Generated points");

            // Triangulate

            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    Edge e = new Edge(points[i], points[j]);
                    if (!intersection(e)) {
                        edges.add(e);
                        e.p.addEdge(e);
                        e.q.addEdge(e);
                    }
                }
            }
            System.out.println("Triangulated: " + n + " points, " + edges.size() + " edges");
            Thread[] threadSet = new Thread[t];
            numberOfFlip = new int[t];

            // start a timer
            long startTime = System.currentTimeMillis();

            GetAllTriangles();
            GetTrianglePairsList();

            // Now your code is required!
            for (int i = 0; i < t; i++) {
                threadSet[i] = new Thread(new Delaunay(i));
                threadSet[i].start();
            }

            for (int i = 0; i < t; i++) {
                threadSet[i].join();
            }


            System.out.println("Time cost:" + (System.currentTimeMillis() - startTime));

        } catch (Exception e) {
            System.out.println("ERROR " + e);
            e.printStackTrace();
        }
        for (int i = 0; i < numberOfFlip.length; i++) {
            totalFlip += numberOfFlip[i];
        }
        System.out.println("Total flip:" + totalFlip);
    }
}

class Delaunay implements Runnable {

    private int numberOfFlip = 0;
    public int threadID;

    public Delaunay(int threadID) {
        this.threadID = threadID;
    }

    @Override
    public void run() {
        while (!q2.isDone) {
            // Loop through all the pairs of triangles
            while (!q2.trianglePairs.isEmpty()) {
                TrianglePair trianglePair = null;
                synchronized (q2.trianglePairs) {
                    synchronized (q2.triangles) { // avoid another thread is updating the triangle list
                        if (q2.trianglePairs.peek() != null) {
                            if (!q2.trianglePairs.peek().t1.isOccupied && !q2.trianglePairs.peek().t2.isOccupied) {
                                trianglePair = q2.trianglePairs.poll();
                                trianglePair.t1.isOccupied = true;
                                trianglePair.t2.isOccupied = true;
                            }
                        }
                    }
                }
                // guarantee that none of two threads are handling with the same triangle

                if (trianglePair != null) {
                    if (!testPairDelaunay(trianglePair)) {
                        flipNonDelaunay(trianglePair);
                        q2.isDone = testPairsDelaunay(q2.trianglePairs);
                    }
                    trianglePair.t1.isOccupied = false;
                    trianglePair.t2.isOccupied = false;
                }

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            q2.isDone = getTrianglePairsAndTestPairsDelaunay();
        }
        q2.numberOfFlip[threadID] = numberOfFlip;
        System.out.println("Thread" + threadID + ": " + numberOfFlip);
    }

    // flip the non-Delaunay edge
    private void flipNonDelaunay(TrianglePair trianglepair) {
        resetEdge(trianglepair.adjacentEdge, trianglepair.newEdge);
        updateTriangleListAndGetTrianglePairs(trianglepair.adjacentEdge, trianglepair.newEdge);
    }

    /**
     * Test a single pair whether it is delaunay or not
     *
     * @param tp the given triangle pair
     * @return true if it is delaunay, otherwise false
     */
    private boolean testPairDelaunay(TrianglePair tp) {
        assert !tp.t1.equals(tp.t2);
        double angle1 = tp.t1.GetOppositeAngle(tp.adjacentEdge);
        double angle2 = tp.t2.GetOppositeAngle(tp.adjacentEdge);
        if (angle1 + angle2 > Math.PI) {
            return false;
        }
        return true;
    }


    private boolean testPairsDelaunay(Queue<TrianglePair> trianglePairs) {
        synchronized (q2.trianglePairs) {
            for (TrianglePair tp : trianglePairs) {
                if (!testPairDelaunay(tp)) {
                    return false;
                }
            }
            return true;
        }
    }

    private void resetEdge(Edge removedEdge, Edge addedEdge) {
        synchronized (q2.edges) {
            // reset the line
            q2.edges.remove(removedEdge);
            // System.out.println("remove" + removedEdge);
            q2.edges.add(addedEdge);
            // System.out.println("add" + addedEdge);
            numberOfFlip++;
        }
    }


    private void updateTriangleListAndGetTrianglePairs(Edge removedEdge, Edge addedEdge) {
        synchronized (q2.trianglePairs) {
            synchronized (q2.triangles) {
                q2.triangles.remove(new Triangle(removedEdge.p, removedEdge.q, addedEdge.p));
                q2.triangles.remove(new Triangle(removedEdge.p, removedEdge.q, addedEdge.q));
                q2.triangles.add(new Triangle(removedEdge.p, addedEdge.p, addedEdge.q));
                q2.triangles.add(new Triangle(removedEdge.q, addedEdge.p, addedEdge.q));
                q2.GetTrianglePairsList();
            }
        }
    }

    private boolean getTrianglePairsAndTestPairsDelaunay() {
        synchronized (q2.trianglePairs) {
            synchronized (q2.triangles) {
                q2.GetTrianglePairsList();
                return testPairsDelaunay(q2.trianglePairs);
            }
        }
    }

}
