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

import java.util.List;
import java.util.Set;


/**
 * Instances provide the sources which can be derived from the given base
 * directory in any supported programming language.
 *
 * @author Martin Goellnitz
 */
public interface SourceProvider {

    /**
     * Obtain all package names available from this source repository.
     *
     * @return set of package names
     */
    Set<String> getPackageNames();


    /**
     * Obtain all class names referenced by the given package.
     *
     * @param packageName name of the package to obtain references for
     * @return set of class names
     */
    List<String> getImportsForPackage(String packageName);


    /**
     * Derive package name from class name.
     * Takes possible package aggregations into account and thus may shorten package name.
     *
     * @param className name of the class to obtain the package name for
     * @return name of potentially aggregated package
     */
    String getPackageName(String className);

}
