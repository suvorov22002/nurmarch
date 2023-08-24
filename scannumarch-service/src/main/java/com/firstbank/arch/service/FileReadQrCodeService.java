package com.firstbank.arch.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.firstbank.arch.util.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.stereotype.Service;

//import com.afb.dsi.ddil.scanToAlfresco.App;
import com.firstbank.arch.util.code.Barcode;



@Service
public class FileReadQrCodeService {

	//private App a;
	private Barcode brd;
	private List<String> resultWork = new ArrayList<String>();
	//private List<List<String>> resultWorks = new ArrayList<>();
	private final static String PATHNAME = "C://numarch/scans/";
	private File directory = new File(PATHNAME);
	private Path filePath;
	private List<String> tempDirectories;
	JSONObject ob = new JSONObject();
	JSONArray arrOb = new JSONArray();

    /*
	public Integer processFile() {

		a = new App();
		filePath = Paths.get(PATHNAME);
		Set<String> listSet = new HashSet<>();
		//List<String> result = new ArrayList<String>();
		List<List<Data>> resultWorks = new ArrayList<>();
		Set<List<Data>> _resultWorks = new HashSet<>();
		List<Data> result = new ArrayList<Data>();

		if (directory.exists() && directory.isDirectory()) {

			// Stream<Path> stream = Files.list(Paths.get(directory.getAbsolutePath()));
			File[] listSets = directory.listFiles();
			listSet =  Arrays.asList(listSets).stream().map(m -> m.toPath())
					.filter(file -> Files.isDirectory(file))
					.map(Path::getFileName)
					.map(Path::toString)
					.collect(Collectors.toSet());

			//	listSet.forEach(System.out::println);
			tempDirectories = new ArrayList<>(listSet.size());
			
			List<String> swap = new ArrayList<String>();
			String ref;
			Iterator<String> it = listSet.iterator();
			while (it.hasNext()) {

				ref = it.next();
				filePath = Paths.get(PATHNAME + ref);
				tempDirectories.add(ref);
				a = new App();
				//result = new ArrayList<Data>();
				ob = new JSONObject();
				swap = a.process(filePath.toString());
				System.out.println("**********swap: " + swap.size());
				if (swap != null) {
					swap.add(ref);
					result.addAll(transform(swap));
					System.out.println(result.size());
				
//					try {
//						ob.put(ref, result);
//						resultWorks.add(result);
//					} catch (JSONException e) {
//						
//						e.printStackTrace();
//					}
					
				 }
					
			}

		}

		return createInputFiles(result);
		
	}
	
	*/

	public Integer processFile2() throws InterruptedException {

		//a = new App();
		filePath = Paths.get(PATHNAME);
		Set<String> listSet;
		List<Data> result = new ArrayList<>();

		if (directory.exists() && directory.isDirectory()) {

			// Stream<Path> stream = Files.list(Paths.get(directory.getAbsolutePath()));
			File[] listSets = directory.listFiles();
			assert listSets != null;
			listSet =  Arrays.stream(listSets).map(File::toPath)
					.filter(Files::isDirectory)
					.map(Path::getFileName)
					.map(Path::toString)
					.collect(Collectors.toSet());

			//	listSet.forEach(System.out::println);
			tempDirectories = new ArrayList<>(listSet.size());

			List<String> swap;
			String ref;
			Iterator<String> it = listSet.iterator();

			while (it.hasNext()) {

				ref = it.next();

				tempDirectories.add(ref);

				brd = new Barcode();
				swap = brd.decodeFolder(ref);

				if (swap != null) {

					swap.add(ref);
					result.addAll(transform(swap));

				}

			}

		}

		return createInputFiles(result);

	}

	private List<Data>  transform(List<String> swap) {

		List<Data> resData = new ArrayList<Data>(swap.size() - 1);
		List<String> doublon = new ArrayList<>(2);
		Data data;
		String[] result;
		String ss;
		List<String> _swap = swap.subList(0, swap.size() - 1);
		String folder = swap.get(swap.size() - 1);

		String str;
		for(int i = 0; i < _swap.size(); i++) {
			data = new Data();
			str = _swap.get(i);

			ss = (String)str;
			result = ss.split(";");
			data.setFoldername(folder);


			if(result.length != 2  || "ERROR".equals(result[1]) || result[1].length() < 50 || result[1].length() > 57) {
				data.setFilename(result[0]);
				data.setDonne("ERROR");
				data.setDecode(Boolean.FALSE);

			} else {

				doublon.add(result[1]);
//	        	System.out.println("result[0]: "+result[0]);
//	        	System.out.println("doublon size: "+doublon.size());
				if(doublon.size() == 2 && doublon.get(0).equals(doublon.get(1))) {

					data = resData.get(resData.size() - 1);
					//	System.out.println("doublon data-0: "+doublon.get(0)+";"+data.getFilename());
					//	System.out.println("doublon data-1: "+doublon.get(1));
					try {
						FileUtils.deleteDirectory(new File("C://numarch//inputs/" + data.getFoldername() + "/" + FilenameUtils.removeExtension(data.getFilename()) + "/" + data.getFilename()));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					data.setDonne("ERROR");
					data.setDecode(Boolean.FALSE);
					//resData.remove(resData.size() - 1);
					//resData.add(data);

					doublon.clear();
				}
				if(doublon.size() == 3 && doublon.get(1).equals(doublon.get(2))) {

					data = resData.get(resData.size() - 1);
					//  	System.out.println("doublon data-0: "+doublon.get(0)+";"+data.getFilename());
					//  	System.out.println("doublon data-1: "+doublon.get(1));
					try {
						FileUtils.deleteDirectory(new File("C://numarch//inputs/" + data.getFoldername() + "/" + FilenameUtils.removeExtension(data.getFilename()) + "/" + data.getFilename()));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					data.setDonne("ERROR");
					data.setDecode(Boolean.FALSE);
					//resData.remove(resData.size() - 1);
					//resData.add(data);
					doublon.clear();
				} else if(doublon.size() == 3) {
					doublon.clear();
				}

				data = new Data();
				data.setFoldername(folder);
				data.setFilename(result[0]);
				data.setDonne(result[1]);//.replaceAll("\\s", "").trim()
				data.setDecode(Boolean.TRUE);
			}

			resData.add(data);
		}

		return resData;
	}

	private Integer createInputFiles(List<Data> folderData) {

		String interFolder;
		String prevFolder = "";
		Path path;
		Path tmpPath;
		Path source;
		Path target;

		List<String> transDirectories = new ArrayList<>();
		Integer numDossierFound = 0;

		for(Data data : folderData) {

			try {
				path = Paths.get("C://numarch/inputs/" + data.getFoldername());

				if(!Files.exists(path)) {
					Files.createDirectories(path);

					if(!transDirectories.isEmpty()) {
						interFolder = transDirectories.get(transDirectories.size() - 1);
						tmpPath = Paths.get("C://numarch/inputs/" + prevFolder + "/" + FilenameUtils.removeExtension(interFolder));
						Files.createDirectories(tmpPath);

						for (String p : transDirectories) {
							source = Paths.get("C://numarch/scans/" +prevFolder + "/" + p);
							target = Paths.get("C://numarch/inputs/" + prevFolder + "/" + FilenameUtils.removeExtension(interFolder) + "/" + p);

							//Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
							Files.copy(source, target);
						}
						String ch = "C://numarch/inputs/" + prevFolder + "/" + FilenameUtils.removeExtension(interFolder) + "/";
						extracted(null, ch);
						numDossierFound++;
						transDirectories.clear();
					}
				}
				prevFolder = data.getFoldername();
				if(!data.getDecode()) {

					transDirectories.add(data.getFilename());

				} else {
					transDirectories.add(data.getFilename());
					tmpPath = Paths.get("C://numarch/inputs/" + data.getFoldername() + "/" + FilenameUtils.removeExtension(data.getFilename()));
					Files.createDirectories(tmpPath);


					for (String p : transDirectories) {

						source = Paths.get("C://numarch/scans/" + data.getFoldername() + "/" + p);
						target = Paths.get("C://numarch/inputs/" + data.getFoldername() + "/" + FilenameUtils.removeExtension(data.getFilename()) + "/" + p);

						//	Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
						//copyFile(String chemin1, String chemin2 );
						Files.copy(source, target);

					}


					//		System.out.println("Data: " + (count++) + " | " + data.getData());
					extracted(data, null);
					numDossierFound++;
					transDirectories.clear();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		try {

			if(!transDirectories.isEmpty()) {
				interFolder = transDirectories.get(transDirectories.size() - 1);
				tmpPath = Paths.get("C://numarch/inputs/" + prevFolder + "/" + FilenameUtils.removeExtension(interFolder));
				Files.createDirectories(tmpPath);

				for (String p : transDirectories) {
					source = Paths.get("C://numarch/scans/" + prevFolder + "/" + p);
					target = Paths.get("C://numarch/inputs/" + prevFolder + "/" + FilenameUtils.removeExtension(interFolder) + "/" + p);

					//	Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
					Files.copy(source, target);

				}
				String ch = "C://numarch/inputs/" + prevFolder + "/" + FilenameUtils.removeExtension(interFolder) + "/";
				extracted(null, ch);
				numDossierFound++;
				transDirectories.clear();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}


		try {
			for (String dir : tempDirectories) {
				System.out.println("--- Suppression du repertoire scan ---");
				FileUtils.deleteDirectory(new File("C://numarch/scans/" + dir));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return numDossierFound;

	}

	private void extracted(Data data, String prevFolder){

		org.json.simple.JSONObject fileResponseObject;
		String respData;
		fileResponseObject = new org.json.simple.JSONObject();
		Path _filePath = null;

		try {
			if (data != null) {
				respData = data.getDonne();

				fileResponseObject.put("setEve", respData.substring(0, 6));
				fileResponseObject.put("setAge", respData.substring(6, 11));
				fileResponseObject.put("setNcp", respData.substring(11, 22));
				fileResponseObject.put("setCle", respData.substring(22, 24));
				fileResponseObject.put("setDco", respData.substring(24, 32));
				fileResponseObject.put("setUti", respData.substring(32, 36));
				fileResponseObject.put("setMon", Integer.parseInt(respData.substring(36, 50).split(",")[0]));
				fileResponseObject.put("setType", respData.substring(50));

				_filePath = Paths.get("C://numarch/inputs/" + data.getFoldername() + "/" + FilenameUtils.removeExtension(data.getFilename()) + "/data.json");
			} else {

				fileResponseObject.put("setEve", "");
				fileResponseObject.put("setAge", "");
				fileResponseObject.put("setNcp", "");
				fileResponseObject.put("setCle", "");
				fileResponseObject.put("setDco", "");
				fileResponseObject.put("setUti", "");
				fileResponseObject.put("setMon", 0);
				fileResponseObject.put("setType", "");

				_filePath = Paths.get(prevFolder + "/data.json");
			}


		} catch(Exception e) {

		}

		try (FileWriter file = new FileWriter(_filePath.toString(), true)) {

			BufferedWriter bw = new BufferedWriter(file);
			bw.write(fileResponseObject.toJSONString());
			bw.newLine();
			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void copyFile(String chemin1, String chemin2 ) {

		try {
			FileInputStream fis = new FileInputStream(chemin1);
			FileOutputStream fos = new FileOutputStream(chemin2);

			int b;
			while ((b = fis.read()) != -1)
				fis.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}
}
