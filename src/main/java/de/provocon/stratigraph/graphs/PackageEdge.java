/*
 * Copyright 2016 PROVOCON
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

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;


/**
 * Edge class for some visualizers.
 * The Jung library and the JGraphT library don't come with a sufficient generic implementations.
 */
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class PackageEdge {

    @Getter
    private String from;

    @Getter
    private String to;

    @Getter
    private int weight;

}
