/*
 * Copyright 2016 Martin Goellnitz
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;


/**
 * Visualize source code measures through Jung library.
 */
@Slf4j
public class JGraphTVisualizer implements Visualizer {

    private DirectedGraph<String, PackageEdge> g;


    /**
     * Initialize graph stream based visualizer.
     *
     * @param title title to be used for the graph
     */
    @Override

    public void init(String title) {
        this.g = new DefaultDirectedWeightedGraph(PackageEdge.class);
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
                g.addEdge(fromVertex, toVertex, edge);
            }
        }
    }


    private Collection<PackageEdge> getOutEdges(String node) {
        Collection<PackageEdge> result = new HashSet<>();
        for (PackageEdge e : g.edgesOf(node)) {
            if (g.getEdgeSource(e).equals(node)) {
                result.add(e);
            }
        }
        return result;
    }


    private Collection<String> getTargetNodes(String node) {
        Collection<String> result = new HashSet<>();
        for (PackageEdge e : getOutEdges(node)) {
            result.add(e.getTo());
        }
        return result;
    }


    /**
     * Display graph.
     *
     * @return Tell if sollected source set is completely layered.
     */
    @Override
    public boolean display() {
        // LOG.info("display() vertices: {} / {}", g.getVertices().size(), g.getVertexCount());
        List<String> outZero = new ArrayList<>();
        for (String n : g.vertexSet()) {
            LOG.info("display() {}: {}", n, g.edgesOf(n));
            if (getOutEdges(n).isEmpty()) {
                outZero.add(n);
            }
        }
        Collections.sort(outZero);
        LOG.info("\ndisplay() no outgoing edge:");
        for (Object n : outZero) {
            LOG.info("display() {}", n);
        }

        // init:
        Collection<String> aggregatedPreviousLayers = outZero;

        int level = 1;
        List<String> remainingNodes = new ArrayList<>();
        for (String n : g.vertexSet()) {
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
                for (PackageEdge e : getOutEdges(n)) {
                    if (!aggregatedPreviousLayers.contains(e.getTo())) {
                        isInLayer = false;
                    }
                }
                if (isInLayer) {
                    LOG.info("display() {}: {}", n, getTargetNodes(n));
                    newLayer.add(n);
                    progress = true;
                }
            }
            aggregatedPreviousLayers.addAll(newLayer);
            remainingNodes = new ArrayList<>();
            for (String n : g.vertexSet()) {
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
        CycleDetector<String, PackageEdge> detector = new CycleDetector<>(g);
        for (String n : remainingNodes) {
            List<String> conflictingNodes = new ArrayList<>();
            for (PackageEdge e : getOutEdges(n)) {
                if (!aggregatedPreviousLayers.contains(e.getTo())) {
                    conflictingNodes.add(e.getTo());
                }
            }
            LOG.info("display() {}: {}", n, detector.findCyclesContainingVertex(n));
            Collections.sort(conflictingNodes);
            for (String toNode : conflictingNodes) {
                LOG.info("display() {} - {}", n, toNode);
            }
        }

        int base = aggregatedPreviousLayers.size()+remainingNodes.size();
        int layered = aggregatedPreviousLayers.size();
        int percentage = layered*100/(Math.max(base, 1));
        LOG.info("\ndisplay() {} of {} layered ({}%)", layered, base, percentage);
        return percentage >= 100;
    }

}
