package com.firstbank.arch.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class FileService {

	@Value("${application.path.alfresco}")
	private String pathname;

	@Value("${application.url.folder}")
	private String urlFolder;

	@Value("${application.url.save}")
	private String urlSave;

	@Value("${application.path.months}")
	private String[] mois;

//	private final static String[] mois = {"JANVIER", "FEVRIER","MARS","AVRIL","MAI","JUIN","JUILLET",
//            "AOUT","SEPTEMBRE","OCTOBRE","NOVEMBRE","DECEMBRE"};

	private Map<String, String> mapDocuments = new HashMap<>();

	public String pushToAlfresco(String name) {

		File directory = new File(pathname + name);
		Set<String> listSet;

		if (directory.exists() && directory.isDirectory()) {

			try (Stream<Path> stream = Files.list(Paths.get(directory.getAbsolutePath()))) {

				listSet =  stream
						.filter(file -> !Files.isDirectory(file))
						.map(Path::getFileName)
						.map(Path::toString)
						.collect(Collectors.toSet());

				File fichierPdf = null;
				File fichierJSON = null;
				Path filePath;

				for (String file : listSet) {

					filePath = Paths.get(directory.getAbsolutePath(), file);

					if (FilenameUtils.getExtension(file).equals("pdf")) {
						fichierPdf = new File(filePath.toString());
					} else if (FilenameUtils.getExtension(file).equals("json")) {

						fichierJSON = new File(filePath.toString());

						extractMapDocument(filePath);
					}

				}

				if (fichierJSON != null && fichierPdf != null && fichierPdf.exists()) {

					return sendToAcs(mapDocuments, fichierPdf);

				}

			} catch (Exception e1) {
				e1.printStackTrace();
			}

		}

		return null;
	}

	private void extractMapDocument(Path filePath) {

		try (FileReader reader = new FileReader(filePath.toString())) {

			JSONParser parser = new JSONParser();
			Object obj = parser.parse(reader);
			org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) obj;

			mapDocuments.put("afbm:trxAmount", jsonObject.get("setMon").toString());
			mapDocuments.put("afbm:trxUser", jsonObject.get("setUti").toString());
			mapDocuments.put("afbm:docType", jsonObject.get("setType").toString());
			mapDocuments.put("afbm:docRef", jsonObject.get("setEve").toString());
			mapDocuments.put("afbm:unitCode", jsonObject.get("setAge").toString());
			mapDocuments.put("afbm:trxAcc", jsonObject.get("setNcp").toString() +
					"-" + jsonObject.get("setCle").toString());
			mapDocuments.put("afbm:trxDate", jsonObject.get("setDco").toString());

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private String sendToAcs(Map<String, String> mapDocument, File file) {

		JSONObject metadata = new JSONObject();
		JSONObject data = new JSONObject();
		JSONObject varia;
		String reper = "" ;
		String destfolder = "";
		String lienDoc = null;
		RestTemplate restTemplate;
		String defaultAspect = "cm:titled;afbm:internalDoc;afbm:transaction;afbm:unit;afbm:nameComponent";
		DateFormat format = new SimpleDateFormat("dd/MM/yyyy");

		mapDocument.forEach((k,v) -> {

			try {

				if ("afbm:trxDate".equals(k)) {

//						String convDate = v.substring(0,2) + '/' + v.substring(2,4) + '/' + v.substring(4,8);
					String convDate = String.join("/", v.substring(0,2), v.substring(2,4), v.substring(4,8));
					metadata.put(k, format.parse(convDate));

				} else if ("afbm:trxAmount".equals(k)) {

					metadata.put(k, Double.parseDouble(v));

				} else {
					metadata.put(k, v);
				}

				//	System.out.println("Key: "+k +" , Value: "+v);
			} catch (java.text.ParseException | JSONException e) {
				e.printStackTrace();
			}

		});


		try {

			varia = new JSONObject();
			varia.put("folderType", "afbm:transferFolder");
			varia.put("rootPath", "/Sites/afriland/documentLibrary");
			reper = (String) metadata.get("afbm:trxUser");
			varia.put("folderName", reper);
			varia.put("aspects", "afbm:transfer");
			String year = mapDocuments.get("afbm:trxDate").substring(4);
			String month = mapDocuments.get("afbm:trxDate").substring(2,4);

			LocalDate currentDate = LocalDate.parse(String.join("-", year, month, "01"));
			Month monthValue = currentDate.getMonth();

			varia.put("destPath", "/AFB_NUMARCH/journeeComptables/" + year + "/" + mois[Integer.parseInt(month)-1]);

			destfolder = destfolder + "/AFB_NUMARCH/journeeComptables/" + year+"/" + mois[Integer.parseInt(month)-1];

			// Create sites in alfresco is does not exist

			//System.out.println("parent: "+varia.toString());


//			JSONObject folderID = restTemplate.postForObject(urlFolder, reqEntity, JSONObject.class);
			String nomFichier;
//			String nomFichier = mapDocuments.get("afbm:docRef") + "_" +mapDocuments.get("afbm:unitCode") + "_" + mapDocuments.get("afbm:trxAcc").split("-")[0];
//			nomFichier = String.join("_", mapDocuments.get("afbm:docRef"),
//					mapDocuments.get("afbm:unitCode"), mapDocuments.get("afbm:trxAcc").split("-")[0]);
//
			String timestampTraitement = FilenameUtils.removeExtension(file.getName());
			String[] interTraitementArray = timestampTraitement.split("_");
			if (interTraitementArray.length == 2) {
				timestampTraitement = interTraitementArray[0];
			}
			else if(interTraitementArray.length == 3) {
				timestampTraitement = interTraitementArray[1];
			}
			//OPE-DATE_OPE-CPT-EVE-ONDB-Nombre aléatoire
			nomFichier = String.join("_", mapDocuments.get("afbm:docType"), mapDocuments.get("afbm:trxDate"), mapDocuments.get("afbm:trxAcc").split("-")[0],
					mapDocuments.get("afbm:docRef"), timestampTraitement);

			data.put("dateKeys", "afbm:trxDate");
//			data.put("fileName", "arch_" + file.getName().replace("tmp_", "").replace("image-", "") + "_" + nomFichier);
			data.put("fileName", "arch_" + nomFichier);
			data.put("rootPath", "/Sites/afriland/documentLibrary");
			data.put("destPath", destfolder+'/'+reper);
			data.put("aspects", defaultAspect);
			//data.put("folderId", "afb_numarch");
			data.put("type", "afbm:doc");

			//cm:titled
			metadata.put("cm:title", "afbm:doc".toUpperCase());
			metadata.put("cm:description", "DESCRIPTION - afbm:doc".toUpperCase());
			//System.out.println("Valid JSON: "+isValid(metadata.toString()));
			data.put("metadata", metadata);

			// Creation du dossier du site afriland dans acs
			log.info("FILE-NAME: {}", nomFichier);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			restTemplate = new RestTemplate();
			HttpEntity<String> reqEntity = new HttpEntity<>(varia.toString(), headers);
			log.info("DATA-FOLDER: " + urlFolder + "/" + reper);
			restTemplate.postForObject(urlFolder, reqEntity, JSONObject.class);

			// Envoi du fichier et des metadata dans acs
			headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);

			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			//System.out.println("DATA: " + urlSave);
			body.set("data", data.toString());
			InputStream is;
			ByteArrayResource contentsAsResource;

			//Convertir document en inputstream
			is = new FileInputStream(file);

			contentsAsResource = new ByteArrayResource(IOUtils.toByteArray(is)) {
				@Override
				public String getFilename() {
					return file.getName(); // Filename has to be returned in order to be able to post.
				}
			};
			is.close();

			body.set("file", contentsAsResource);
			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

			log.info("DATA: " + urlSave + "/" + file.getName());
			restTemplate = new RestTemplate();
			lienDoc = restTemplate.postForEntity(urlSave, requestEntity, String.class).getBody();

		} catch (Exception e) {

			log.error("ERREUR LORS DE L'ENVOI VERS ALFRESCO " +file.getName());

		}

		return lienDoc;
	}

}