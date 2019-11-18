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

import java.util.Map;


/**
 * Interface to let the obtained source code measures be visualized through different graph libraries.
 */
public interface Visualizer {

    /**
     * Initialize graph stream based visualizer.
     *
     * @param title title to be used for the graph
     */
    void init(String title);


    /**
     * Add node and all its outgoing edges with target nodes to graph.
     *
     * @param packageName name of the package node to add
     * @param targetPackages names for target nodes and edge weights
     */
    void addToGraph(String packageName, Map<String, Integer> targetPackages);


    /**
     * Display graph.
     *
     * @return Tell if sollected source set is completely layered.
     */
    boolean display();

}
