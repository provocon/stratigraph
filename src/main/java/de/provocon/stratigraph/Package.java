/*
 * Copyright 2015-2019 PROVOCON
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
package de.provocon.stratigraph;

import de.provocon.stratigraph.source.SourceProvider;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * Class describing a Java package in the code set to be analyzed.
 */
@Slf4j
@RequiredArgsConstructor
public class Package {

    @NonNull
    private SourceProvider sourceProvider;

    @Getter
    @NonNull
    private String name;


    /**
     *
     * @param ignorePackages
     * @return
     */
    public Map<String, Integer> getTargetPackages(Set<String> ignorePackages) {
        Map<String, Integer> result = new HashMap<>();
        for (String i : sourceProvider.getImportsForPackage(name)) {
            int idx = i.lastIndexOf('.');
            if (idx>0) {
                String p = sourceProvider.getPackageName(i);
                LOG.debug("getTargetPackages({}) {} {}", i, p);
                boolean include = true;
                for (String ip :ignorePackages) {
                    if (p.startsWith(ip+".")) {
                        include = false;
                    }
                }
                if (include) {
                    Integer count = result.get(p);
                    if (count==null) {
                        count = 0;
                    }
                    count++;
                    result.put(p, count);
                }
            }
        }
        return result;
    }


    /**
     *
     * @return
     */
    public Map<String, Integer> getTargetPackages() {
        Set<String> ignores = Collections.emptySet();
        return getTargetPackages(ignores);
    }

}
