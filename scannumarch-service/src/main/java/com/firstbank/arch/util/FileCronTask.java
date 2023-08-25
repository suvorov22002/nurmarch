package com.firstbank.arch.util;

import com.firstbank.arch.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class FileCronTask {

	//private static final Logger logger = LoggerFactory.getLogger(FileCronTask.class);
	//private static final String PATHNAME = "C://numarch/alfresco/";

	@Value("${application.path.alfresco}")
	private String pathname;

	@Value("${application.cron.time}")
	private String timer;

//	@Value("${application.url.folder}")
//	private String urlFolder;
//	
//	@Value("${application.url.save}")
//	private String urlSave;

	@Autowired
	private FileService fileservice;

	@Scheduled(cron = "0 */${application.cron.time} * ? * *")
	public void scheduleTaskFixedDelay(){

		  org.json.simple.JSONArray fileResponseLists;
		  org.json.simple.JSONObject fileResponseObject;

		log.info("scheduleTaskFixedDelay executed at {}", LocalDateTime.now());

		File directory = new File(pathname);
		Set<String> listDoc;

//		System.out.println("Parameter.timer: "+timer);

//		System.out.println("Parameter.urlSave: "+urlSave);
//		
//		System.out.println("Parameter.urlFolder: "+urlFolder);

		if (directory.exists() && directory.isDirectory()) {

			fileResponseLists = new org.json.simple.JSONArray();

			try (Stream<Path> stream = Files.list(Paths.get(directory.getAbsolutePath()))) {

				listDoc =  stream
						.filter(Files::isDirectory)
						.map(Path::getFileName)
						.map(Path::toString)
						.collect(Collectors.toSet());

				Iterator<String> it = listDoc.iterator();
				String toSend;
				String result;

				while(it.hasNext()) {

					toSend = it.next();

					try {

						fileResponseObject = new org.json.simple.JSONObject();

						result = fileservice.pushToAlfresco(toSend);
						//	System.out.println(res);

						if (result != null) {

							fileResponseObject.put("filename", toSend);
							fileResponseObject.put("lien", result);
							fileResponseLists.add(fileResponseObject);
							FileUtils.deleteDirectory(Paths.get(directory.getAbsolutePath(), toSend).toFile());

							it.remove();

						}

					} catch (IOException e) {
						e.printStackTrace();
						log.info("ERROR LORS DE LA SUPPRESSION DU DOSSIER");
					}

				}

				if (!fileResponseLists.isEmpty()) {

					Path filePath = Paths.get(directory.getAbsolutePath(), RefactorDateUtil.now() + "_AlfrecoDoc.json");
					try (FileWriter file = new FileWriter(filePath.toString(), true);
						 BufferedWriter bw = new BufferedWriter(file);) {

						//file.write(fileResponseLists.toJSONString());
						//BufferedWriter bw = new BufferedWriter(file);
						bw.write(fileResponseLists.toJSONString());
						bw.newLine();
						//bw.close();
						//file.flush();
					}
				}


			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}
}
