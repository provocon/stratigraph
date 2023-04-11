/*
 * Copyright 2016-2023 PROVOCON
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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;


/**
 * Visualize source code measures through graphstream library.
 *
 * @author Martin Goellnitz
 */
@Slf4j
public class GraphstreamVisualizer implements Visualizer {

    private static final String UI_LABEL = "ui.label";

    private Graph g;

    private final int delay;


    /**
     * Initialize new GraphstreamVisualizer with a given drawing delay.
     *
     * @param delay drawing delay in ms to be used
     */
    public GraphstreamVisualizer(int delay) {
        this.delay = delay;
    }


    /**
     * Initialize graph stream based visualizer.
     *
     * @param title title to be used for the graph
     */
    @Override
    public void init(String title) {
        g = new SingleGraph(title);
        URL cssResource = GraphstreamVisualizer.class.getResource("/graph.css");
        LOG.debug("css: {}", cssResource);
        g.setAttribute("ui.stylesheet", "url('"+cssResource.toString()+"')");
        System.setProperty("org.graphstream.ui", "swing");
        Viewer viewer = g.display();
        View view = viewer.getDefaultView();
        LOG.info("Dummy output for PMG: {}", view.getIdView());
        // view.setLocation(50, 50);
        // view.resizeFrame(1800, 900);
    }


    /**
     * Add node and all its outgoing edges with target nodes to graph.
     *
     * @param packageName name of the package node to add
     * @param targetPackages names for target nodes and edge weights
     */
    @Override
    public void addToGraph(String packageName, Map<String, Integer> targetPackages) {
        int nodeDelay = delay*5;
        int edgeDelay = delay*2;
        Node fn = g.getNode(packageName);
        if (fn==null) {
            fn = g.addNode(packageName);
            fn.setAttribute(UI_LABEL, packageName);
            try {
                Thread.sleep(nodeDelay);
            } catch (InterruptedException ie) {

            }
        }
        for (Map.Entry<String, Integer> tp : targetPackages.entrySet()) {
            Node tn = g.getNode(tp.getKey());
            if (tn==null) {
                tn = g.addNode(tp.getKey());
                tn.setAttribute(UI_LABEL, tp.getKey());
                try {
                    Thread.sleep(nodeDelay);
                } catch (InterruptedException ie) {

                }
            }
            Edge e = g.addEdge(packageName+tp.getKey(), fn, tn, true);
            try {
                Thread.sleep(edgeDelay);
            } catch (InterruptedException ie) {

            }
            Integer weight = tp.getValue();
            e.setAttribute("weight", weight);
            e.setAttribute(UI_LABEL, weight);
            if (weight>9) {
                weight = 10;
            }
            e.setAttribute("ui.class", "w"+weight);
        }
    }


    /**
     * Display and evaluate graph.
     *
     * @return Tell if sollected source set is completely layered.
     */
    @Override
    public boolean display() {
        Comparator<Node> nodeComparator = new Comparator<Node>() {

            /**
             * Compare two nodes according to their ui.label attribute.
             *
             * @param o1 node one
             * @param o2 node two
             * @return result of the comparison of the ui.label strings of the two nodes
             */
            @Override
            public int compare(Node o1, Node o2) { // NOPMD
                return o1.getAttribute(UI_LABEL).toString().compareTo(o2.getAttribute(UI_LABEL).toString());
            }

        };

        List<Node> outZero = new ArrayList<>();
        for (int i=0; i < g.getNodeCount(); i++) {
            Node n = g.getNode(i);
            if (n.getOutDegree()==0) {
                outZero.add(n);
            }
        }
        Collections.sort(outZero, nodeComparator);
        LOG.info("\ndisplay() no outgoing edge:");
        for (Node n : outZero) {
            LOG.info("display() {}", (String) n.getAttribute(UI_LABEL));
        }

        // init:
        Collection<Node> aggregatedPreviousLayers = outZero;
        int level = 1;
        List<Node> remainingNodes = new ArrayList<>();
        for (int i=0; i < g.getNodeCount(); i++) {
            Node n = g.getNode(i);
            if (!aggregatedPreviousLayers.contains(n)) {
                remainingNodes.add(n);
            }
        }
        boolean progress = !remainingNodes.isEmpty();
        while (progress) {
            progress = false;
            Collections.sort(remainingNodes, nodeComparator);
            // LOG.info("display() remaining  nodes {}", remainingNodes);
            LOG.info("\ndisplay() level {}", level++);
            // LOG.info("display() allready handled {}", aggregatedPreviousLayers);
            Set<Node> newLayer = new HashSet<>();
            for (Node n : remainingNodes) {
                boolean isInLayer = true;
                for (int i=0; i<n.getOutDegree(); i++) {
                    Edge e = n.getLeavingEdge(i);
                    if (!aggregatedPreviousLayers.contains(e.getTargetNode())) {
                        isInLayer = false;
                    }
                }
                if (isInLayer) {
                    LOG.info("display() {}", (String) n.getAttribute(UI_LABEL));
                    newLayer.add(n);
                    progress = true;
                }
            }
            aggregatedPreviousLayers.addAll(newLayer);
            remainingNodes = new ArrayList<>();
            for (int i=0; i < g.getNodeCount(); i++) {
                Node n = g.getNode(i);
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
            Collections.sort(remainingNodes, nodeComparator);
        }
        for (Node n : remainingNodes) {
            List<String> conflictingNodes = new ArrayList<>();
            for (int i=0; i<n.getOutDegree(); i++) {
                Edge e = n.getLeavingEdge(i);
                if (!aggregatedPreviousLayers.contains(e.getTargetNode())) {
                    conflictingNodes.add(e.getTargetNode().getAttribute(UI_LABEL, String.class));
                }
            }
            Collections.sort(conflictingNodes);
            String fromNode = n.getAttribute(UI_LABEL, String.class);
            for (String toNode : conflictingNodes) {
                LOG.info("display() {} - {}", fromNode, toNode);
            }
        }

        int base = aggregatedPreviousLayers.size()+remainingNodes.size();
        int layered = aggregatedPreviousLayers.size();
        int percentage = layered*100/(Math.max(base, 1));
        LOG.info("\ndisplay() {} of {} layered ({}%)", layered, base, percentage);
        return percentage >= 100;
    }

}
