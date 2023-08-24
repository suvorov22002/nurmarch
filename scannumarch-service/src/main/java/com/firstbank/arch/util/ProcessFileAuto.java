package com.firstbank.arch.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ProcessFileAuto {

	private static final Logger logger = LoggerFactory.getLogger(ProcessFileAuto.class);
	private static final String PATHNAME = "C://numarch/in/";
	private static final String ARCH_PATHNAME = "C://numarch/archDocx/";
	private static final String INDEX_PATHNAME = "C://numarch/indexes/";
	public static Set<String> processingFiles = new HashSet<>();
	public static List<String> archFiles = new ArrayList<>();
	public static List<String> indexFiles = new ArrayList<>();
	public static Boolean onProcess = Boolean.FALSE;


	@Scheduled(cron = "0 0 0 * * 0")  //@Scheduled(cron = "0 */1 * ? * *")  @Scheduled(cron = "0 18 L * ?")
	public void emptyDirectory() {

		File directory = new File(ARCH_PATHNAME);
		archFiles.clear();

		if (directory.exists() && directory.isDirectory()) {

			try (Stream<Path> stream = Files.list(Paths.get(directory.getAbsolutePath()))) {

				archFiles = stream
						.filter(file -> !Files.isDirectory(file))
						.map(Path::getFileName)
						.map(Path::toString)
						.collect(Collectors.toList());

				logger.info("archFiles archFiles: {}", archFiles.size());

				if(!archFiles.isEmpty()) {

					Path path;
					for (String str : archFiles) {
						path = Paths.get(ARCH_PATHNAME + str);
						Files.deleteIfExists(path);
					}
				}

			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Scheduled(cron = "@daily")
	public void emptyDirectoryIndex() {

		File directory = new File(INDEX_PATHNAME);
		indexFiles.clear();

		if (directory.exists() && directory.isDirectory()) {

			try (Stream<Path> stream = Files.list(Paths.get(directory.getAbsolutePath()))) {

				indexFiles = stream
						.filter(file -> Files.isDirectory(file))
						.map(Path::getFileName)
						.map(Path::toString)
						.collect(Collectors.toList());

				logger.info("indexFiles indexFiles: "+indexFiles.size());

				if(!indexFiles.isEmpty()) {
					for (String str : indexFiles) {
						FileUtils.deleteDirectory(new File(INDEX_PATHNAME + str));
					}
				}

			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

}
