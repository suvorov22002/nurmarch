package com.firstbank.arch.util.code;



import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import com.dynamsoft.dbr.BarcodeReader;
import com.dynamsoft.dbr.BarcodeReaderException;
import com.dynamsoft.dbr.EnumBarcodeFormat;
import com.dynamsoft.dbr.EnumBarcodeFormat_2;
import com.dynamsoft.dbr.EnumConflictMode;
import com.dynamsoft.dbr.PublicRuntimeSettings;
import com.dynamsoft.dbr.TextResult;
import com.firstbank.arch.util.SortFileByName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class Barcode {

	private static int barcodeFormatIds;
	private static int barcodeFormatIds_2;

	@Value("${application.path.scans}")
	private String pathname;

	public Barcode() {
		barcodeFormatIds = 0;
		barcodeFormatIds_2 = 0;
	}

	private static int GetFormatID(int iIndex) {
		int ret = 0;

		switch (iIndex) {
			case 1:
				barcodeFormatIds = EnumBarcodeFormat.BF_ALL;
				barcodeFormatIds_2 = EnumBarcodeFormat_2.BF2_POSTALCODE | EnumBarcodeFormat_2.BF2_DOTCODE;
				break;
			case 2:
				barcodeFormatIds = EnumBarcodeFormat.BF_ONED;
				barcodeFormatIds_2 = 0;
				break;
			case 3:
				barcodeFormatIds = EnumBarcodeFormat.BF_QR_CODE;
				barcodeFormatIds_2 = 0;
				break;
			case 4:
				barcodeFormatIds = EnumBarcodeFormat.BF_CODE_39;
				barcodeFormatIds_2 = 0;
				break;
			case 5:
				barcodeFormatIds = EnumBarcodeFormat.BF_CODE_128;
				barcodeFormatIds_2 = 0;
				break;
			case 6:
				barcodeFormatIds = EnumBarcodeFormat.BF_CODE_93;
				barcodeFormatIds_2 = 0;
				break;
			case 7:
				barcodeFormatIds = EnumBarcodeFormat.BF_CODABAR;
				barcodeFormatIds_2 = 0;
				break;
			case 8:
				barcodeFormatIds = EnumBarcodeFormat.BF_ITF;
				barcodeFormatIds_2 = 0;
				break;
			case 9:
				barcodeFormatIds = EnumBarcodeFormat.BF_INDUSTRIAL_25;
				barcodeFormatIds_2 = 0;
				break;
			case 10:
				barcodeFormatIds = EnumBarcodeFormat.BF_EAN_13;
				barcodeFormatIds_2 = 0;
				break;
			case 11:
				barcodeFormatIds = EnumBarcodeFormat.BF_EAN_8;
				barcodeFormatIds_2 = 0;
				break;
			case 12:
				barcodeFormatIds = EnumBarcodeFormat.BF_UPC_A;
				barcodeFormatIds_2 = 0;
				break;
			case 13:
				barcodeFormatIds = EnumBarcodeFormat.BF_UPC_E;
				barcodeFormatIds_2 = 0;
				break;
			case 14:
				barcodeFormatIds = EnumBarcodeFormat.BF_PDF417;
				barcodeFormatIds_2 = 0;
				break;
			case 15:
				barcodeFormatIds = EnumBarcodeFormat.BF_DATAMATRIX;
				barcodeFormatIds_2 = 0;
				break;
			case 16:
				barcodeFormatIds = EnumBarcodeFormat.BF_AZTEC;
				barcodeFormatIds_2 = 0;
				break;
			case 17:
				barcodeFormatIds = EnumBarcodeFormat.BF_CODE_39_EXTENDED;
				barcodeFormatIds_2 = 0;
				break;
			case 18:
				barcodeFormatIds = EnumBarcodeFormat.BF_MAXICODE;
				barcodeFormatIds_2 = 0;
				break;
			case 19:
				barcodeFormatIds = EnumBarcodeFormat.BF_GS1_DATABAR;
				barcodeFormatIds_2 = 0;
				break;
			case 20:
				barcodeFormatIds = EnumBarcodeFormat.BF_PATCHCODE;
				barcodeFormatIds_2 = 0;
				break;
			case 21:
				barcodeFormatIds = EnumBarcodeFormat.BF_GS1_COMPOSITE;
				barcodeFormatIds_2 = 0;
				break;
			case 22:
				barcodeFormatIds = 0;
				barcodeFormatIds_2 = EnumBarcodeFormat_2.BF2_POSTALCODE;
				break;
			case 23:
				barcodeFormatIds = 0;
				barcodeFormatIds_2 = EnumBarcodeFormat_2.BF2_DOTCODE;
				break;
			case 24:
				barcodeFormatIds = EnumBarcodeFormat.BF_MSI_CODE;
				barcodeFormatIds_2 = 0;
				break;
			default:
				ret = -1;
				break;
		}
		return ret;
	}

	private String decode(String pszImageFile) {

		int iMaxCount = 1; //number of qrcode in doc
		int ret = 3;
		int iIndex = 3; // type qr code
		String decodeText = "ERROR";

		java.io.File file = new java.io.File(pszImageFile);
         if (!file.exists() || !file.isFile()) return "ERROR";


		ret = GetFormatID(iIndex);

		try {

			// Set license
			// https://www.dynamsoft.com/customer/license/fullLicense
			// BarcodeReader.initLicense("t0072oQAAACezmZxkmq7M49mQfCSLkocTqy7r3UfEM//rIwhPHuNiUe+Y9HUGHC2sqT8Bpr65mG6O+DyRhYYbhDGGnIxjskvTQCJC");
			// BarcodeReader.initLicense("t0072oQAAACpX0I+ZaCto/PfnHQzFcnwEbNytI/V88hjSSiiyYRk+x8NFKSTi0ephQOR2Utb78fqaHr50tRi90MZ3zA44HrIE5CMG");
			// BarcodeReader.initLicense("t0072oQAAAC2O/CM7L76WA3yutR6VauDhkeWHpFJOqE+JUw/00RtEGashX8GG6bwg3vX0/RZVQoBcmTIHPJFeu7RymCGaxnrOECI0");
			// BarcodeReader.initLicense("t0072oQAAAKHV4zm8uV9o/K3LPdz+p46/yupG7u1je7cUzulduoyGYEAjOqvonscNrZBB4O5WhbJLpmOCAIlSp24RhvZG+PTH5SIl");
			//BarcodeReader.initLicense("t0072oQAAAKqvmZqYD8GY4HUJwElVJFMx4iuqirTg9tKPMhBBn9FpkU1uBcCLNUL/QZOSRuLb4SE35oM77mTAmbeat6Ka1zjXxyJa");

			BarcodeReader.initLicense("f0068fQAAAEy/mNjcEb2d9bhd05AsnrvrLqmGo82Ri2ost1t7EJxgYISy8ayLSXgMqVYd3kb2/g8OjMFQuFXMBRJSFl1u3N4=");


			BarcodeReader br = new BarcodeReader();


			// Read barcode
             
             long ullTimeBegin = new Date().getTime();

             //Best coverage settings
             br.initRuntimeSettingsWithString("{\"ImageParameter\":{\"Name\":\"BestCoverage\",\"DeblurLevel\":9,\"ExpectedBarcodesCount\":512,\"ScaleDownThreshold\":100000,\"LocalizationModes\":[{\"Mode\":\"LM_CONNECTED_BLOCKS\"},{\"Mode\":\"LM_SCAN_DIRECTLY\"},{\"Mode\":\"LM_STATISTICS\"},{\"Mode\":\"LM_LINES\"},{\"Mode\":\"LM_STATISTICS_MARKS\"}],\"GrayscaleTransformationModes\":[{\"Mode\":\"GTM_ORIGINAL\"},{\"Mode\":\"GTM_INVERTED\"}]}}", EnumConflictMode.CM_OVERWRITE);
             //Best speed settings
             //br.initRuntimeSettingsWithString("{\"ImageParameter\":{\"Name\":\"BestSpeed\",\"DeblurLevel\":3,\"ExpectedBarcodesCount\":512,\"LocalizationModes\":[{\"Mode\":\"LM_SCAN_DIRECTLY\"}],\"TextFilterModes\":[{\"MinImageDimension\":262144,\"Mode\":\"TFM_GENERAL_CONTOUR\"}]}}",EnumConflictMode.CM_OVERWRITE);
             //Balance settings
             //br.initRuntimeSettingsWithString("{\"ImageParameter\":{\"Name\":\"Balance\",\"DeblurLevel\":5,\"ExpectedBarcodesCount\":512,\"LocalizationModes\":[{\"Mode\":\"LM_CONNECTED_BLOCKS\"},{\"Mode\":\"LM_STATISTICS\"}]}}",EnumConflictMode.CM_OVERWRITE);

             PublicRuntimeSettings runtimeSettings = br.getRuntimeSettings();
             runtimeSettings.expectedBarcodesCount = iMaxCount;
             runtimeSettings.barcodeFormatIds = barcodeFormatIds;
             runtimeSettings.barcodeFormatIds_2 = barcodeFormatIds_2;
             br.updateRuntimeSettings(runtimeSettings);

             TextResult[] results = br.decodeFile(pszImageFile, "");
             long ullTimeEnd = new Date().getTime();
             if (results == null || results.length == 0) {
                 String pszTemp = String.format("No barcode found. Total time spent: %.3f seconds.", ((float) (ullTimeEnd - ullTimeBegin) / 1000));
                 log.info(pszTemp);
                 decodeText = "ERROR";
             } else {
                 String pszTemp = String.format("Total barcode(s) found: %d. Total time spent: %.3f seconds.", results.length, ((float) (ullTimeEnd - ullTimeBegin) / 1000));
				 log.info(pszTemp);
                 iIndex = 0;

				 for (TextResult result : results) {

					 iIndex++;
                     pszTemp = "    Value: " + result.barcodeText;
                     decodeText = result.barcodeText;
					 log.info(pszTemp);

				 }
             }
             
             return file.getName()+";"+decodeText;
         } catch (BarcodeReaderException e) {
             e.printStackTrace();
         }

         return decodeText;
	}

	public List<String> decodeFolder(String folder) throws InterruptedException {

		List<String> swap;
		File[] listFileSets = new File(pathname + folder).listFiles();

		assert listFileSets != null;
		List<File> listOfFilesOrder = Arrays.asList(listFileSets);
		listOfFilesOrder.sort(new SortFileByName());

		List<File> listOfFilesOrdered =  listOfFilesOrder.stream()
				.filter(file -> !file.isDirectory())
				.toList();

		swap = extractQRCode(listOfFilesOrdered, folder);

		return swap;
	}

//	 public static void main(String[] args) {
//		 try {
//	            
//			    // 1.Initialize license.
//	            // The string "DLS2eyJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSJ9" here is a free public trial license. Note that network connection is required for this license to work.
//	            // You can also request a 30-day trial license in the customer portal: https://www.dynamsoft.com/customer/license/trialLicense?product=dbr&utm_source=samples&package=java
//			 //  BarcodeReader.initLicense("DLS2eyJvcmdhbml6YXRpb25JRCI6IjIwMDAwMSJ9");
//			   BarcodeReader.initLicense("t0072oQAAAFWhepYU6uHV+IytQaz2aOxFc+RSp7AXwoIwcPgGmRrQ/NQoGOQBhskfqWMNLdxRMIRV/Ox3EVS3ewBbvPev++jRmiJB");
//			   
//			   String chemin = "C://numarch//inputs//image-12.jpg";
//			    // 2.Create an instance of Barcode Reader.
//			    BarcodeReader dbr = new BarcodeReader();
//			   
//			    
//		        // 3.Decode barcodes from an image file.
//				TextResult[] results = dbr.decodeFile(chemin, "");
//				
//				// 4.Output the barcode text.
//				if (results != null && results.length > 0) {
//					for (int i = 0; i < results.length; i++) {
//						TextResult result = results[i];
//						System.out.println("Barcode " + i + ":" + result.barcodeText);
//					}
//				} else {
//					System.out.println("No data detected.");
//				}
//			} catch (BarcodeReaderException ex) {
//				ex.printStackTrace();
//			}
//		 
//		         
//		 //String chemin = "C://numarch//inputs//numarch432//image-141//image-4.jpg";
//		 //System.out.println(b.decodeFolder("image-141"));
//	 }


	private List<String> extractQRCode(final List<File> listOfFilesOrder, String folder) throws InterruptedException {

		if (listOfFilesOrder.isEmpty()) {
			return Collections.emptyList();
		}

		final List<String> listSetpack = new ArrayList<>();
		final List<Callable< List<String>>> tasks = new ArrayList<>();

		final int threadCount = Math.min(
				Runtime.getRuntime().availableProcessors(),
				listOfFilesOrder.size()
		);

		final ExecutorService executor = Executors.newFixedThreadPool(threadCount);


		try {
			for (final File file : listOfFilesOrder) {
				tasks.add(
						() -> {
							String ref = file.getName();
							List<String> swap = new ArrayList<>();
							String res = decode(pathname + folder + "/" + ref);
							swap.add(res);
							return swap;

						}
				);
			}

			for (final Future<List<String>> f : executor.invokeAll(tasks)) {
				try {

					if(f.get() != null) {
						listSetpack.addAll(f.get());
					}

				} catch (final ExecutionException e) {
					// This request failed.  Handle it however you like.
				}
			}
		} finally {
			executor.shutdown();
		}

		return listSetpack;
	}

	public List<String> decodeFolderBis(String folder) throws InterruptedException {

		List<String> swap;
		File[] listFileSets = new File(folder).listFiles();

		assert listFileSets != null;
		List<File> listOfFilesOrder = Arrays.asList(listFileSets);

		// Ordonne les fichiers par ordre naturel (dès la page 1 du pdf reçu)
		listOfFilesOrder.sort(new SortFileByName());

		List<File> listOfFilesOrdered =  listOfFilesOrder.stream()
				.filter(file -> !file.isDirectory())
				.toList();

		swap = extractQRCodeBis(listOfFilesOrdered, folder);

		return swap;
	}

	private List<String> extractQRCodeBis_Old(final List<File> listOfFilesOrder, String folder) throws InterruptedException {

		if (listOfFilesOrder.isEmpty()) {
			return Collections.emptyList();
		}

		final List<String> listSetpack = new ArrayList<>();
		final List<Callable< List<String>>> tasks = new ArrayList<>();

		final int threadCount = Math.min(
				Runtime.getRuntime().availableProcessors(),
				listOfFilesOrder.size()
		);

		final ExecutorService executor = Executors.newFixedThreadPool(threadCount);


		try {
			for (final File file : listOfFilesOrder) {
				tasks.add(
						() -> {
							String ref = file.getName();
							List<String> swap = new ArrayList<>();
							String res = decode(folder + "/" + ref);
							swap.add(res);
							return swap;
						}
				);
			}

			for (final Future<List<String>> f : executor.invokeAll(tasks)) {
				try {

					if(f.get() != null) {
						listSetpack.addAll(f.get());
					}

				} catch (final ExecutionException e) {
					// This request failed.  Handle it however you like.
				}
			}
		} finally {
			executor.shutdown();
		}

		return listSetpack;
	}

	private List<String> extractQRCodeBis(final List<File> listOfFilesOrder, String folder) {

		if (listOfFilesOrder.isEmpty()) {
			return Collections.emptyList();
		}

		List<String> listSetpack = new ArrayList<>();

		try {
			String ref;
			List<String> swap;

			for (final File file : listOfFilesOrder) {

				ref = file.getName();
				swap = new ArrayList<>();
				String res = decode(folder + "/" + ref);
				swap.add(res);
				listSetpack.addAll(swap);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return listSetpack;
	}

}
