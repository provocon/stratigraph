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
package de.provocon.stratigraph.source;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;


/**
 * Source to obtain source codes.
 * Take a set of source-directories into account.
 */
@Slf4j
public class JavaSourceProvider extends AbstractSourceProvider implements SourceProvider {

    /**
     * Initialize source repository.
     *
     * @param ignores set of package names to ignore on using this repository
     * @param aggregations set of packages names which should be aggregated and no subpackages should be considered
     * @param onlyInternalRelations only consider relations between packages in this source repository.
     */
    public JavaSourceProvider(Set<String> ignores, Set<String> aggregations, boolean onlyInternalRelations) {
        super(ignores, aggregations, onlyInternalRelations);
    }


    /**
     * Initialize source repository.
     *
     * @param ignores set of package names to ignore on using this repository
     * @param aggregations set of packages names which should be aggregated and no subpackages should be considered
     */
    public JavaSourceProvider(Set<String> ignores, Set<String> aggregations) {
        this(ignores, aggregations, false);
    }


    /**
     *
     * @param directoryName
     */
    public void scanHierarchy(String directoryName) {
        if (StringUtils.isEmpty(directoryName)) {
            return;
        }
        // LOG.debug("scanHierarchy({})", directoryName);
        File directory = new File(directoryName);
        LOG.debug("scanHierarchy({}) {}", directory.getAbsolutePath(), directory.isDirectory());
        if (directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                if (file.getName().equals("src")) {
                    LOG.debug("scanHierarchy({}) source base dir {}", directoryName, file);
                    File mvnSrcDir = new File(file.getAbsolutePath()+"/main/java");
                    if (mvnSrcDir.exists()) {
                        addBaseDirectory(mvnSrcDir.getAbsolutePath());
                    } else {
                        addBaseDirectory(file.getAbsolutePath());
                    }
                } else {
                    scanHierarchy(file.getAbsolutePath());
                }

            }
        }
    }


    /**
     *
     * @param directoryName
     */
    private void addBaseDirectory(String directoryName) {
        if (StringUtils.isEmpty(directoryName)) {
            return;
        }
        // LOG.debug("addBaseDirectory({})", directoryName);
        File directory = new File(directoryName);
        LOG.debug("addBaseDirectory({}) {}", directory.getAbsolutePath(), directory.isDirectory());
        if (directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                LOG.debug("addBaseDirectory() {}: {}", directory.getName(), file.getAbsolutePath());
                if (file.isDirectory()) {
                    addBaseDirectory(file.getAbsolutePath());
                }
                if (file.getName().endsWith(".java")) {
                    LOG.info("addBaseDirectory() reading {}", file.getName());
                    try (BufferedReader reader = Files.newBufferedReader(Paths.get(file.getAbsolutePath()))) {
                        String line = "";
                        String fqClassname = null;
                        while (line!=null) {
                            if (line.startsWith("package")) {
                                String className = file.getName().substring(0, file.getName().length()-5);
                                fqClassname = line.substring(8, line.length()-1)+"."+className;
                                LOG.debug("addBaseDirectory({}) {}", file.getName(), fqClassname);
                            }
                            if (line.startsWith("import")) {
                                if (fqClassname==null) {
                                    LOG.error("addBaseDirectory() Import for unidentified context {}", file.getAbsolutePath());
                                } else {
                                    Set<String> importSet = getImports().get(fqClassname);
                                    if (importSet==null) {
                                        importSet = new HashSet<>();
                                        getImports().put(fqClassname, importSet);
                                    }
                                    String imp = line.substring(7, line.length()-1);
                                    boolean add = true;
                                    for (String ignore : getIgnorePackages()) {
                                        if (imp.startsWith(ignore)) {
                                            add = false;
                                        }
                                    }
                                    if (add) {
                                        LOG.debug("addBaseDirectory() adding import {}", imp);
                                        importSet.add(imp);
                                    }
                                }
                            }
                            line = reader.readLine();
                        }
                        reader.close();
                    } catch (IOException ioe) {
                        LOG.error("addBaseDirectory()", ioe);
                    }
                }
            }
        }

        // LOG.debug("addBaseDirectory() {}", imports);
    }


    /**
     * Derive package name from class name.
     * Takes possible package aggregations into account and thus may shorten package name.
     *
     * @param className name of the class to obtain the package name for
     * @return name of potentially aggregated package
     */
    @Override
    public String getPackageName(String className) {
        int idx = className.lastIndexOf('.');
        // LOG.debug("getPackageName('{}')", className);
        String packageName = className.substring(0, idx);
        for (String pack : getAggregationPackages()) {
            if (packageName.startsWith(pack+".")) {
                LOG.debug("getPackageName() replacing {} with {}", packageName, pack);
                packageName = pack;
            }
        }
        return packageName;
    }

}
