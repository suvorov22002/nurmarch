package com.firstbank.arch.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class ExtractImageFromPdf {

	@Value("${application.path.scans}")
	private String pathname;

	@Value("${application.path.in}")
	private String filename;

	/**
	 * read all pdf in directory and store it in set.
	 * @return - file in base64
	 */
	public Integer extractJPGImages() throws IOException, InterruptedException {

		File directory = new File(filename);
		Set<String> listSet;
		List<String> listBase64 = new ArrayList<>();

		if (directory.exists() && directory.isDirectory()) {

			try (Stream<Path> stream = Files.list(Paths.get(directory.getAbsolutePath()))) {

				listSet =  stream
						.filter(file -> !Files.isDirectory(file))
						.map(Path::getFileName)
						.map(Path::toString)
						.collect(Collectors.toSet());

				listBase64 = extractJPGImage(listSet);

			}
		}

		return listBase64.size();

	}

	/**
	 * extract images from pdf file and group them in directory scan
	 * @param listSet - set of all pdf files containing images
	 * @return - list of file processed
	 */
	public List<String> extractJPGImage(final Set<String> listSet) throws InterruptedException {

		if (listSet.isEmpty()) {
			return Collections.emptyList();
		}

		final List<String> listSetpack = new ArrayList<>();
		final List<Callable<List<String>>> tasks = new ArrayList<>();

		// On recupere le nombre de coeur du processeur pour lancer les jobs en parrallèle
		final int threadCount = Math.min(
				Runtime.getRuntime().availableProcessors(),
				listSet.size()
		);

		final ExecutorService executor = Executors.newFixedThreadPool(threadCount);

		File directory = new File(filename);

		try {

			for (final String file : listSet) {

				tasks.add(
						() -> {
							Path filePath;
							filePath = Paths.get(directory.getAbsolutePath(), file);
							Path path;
							List<String> listBase64;

							if (FilenameUtils.getExtension(file).equals("pdf")) {

								// On Crée le repertoire de même nom dans scan pour stocker les images extraites
								path = Paths.get(pathname + FilenameUtils.removeExtension(file));
								Files.createDirectories(path);

								// On lit le fichier correspondant dans le repertoire in pour commencer l'extraction
								// on indique le repertoire de stockage des images scan
								ExtractImagesUseCase useCase = new ExtractImagesUseCase(filePath.toString(), path.toString());
								listBase64 = useCase.execute();

								return listBase64;

							}
							return null;
						}
				);
			}

			// On lance toutes les tâches parallèles et on attend leur realisation
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
}
