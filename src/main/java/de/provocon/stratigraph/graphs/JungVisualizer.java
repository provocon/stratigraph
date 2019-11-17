/*
 * Copyright 2016-2019 Martin Goellnitz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.provocon.stratigraph.graphs;

import com.google.common.base.Function;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.renderers.VertexLabelRenderer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import lombok.extern.slf4j.Slf4j;


/**
 * Visualize source code measures through Jung library.
 */
@Slf4j
public class JungVisualizer implements Visualizer {

    private String title;

    private Graph<String, PackageEdge> g;

    private final boolean draw;


    /**
     * Initialize JungVisualizer
     *
     * @param draw should the graph be drawn on the screen or not
     */
    public JungVisualizer(boolean draw) {
        this.draw = draw;
    }


    /**
     * Initialize graph stream based visualizer.
     *
     * @param title title to be used for the graph
     */
    @Override

    public void init(String title) {
        this.g = new DirectedSparseGraph();
        this.title = title;
    }


    /**
     * Add node and all its outgoing edges with target nodes to graph.
     *
     * @param packageName name of the package node to add
     * @param targetPackages names for target nodes and edge weights
     */
    @Override
    public void addToGraph(String packageName, Map<String, Integer> targetPackages) {
        Set<String> vertices = new HashSet<>();
        Set<PackageEdge> edges = new HashSet<>();
        String fromVertex = packageName;
        if (!vertices.contains(fromVertex)) {
            vertices.add(fromVertex);
            g.addVertex(fromVertex);
        }
        for (Map.Entry<String, Integer> tp : targetPackages.entrySet()) {
            String toVertex = tp.getKey();
            if (!vertices.contains(toVertex)) {
                vertices.add(toVertex);
                g.addVertex(toVertex);
            }
            PackageEdge edge = new PackageEdge(fromVertex, toVertex, tp.getValue());
            if (edges.contains(edge)) {
                LOG.error("addToGraph() unexpected edge found: {}", edge);
            } else {
                edges.add(edge);
                g.addEdge(edge, fromVertex, toVertex, edu.uci.ics.jung.graph.util.EdgeType.DIRECTED);
            }
        }
    }


    /**
     * Display graph.
     */
    @Override
    public void display() {
        if (draw) {
            JFrame jf = new JFrame(title);
            // Forest f = new DelegateForest(g);
            KKLayout layout = new KKLayout(g);
            layout.setAdjustForGravity(true);
            layout.setDisconnectedDistanceMultiplier(0);
            layout.setExchangeVertices(true);
            Dimension preferredSize = new Dimension(1600, 850);
            layout.setSize(preferredSize);
            VisualizationViewer vv = new VisualizationViewer(layout, preferredSize);
            final Function edgePaintFunction = new Function() {
                @Override
                public Object apply(Object f) {
                    return Color.BLUE;
                }

            };
            vv.getRenderContext().setEdgeDrawPaintTransformer(edgePaintFunction);
            final Function edgeStrokeFunction = new Function() {
                @Override
                public Object apply(Object f) {
                    PackageEdge s = (PackageEdge)f;
                    int weight = s.getWeight();
                    if (weight>9) {
                        weight = 9;
                    }
                    return new BasicStroke(weight, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
                }

            };
            vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeFunction);
            final VertexLabelRenderer vertexLabel = new VertexLabelRenderer() {
                @Override
                public <T> Component getVertexLabelRendererComponent(JComponent jc, Object o, Font font, boolean bln, T t) { // NOPMD
                    return new JLabel(t.toString());
                }

            };
            vv.getRenderContext().setVertexLabelRenderer(vertexLabel);
            vv.setLocation(800, 500);
            jf.getContentPane().add(vv);
            jf.pack();
            jf.setVisible(true);
            jf.setSize(1700, 950);
            jf.setLocation(50, 50);
        }

        // LOG.info("display() vertices: {} / {}", g.getVertices().size(), g.getVertexCount());
        List<String> outZero = new ArrayList<>();
        for (String n : g.getVertices()) {
            // LOG.info("display() {}: {}", n, g.getOutEdges(n).size());
            if (g.getOutEdges(n).isEmpty()) {
                outZero.add(n);
            }
        }
        Collections.sort(outZero);
        LOG.info("\ndisplay() no outgoing edge:");
        for (String n : outZero) {
            LOG.info("display() {}", n);
        }

        // init:
        Collection<String> aggregatedPreviousLayers = outZero;

        int level = 1;
        List<String> remainingNodes = new ArrayList<>();
        for (String n : g.getVertices()) {
            if (!aggregatedPreviousLayers.contains(n)) {
                remainingNodes.add(n);
            }
        }
        boolean progress = !remainingNodes.isEmpty();
        while (progress) {
            progress = false;
            Collections.sort(remainingNodes);
            // LOG.info("display() remaining  nodes {}", remainingNodes);
            LOG.info("\ndisplay() level {}", level++);
            // LOG.info("display() allready handled {}", aggregatedPreviousLayers);
            Set<String> newLayer = new HashSet<>();
            for (String n : remainingNodes) {
                boolean isInLayer = true;
                for (PackageEdge e : g.getOutEdges(n)) {
                    if (!aggregatedPreviousLayers.contains(e.getTo())) {
                        isInLayer = false;
                    }
                }
                if (isInLayer) {
                    LOG.info("display() {}", n);
                    newLayer.add(n);
                    progress = true;
                }
            }
            aggregatedPreviousLayers.addAll(newLayer);
            remainingNodes = new ArrayList<>();
            for (String n : g.getVertices()) {
                if (!aggregatedPreviousLayers.contains(n)) {
                    remainingNodes.add(n);
                }
            }
            if (remainingNodes.isEmpty()) {
                progress = false;
            }
        }
        if (remainingNodes.isEmpty()) {
            LOG.info("\ndisplay() successfully stacked software");
        } else {
            LOG.info("\ndisplay() list of offending nodes and reason packages:");
            Collections.sort(remainingNodes);
        }
        for (String n : remainingNodes) {
            List<String> conflictingNodes = new ArrayList<>();
            for (PackageEdge e : g.getOutEdges(n)) {
                if (!aggregatedPreviousLayers.contains(e.getTo())) {
                    conflictingNodes.add(e.getTo());
                }
            }
            Collections.sort(conflictingNodes);
            for (String toNode : conflictingNodes) {
                LOG.info("display() {} - {}", n, toNode);
            }
        }

        int base = aggregatedPreviousLayers.size()+remainingNodes.size();
        int layered = aggregatedPreviousLayers.size();
        LOG.info("\ndisplay() {} of {} layered ({}%)", layered, base, layered*100/(base));

        if (draw) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ie) {

            }
            System.exit(0); // NOPMD
        }
    }

}
