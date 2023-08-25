package com.firstbank.arch.util;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.imageio.ImageIO;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import com.firstbank.arch.util.code.Barcode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
public class ExtractImagesUseCase extends PDFStreamEngine{

	private String filePath;
    private final String outputDir;
	private String inputs;
	private String scans;
    private List<String> listBase64;
	private int nbrePages;

	public ExtractImagesUseCase(String outputDir, String scans, String inputs){

		this.outputDir = outputDir;
		this.scans = scans;
		this.inputs = inputs;

	}

	public ExtractImagesUseCase(String filePath, String outputDir, String scans, String inputs){

		this.filePath = filePath;
		this.outputDir = outputDir;
		this.scans = scans;
		this.inputs = inputs;
		listBase64 = new ArrayList<>();

	}
    
    public List<String> execute() throws IOException{

		File file = new File(filePath);
		byte[] bytes = FileUtils.readFileToByteArray(file);

		try(PDDocument document = PDDocument.load(bytes)) {

			nbrePages = document.getNumberOfPages();

			for(PDPage page : document.getPages()){
                processPage(page);
			}

			processFile2(this.outputDir);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return listBase64;
    }
    
    @Override
    protected void processOperator(Operator operator, List<COSBase> operands) throws IOException{

        String operation = operator.getName();

		if("Do".equals(operation)){

			COSName objectName = (COSName) operands.get(0);
            PDXObject pdxObject = getResources().getXObject(objectName);

            if(pdxObject instanceof PDImageXObject image){

                BufferedImage bImage = image.getImage();
                String randomName = "image-"+(nbrePages--);

                File outputFile = new File(outputDir, randomName + ".jpg");

                // Write image to file
                ImageIO.write(bImage, "JPG", outputFile);
                
                byte[] fileContent = Files.readAllBytes(outputFile.toPath());
                String code =  Base64.getEncoder().encodeToString(fileContent);
                listBase64.add(code);

			}else if(pdxObject instanceof PDFormXObject form){

				showForm(form);

			}
        }

        else super.processOperator(operator, operands);
    }

	/**
	 * process le images pour detecter les qr codes
	 * @param outDir - repertoire scan des images
	 */
	public Integer processFile2(String outDir) throws InterruptedException {

		List<String> swap;
		List<Data> result = new ArrayList<>();
		Barcode brd = new Barcode();
		swap = brd.decodeFolderBis(outDir);

		Path p = Paths.get(outDir);

		if (swap != null) {
			swap.add(p.getFileName().toString());
			result.addAll(transform(swap));
			log.info("result "+result.size());
		}

		return createInputFiles(result);

	}
    
   private List<Data>  transform(List<String> swap) {

	   List<Data> resData = new ArrayList<>(swap.size() - 1);
	   List<String> doublon = new ArrayList<>(2);
	   Data data;
	   String[] result;
	   //String ss;
	   List<String> _swap = swap.subList(0, swap.size() - 1);
	   String folder = swap.get(swap.size() - 1);

	   for (String s : _swap) {

		   data = new Data();

//		   log.info("SS-----:  " + ss);
		   result = s.split(";");
		   data.setFoldername(folder);


		   if (result.length != 2 || "ERROR".equals(result[1]) || result[1].length() < 50 || result[1].length() > 57) {
			   data.setFilename(result[0]);
			   data.setDonne("ERROR");
			   data.setDecode(Boolean.FALSE);

		   } else {

			   doublon.add(result[1]);
//	        	System.out.println("result[0]: "+result[0]);
//			   System.out.println("doublon size: "+doublon.size());
			   if (doublon.size() == 2 && doublon.get(0).equals(doublon.get(1))) {

				   data = resData.get(resData.size() - 1);
				   //	System.out.println("doublon data-0: "+doublon.get(0)+";"+data.getFilename());
				   //	System.out.println("doublon data-1: "+doublon.get(1));
				   try {
					   FileUtils.deleteDirectory(new File(inputs + String.join("/", data.getFoldername(),
							   FilenameUtils.removeExtension(data.getFilename()), data.getFilename())));
				   } catch (IOException e) {
					   e.printStackTrace();
				   }
				   data.setDonne("ERROR");
				   data.setDecode(Boolean.FALSE);
				   //resData.remove(resData.size() - 1);
				   //resData.add(data);

				   doublon.clear();
			   }
			   if (doublon.size() == 3 && doublon.get(1).equals(doublon.get(2))) {

				   data = resData.get(resData.size() - 1);

				   try {
					   FileUtils.deleteDirectory(new File(inputs + String.join("/", data.getFoldername(),
							   FilenameUtils.removeExtension(data.getFilename()), data.getFilename())));
				   } catch (IOException e) {
					   e.printStackTrace();
				   }

				   data.setDonne("ERROR");
				   data.setDecode(Boolean.FALSE);
				   //resData.remove(resData.size() - 1);
				   //resData.add(data);
				   doublon.clear();
			   } else if (doublon.size() == 3) {
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

	   String prevFolder = "";
	   Path path;
	   Path tmpPath;
	   List<String> transDirectories = new ArrayList<>();
	   Integer numDossierFound = 0;

	   for(Data data : folderData) {

		   try {

			   path = Paths.get(inputs + data.getFoldername());

			   if(!Files.exists(path)) {

				   Files.createDirectories(path);

				   numDossierFound = getnumDossierFound(prevFolder, transDirectories, numDossierFound);
			   }

			   prevFolder = data.getFoldername();
			   transDirectories.add(data.getFilename());

			   if(Boolean.TRUE.equals(data.getDecode())) {

				   tmpPath = Paths.get(inputs + String.join("/", data.getFoldername(), FilenameUtils.removeExtension(data.getFilename())));

				   Files.createDirectories(tmpPath);

				   // Copie des fichier de scan vers inputs
//				   for (String p : transDirectories) {
//
//					   source = Paths.get(scans + String.join("/", data.getFoldername(), p));
//					   target = Paths.get(inputs + String.join("/", data.getFoldername(), FilenameUtils.removeExtension(data.getFilename()), p));
//
//					   Files.copy(source, target);
//
//				   }
				   moveFileToDirectory(transDirectories, data.getFoldername(), data.getFilename());

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

		   numDossierFound = getnumDossierFound(prevFolder, transDirectories, numDossierFound);

	   } catch (IOException e) {
		   e.printStackTrace();
	   }


	   try {

		   log.info("--- Suppression du repertoire scan ---");
		   FileUtils.deleteDirectory(new File(this.outputDir));

	   } catch (Exception e) {
		   e.printStackTrace();
	   }

	   //

	   return numDossierFound;

   }

	private Integer getnumDossierFound(String prevFolder, List<String> transDirectories, Integer numDossierFound) throws IOException {

		String interFolder;
		Path tmpPath;
		if(!transDirectories.isEmpty()) {

			interFolder = transDirectories.get(transDirectories.size() - 1);
			tmpPath = Paths.get(inputs + String.join("/", prevFolder, FilenameUtils.removeExtension(interFolder)));

			Files.createDirectories(tmpPath);

			moveFileToDirectory(transDirectories, prevFolder, interFolder);
			String ch = inputs + prevFolder + "/" + FilenameUtils.removeExtension(interFolder) + "/";
			extracted(null, ch);
			numDossierFound++;
			transDirectories.clear();
		}
		return numDossierFound;
	}

	private void moveFileToDirectory(List<String> transDirectories, String prevFolder,String interFolder) {
		Path source;
		Path target;

		for (String p : transDirectories) {
			source = Paths.get(scans + String.join("/", prevFolder, p));
			target = Paths.get(inputs + String.join("/", prevFolder, FilenameUtils.removeExtension(interFolder), p));

			try {
				Files.copy(source, target);
			} catch (IOException e) {
				log.info("Probleme lors de la copie vers inputs");
			}

		}
	}

	private void extracted(Data data, String prevFolder){

		org.json.simple.JSONObject fileResponseObject;
		String respData;
		fileResponseObject = new org.json.simple.JSONObject();
		Path filesPath;


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

				filesPath = Paths.get(inputs + String.join("/", data.getFoldername(),
						FilenameUtils.removeExtension(data.getFilename()), "data.json"));

				log.info("Data not null: {} - {} - {}", respData.substring(0, 6),  respData.substring(11, 22)
						, respData.substring(50));

			} else {

				fileResponseObject.put("setEve", "");
				fileResponseObject.put("setAge", "");
				fileResponseObject.put("setNcp", "");
				fileResponseObject.put("setCle", "");
				fileResponseObject.put("setDco", "");
				fileResponseObject.put("setUti", "");
				fileResponseObject.put("setMon", 0);
				fileResponseObject.put("setType", "");

				filesPath = Paths.get(prevFolder + "/data.json");
				log.info("Data null: " + filesPath);
			}

			// Ecriture dans le fichier
			try(
					FileWriter file = new FileWriter(filesPath.toString(), true);
					BufferedWriter bw = new BufferedWriter(file);
			) {
				bw.write(fileResponseObject.toJSONString());
				bw.newLine();
			}

		} catch(Exception e) {
			e.printStackTrace();
		}
   }

}
