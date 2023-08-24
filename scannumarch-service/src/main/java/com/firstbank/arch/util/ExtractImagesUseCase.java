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
import org.springframework.stereotype.Component;

@Slf4j
public class ExtractImagesUseCase extends PDFStreamEngine{

	private final String filePath;
    private final String outputDir;
    
    private List<String> listBase64;

	private int nbrePages;
    
    public ExtractImagesUseCase(String filePath, String outputDir){
		this.filePath = filePath;
		this.outputDir = outputDir;
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
		List<Data> result = new ArrayList<Data>();
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
	   String ss;
	   List<String> _swap = swap.subList(0, swap.size() - 1);
	   String folder = swap.get(swap.size() - 1);

	   String str;
	   for (String s : _swap) {
		   data = new Data();
		   str = s;

		   ss = str;
//		   log.info("SS-----:  " + ss);
		   result = ss.split(";");
		   data.setFoldername(folder);


		   if (result.length != 2 || "ERROR".equals(result[1]) || result[1].length() < 50 || result[1].length() > 57) {
			   data.setFilename(result[0]);
			   data.setDonne("ERROR");
			   data.setDecode(Boolean.FALSE);

		   } else {

			   doublon.add(result[1]);
//	        	System.out.println("result[0]: "+result[0]);
	        	System.out.println("doublon size: "+doublon.size());
			   if (doublon.size() == 2 && doublon.get(0).equals(doublon.get(1))) {

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
			   if (doublon.size() == 3 && doublon.get(1).equals(doublon.get(2))) {

				   data = resData.get(resData.size() - 1);
				   // 	System.out.println("doublon data-0: "+doublon.get(0)+";"+data.getFilename());
				   // 	System.out.println("doublon data-1: "+doublon.get(1));
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

	   String interFolder = "";
	   String prevFolder = "";
	   Path path, tmpPath;
	   Path source;
	   Path target;
	   List<String> transDirectories = new ArrayList<>();
	   Integer numDossierFound = 0;
//		CopyOption[] options = { StandardCopyOption.REPLACE_EXISTING,
//               StandardCopyOption.COPY_ATTRIBUTES,
//               LinkOption.NOFOLLOW_LINKS };
	   //System.out.println(" data.size(): "+ folderData.size());
	   for(Data data : folderData) {
		   //	System.out.println(" data.getFoldername(): "+ data.getFoldername());
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

		   System.out.println("--- Suppression du repertoire scan ---");
		   FileUtils.deleteDirectory(new File(this.outputDir));

	   } catch (IOException e) {
		   e.printStackTrace();
	   }

	   return numDossierFound;

   }

   private void extracted(Data data, String prevFolder){

	   org.json.simple.JSONObject fileResponseObject;
	   String respData;
	   fileResponseObject = new org.json.simple.JSONObject();
	   Path filesPath = null;


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

			   filesPath = Paths.get("C://numarch/inputs/" + data.getFoldername() + "/" + FilenameUtils.removeExtension(data.getFilename()) + "/data.json");
			   System.out.println("Data not null: " + filePath);
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
			   System.out.println("Data null: " + filesPath);
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
