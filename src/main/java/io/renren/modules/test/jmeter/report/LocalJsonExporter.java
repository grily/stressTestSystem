/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.renren.modules.test.jmeter.report;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.report.config.ExporterConfiguration;
import org.apache.jmeter.report.config.ReportGeneratorConfiguration;
import org.apache.jmeter.report.dashboard.AbstractDataExporter;
import org.apache.jmeter.report.dashboard.ExportException;
import org.apache.jmeter.report.dashboard.ReportGenerator;
import org.apache.jmeter.report.dashboard.SamplingStatistic;
import org.apache.jmeter.report.processor.ListResultData;
import org.apache.jmeter.report.processor.MapResultData;
import org.apache.jmeter.report.processor.SampleContext;
import org.apache.jmeter.report.processor.ValueResultData;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Implementation of {@link DataExporter} that exports statistics to JSON
 * Created by smooth00 on 2021/4/14 10:31.
 * 重写了JsonExporter类，为了兼容5.1~5.4版本的jmeter（jmeter4.0需要删除该类）
 *
 * @since 5.1
 */
public class LocalJsonExporter extends AbstractDataExporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalJsonExporter.class);
    public static final String OUTPUT_FILENAME = "statistics.json";
    private static final FileFilter JSON_FILE_FILTER =
            file -> file.isFile() && file.getName().equals(OUTPUT_FILENAME);
    private final static ObjectWriter OBJECT_WRITER = new ObjectMapper().writerWithDefaultPrettyPrinter();


    public LocalJsonExporter() {
        super();
    }

    @Override
    public void export(SampleContext context, File file, ReportGeneratorConfiguration reportGeneratorConfiguration)
            throws ExportException {
        Object data = context.getData().get(ReportGenerator.STATISTICS_SUMMARY_CONSUMER_NAME);
        if (data instanceof MapResultData) {
            LOGGER.info("Found data for consumer {} in context", ReportGenerator.STATISTICS_SUMMARY_CONSUMER_NAME);
            MapResultData result = (MapResultData) data;
            Map<String, SamplingStatistic> statistics = new HashMap<>();
            MapResultData overallData = (MapResultData) result.getResult("overall");
            LOGGER.info("Creating statistics for overall");
            createStatistic(statistics, overallData);

            ListResultData itemsData = (ListResultData) result.getResult("items");
            LOGGER.info("Creating statistics for other transactions");
            itemsData.forEach(r -> createStatistic(statistics, (MapResultData)r));

            LOGGER.info("Checking output folder");
            File outputDir = checkAndGetOutputFolder(reportGeneratorConfiguration);

            File outputFile = new File(outputDir, OUTPUT_FILENAME);
            LOGGER.info("Writing statistics JSON to {}", outputFile);
            try (Writer fileWriter = Files.newBufferedWriter(outputFile.toPath())) {
                OBJECT_WRITER.writeValue(fileWriter, statistics);
            } catch (IOException e) {
                throw new ExportException("Error generating JSON statistics file to " + outputFile +" for "+statistics, e);
            }
        }
    }

    /**
     * Check folder and return output folder.
     * @param reportGeneratorConfiguration {@link ReportGeneratorConfiguration}
     * @return {@link File} output folder
     * @throws ExportException
     */
    private File checkAndGetOutputFolder(ReportGeneratorConfiguration reportGeneratorConfiguration)
            throws ExportException {
        final ExporterConfiguration exportCfg = reportGeneratorConfiguration
                .getExportConfigurations().get(getName());
        // Get output directory property value
        File outputDir = getPropertyFromConfig(exportCfg, "output_dir",
                new File(JMeterUtils.getJMeterBinDir(), "report-output"), File.class);
        String globallyDefinedOutputDir = JMeterUtils.getProperty(JMeter.JMETER_REPORT_OUTPUT_DIR_PROPERTY);
        if(!StringUtils.isEmpty(globallyDefinedOutputDir)) {
            outputDir = new File(globallyDefinedOutputDir);
        }

        LocalJOrphanUtils.canSafelyWriteToFolder(outputDir, JSON_FILE_FILTER);
        try {
            FileUtils.forceMkdir(outputDir);
        } catch (IOException ex) {
            throw new ExportException("Error creating output folder "+outputDir.getAbsolutePath(), ex);
        }
        return outputDir;
    }

    private void createStatistic(Map<String, SamplingStatistic> statistics, MapResultData resultData) {
        LOGGER.debug("Creating statistics for result data:{}", resultData);
        SamplingStatistic statistic = new SamplingStatistic();
        ListResultData listResultData = (ListResultData) resultData.getResult("data");
        statistic.setTransaction((String) ((ValueResultData)listResultData.get(0)).getValue());
        statistic.setSampleCount((Long) ((ValueResultData)listResultData.get(1)).getValue());
        statistic.setErrorCount((Long) ((ValueResultData)listResultData.get(2)).getValue());
        statistic.setErrorPct(((Double) ((ValueResultData)listResultData.get(3)).getValue()).floatValue());
        statistic.setMeanResTime((Double) ((ValueResultData)listResultData.get(4)).getValue());
        statistic.setMinResTime((Long) ((ValueResultData)listResultData.get(5)).getValue());
        statistic.setMaxResTime((Long) ((ValueResultData)listResultData.get(6)).getValue());
        // 低于jmeter5.3版本的没有statistic.setMedianResTime()方法，所以用反射调用实现兼容性
        Class<? extends SamplingStatistic> clazz = statistic.getClass();
        try {
			Method setMedianResTime = clazz.getDeclaredMethod("setMedianResTime", Double.class);
			try {
				setMedianResTime.invoke(statistic, (Double) ((ValueResultData)listResultData.get(7)).getValue());
				statistic.setPct1ResTime((Double) ((ValueResultData)listResultData.get(8)).getValue());
		        statistic.setPct2ResTime((Double) ((ValueResultData)listResultData.get(9)).getValue());
		        statistic.setPct3ResTime((Double) ((ValueResultData)listResultData.get(10)).getValue());
		        statistic.setThroughput((Double) ((ValueResultData)listResultData.get(11)).getValue());
		        statistic.setReceivedKBytesPerSec((Double) ((ValueResultData)listResultData.get(12)).getValue());
		        statistic.setSentKBytesPerSec((Double) ((ValueResultData)listResultData.get(13)).getValue());
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			statistic.setPct1ResTime((Double) ((ValueResultData)listResultData.get(7)).getValue());
	        statistic.setPct2ResTime((Double) ((ValueResultData)listResultData.get(8)).getValue());
	        statistic.setPct3ResTime((Double) ((ValueResultData)listResultData.get(9)).getValue());
	        statistic.setThroughput((Double) ((ValueResultData)listResultData.get(10)).getValue());
	        statistic.setReceivedKBytesPerSec((Double) ((ValueResultData)listResultData.get(11)).getValue());
	        statistic.setSentKBytesPerSec((Double) ((ValueResultData)listResultData.get(12)).getValue());
		}
        
        statistics.put(statistic.getTransaction(), statistic);
    }
}
