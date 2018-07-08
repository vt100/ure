package ure.math;

import ure.areas.UArea;
import ure.actors.UActor;
import ure.things.UThing;

import java.util.Comparator;
import java.util.Random;
import java.util.TreeSet;

public class UPath {

    Random random;

    public UPath() {
        random = new Random();
    }

    class Nodelist extends TreeSet<Node> {
        public Nodelist() {
            super(new Node(0,0));
        }
    }
    class Node implements Comparator<Node> {
        int x, y;
        Node parent;
        double g, h, f;

        public Node(int thex, int they) {
            x = thex;
            y = they;
        }

        public Node(int thex, int they, Node theparent) {
            x = thex;
            y = they;
            parent = theparent;
        }
        public void recalc(int goalx, int goaly) {
            if (parent != null)
                g = parent.g + 1;
            h = Math.abs(x - goalx) + Math.abs(y - goaly);
            f = g + h;
        }
        public double getval() {
            return f;
        }
        public int compare(Node n1, Node n2) {
            if (n1.getval() > n2.getval())
                return 1;
            return -1;
        }
    }

    public Node NodeIfOpen(UArea area, int x, int y, Node parent, UActor actor) {
        if (x < 0 || y < 0 || x >= area.xsize || y >= area.ysize)
            return null;
        if (area.willAcceptThing((UThing)actor, x, y))
            return new Node(x,y,parent);
        return null;
    }

    public int mdist(int x1, int y1, int x2, int y2) {
        return Math.abs(x2-x1) + Math.abs(y2-y1);
    }

    public int[] nextStep(UArea area, int x1, int y1, int x2, int y2, UActor actor, int range) {
        if (mdist(x1,y1,x2,y2) > range)
            return null;
        Nodelist openlist = new Nodelist();
        Nodelist closedlist = new Nodelist();
        Node start = new Node(x1,y1);
        openlist.add(start);
        int stepcount = 0;
        while (!openlist.isEmpty() && stepcount < 2000) {
            stepcount++;
            Node q = openlist.pollFirst();
            openlist.remove(q);
            Node[] steps = new Node[5];
            steps[0] = NodeIfOpen(area, q.x-1, q.y, q, actor);
            steps[1] = NodeIfOpen(area, q.x+1, q.y, q, actor);
            steps[2] = NodeIfOpen(area, q.x, q.y-1, q, actor);
            steps[3] = NodeIfOpen(area, q.x, q.y+1, q, actor);
            if (random.nextFloat() < 0.5f) {
                steps[4] = steps[1];
                steps[1] = steps[3];
                steps[3] = steps[4];
            }
            if (random.nextFloat() < 0.5f) {
                steps[4] = steps[0];
                steps[0] = steps[2];
                steps[2] = steps[4];
            }
            for (int i=0;i<4;i++) {
                Node step = steps[i];
                if (step != null) {
                    if (step.x == x2 && step.y == y2) { // FOUND IT
                        while (step.parent != start) {
                            System.out.println("step " + Integer.toString(step.x - x1) + "," + Integer.toString(step.y - y1));
                            step = step.parent;
                        }
                        System.out.println("found path in " + Integer.toString(stepcount) + " steps");
                        System.out.println("walk " + Integer.toString(step.x - x1) + "," + Integer.toString(step.y - y1));

                        return new int[]{step.x, step.y};
                    }
                    step.recalc(x2,y2);
                    boolean skipstep = false;
                    for (Node o : openlist) {
                        if ((o.x == step.x) && (o.y == step.y) && (o.f < step.f)) {
                            skipstep = true;
                        }
                    }
                    for (Node c : closedlist) {
                        if ((c.x == step.x) && (c.y == step.y) && (c.f < step.f)) {
                            skipstep = true;
                        }
                    }
                    if (mdist(x1,y1,step.x,step.y) > range)
                        skipstep = true;
                    if (!skipstep) {
                        openlist.add(step);
                    }
                }
            }
            closedlist.add(q);
        }
        System.out.println("failed path in max stepcount");
        return null;
    }
}