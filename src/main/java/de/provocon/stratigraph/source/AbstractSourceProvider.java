/*
 * Copyright (C) 2019 PROVOCON
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.provocon.stratigraph.source;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.Getter;


/**
 * Common base elements for any source provider implementation.
 *
 * @author Martin Goellnitz
 */
public abstract class AbstractSourceProvider {

    @Getter
    private final Map<String, Set<String>> imports = new HashMap<>();

    @Getter
    private final Set<String> ignorePackages;

    @Getter
    private final Set<String> aggregationPackages;

    @Getter
    private final boolean onlyInternalRelations;


    /**
     * Initialize source repository.
     *
     * @param ignores set of package names to ignore on using this repository
     * @param aggregations set of packages names which should be aggregated and no subpackages should be considered
     * @param onlyInternalRelations only consider relations between packages in this source repository.
     */
    protected AbstractSourceProvider(Set<String> ignores, Set<String> aggregations, boolean onlyInternalRelations) {
        this.ignorePackages = ignores;
        this.aggregationPackages = aggregations;
        this.onlyInternalRelations = onlyInternalRelations;
    }

}
