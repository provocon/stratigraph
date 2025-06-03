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
package de.provocon.stratigraph.command;

import de.provocon.stratigraph.Package;
import de.provocon.stratigraph.graphs.GraphstreamVisualizer;
import de.provocon.stratigraph.graphs.JGraphTVisualizer;
import de.provocon.stratigraph.graphs.JungVisualizer;
import de.provocon.stratigraph.graphs.Visualizer;
import de.provocon.stratigraph.source.JavaSourceProvider;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


/**
 * Main class to run the analyse process.
 */
@Slf4j
public final class Analyse {

    private static final String FILENAME_AGGREGATION_LIST = "/.stratigraph.aggregation.list";

    private static final String FILENAME_IGNORE_LIST = "/.stratigraph.ignore.list";


    private Analyse() {
    }


    /**
     * Main method as starting point for the tool to run.
     *
     * TODO: Map to modules / JARs
     * TODO: configure/find packages to analyse
     * TODO: aggregation packages
     * TODO: configure ignore packages for inclusion and for references separately
     *
     * @param args the usual command line arguments
     * @throws ParseException command line arguments might be problematic
     * @throws IOException file handling might fail
     */
    public static void main(String[] args) throws ParseException, IOException {
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

        LOG.info("Starting at directory {}", new File(".").getCanonicalPath());

        Options options = new Options();
        options.addOption("d", "basedir", true, "base directory to scan for java source files.");
        options.addOption("i", "internal", false, "Only take references internal to the project into account.");
        options.addOption("e", "noerror", false, "Don't issue error to calling operating system when not 100% layered.");
        options.addOption("h", "help", false, "Issue this help message and exit.");
        options.addOption("g", "graphstream", false, "Use Graphstream library instead of Jung.");
        options.addOption("j", "jgrapht", false, "Use JGraphT library instead of Jung.");
        options.addOption("w", "draw", false, "When using Jung library really draw the graph in a window.");
        options.addOption("t", "delay", true, "Delay in ms when drawing with the Graphstream library.");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        if (cmd.hasOption('h')) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("stg", options);
        } else {
            boolean useGraphstream = cmd.hasOption('g');
            boolean useJGraphT = cmd.hasOption('j');
            boolean onlyInternalReferences = cmd.hasOption('i');
            boolean draw = cmd.hasOption('w');
            boolean noerror = cmd.hasOption('e');
            int delay = Integer.parseInt(cmd.getOptionValue('t', "50"));
            String baseDir = cmd.getOptionValue("d", ".");
            File ignoresFile = new File(baseDir+FILENAME_IGNORE_LIST);
            Set<String> ignores = new HashSet<>();
            if (ignoresFile.exists()) {
                try (BufferedReader br = Files.newBufferedReader(Paths.get(ignoresFile.getAbsolutePath()))) {
                    ignores = br.lines().collect(Collectors.toSet());
                } catch (IOException e) {
                    LOG.error("main()", e);
                }
            } else {
                ignores.add("java.");
                ignores.add("org.slf4j.");
                ignores.add("lombok.");
            }

            File aggregationsFile = new File(baseDir+FILENAME_AGGREGATION_LIST);
            Set<String> aggregations = new HashSet<>();
            if (aggregationsFile.exists()) {
                try (BufferedReader br = Files.newBufferedReader(Paths.get(aggregationsFile.getAbsolutePath()))) {
                    aggregations = br.lines().collect(Collectors.toSet());
                } catch (IOException e) {
                    LOG.error("main()", e);
                }
            }

            LOG.info("ignores {}", ignores);
            LOG.info("aggregations {}", aggregations);

            JavaSourceProvider srcsrc = new JavaSourceProvider(ignores, aggregations, onlyInternalReferences);
            srcsrc.scanHierarchy(baseDir);

            Visualizer v = useJGraphT ? new JGraphTVisualizer() : (useGraphstream ? new GraphstreamVisualizer(delay) : new JungVisualizer(draw));
            v.init(baseDir);
            Set<String> packageNames = srcsrc.getPackageNames();
            // LOG.info("package names ({}) {}", packageNames.size(), packageNames);
            for (String packageName : packageNames) {
                Package p = new Package(srcsrc, packageName);
                final Map<String, Integer> targetPackages = p.getTargetPackages();
                // LOG.info("package imports {}: {}", p.getName(), targetPackages);
                v.addToGraph(packageName, targetPackages);
            }
            boolean success = v.display();
            if (!(noerror || success)) {
                System.exit(1);
            }
        }
    }

}
