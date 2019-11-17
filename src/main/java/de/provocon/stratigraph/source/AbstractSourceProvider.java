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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


/**
 * Common base elements for any source provider implementation.
 *
 * @author Martin Goellnitz
 */
@Slf4j
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


    /**
     * Derive package name from class name.
     * Takes possible package aggregations into account and thus may shorten package name.
     *
     * @param className name of the class to obtain the package name for
     * @return name of potentially aggregated package
     */
    public abstract String getPackageName(String className);


    /**
     * Obtain all package names available from this source repository.
     *
     * @return set of package names
     */
    public Set<String> getPackageNames() {
        Set<String> result = new HashSet<>();
        for (String className : getImports().keySet()) {
            String packageName = getPackageName(className);
            LOG.debug("getPackageNames() {} -> {}", className, packageName);
            if (!result.contains(packageName)) {
                result.add(packageName);
            }
        }
        return result;
    }


    /**
     * Obtain all class names referenced by the given package.
     *
     * @param packageName name of the package to obtain references for
     * @return set of class names
     */
    public List<String> getImportsForPackage(String packageName) {
        List<String> result = new ArrayList<>(128);
        Set<String> packageNames = Collections.emptySet();
        if (isOnlyInternalRelations()) {
            packageNames = getPackageNames();
        }
        for (String imp : getImports().keySet()) {
            LOG.debug("getImports({}) {}", packageName, imp);
            if (packageName.equals(getPackageName(imp))) {
                for (String i : getImports().get(imp)) {
                    if (!i.startsWith(packageName)&&(packageNames.contains(getPackageName(i))||(!isOnlyInternalRelations()))) {
                        result.add(i);
                    }
                }
            }
        }
        return result;
    }

}
