package com.firstbank.arch.util;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ProcessFileAuto {
	private static final Logger logger = LoggerFactory.getLogger(ProcessFileAuto.class);

@Value("${application.path.in}")
private String pathname;
//	private static final String archPathname = "C://numarch/archDocx/";
@Value("${application.path.archdoc}")
private String archPathname;
//	private static final String indexPathname = "C://numarch/indexes/";
@Value("${application.path.indexes}")
private String indexPathname;

	public static List<String> archFiles = new ArrayList<>();
	public static List<String> indexFiles = new ArrayList<>();


	//@Scheduled(cron = "0 0 0 * * 0")  //@Scheduled(cron = "0 */1 * ? * *")  @Scheduled(cron = "0 18 L * ?")
	public void emptyDirectory() {

		File directory = new File(archPathname);
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
						path = Paths.get(archPathname + str);
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

		File directory = new File(indexPathname);
		indexFiles.clear();

		if (directory.exists() && directory.isDirectory()) {

			try (Stream<Path> stream = Files.list(Paths.get(directory.getAbsolutePath()))) {

				indexFiles = stream
						.filter(Files::isDirectory)
						.map(Path::getFileName)
						.map(Path::toString)
						.collect(Collectors.toList());

				logger.info("indexFiles indexFiles: {}", indexFiles.size());

				if(!indexFiles.isEmpty()) {
					for (String str : indexFiles) {
						FileUtils.deleteDirectory(new File(indexPathname + str));
					}
				}

			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

}
