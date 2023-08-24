package com.firstbank.arch.service;

import java.io.IOException;

import com.firstbank.arch.util.ExtractImageFromPdf;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class FileExtractImgService {


	private final ExtractImageFromPdf extImg;
	public Integer extractImages() throws IOException, InterruptedException {

		//ExtractImageFromPdf extImg = new ExtractImageFromPdf();
        return extImg.extractJPGImages();
    
	}
}
