package com.firstbank.arch.util;

import java.io.File;
import java.util.Comparator;

import org.apache.commons.io.FilenameUtils;

public class SortFileByName implements Comparator<File>{

		@Override
	    public int compare(File a, File b) {

	    	String num1 = FilenameUtils.removeExtension(a.getName()).replace("image-", "");
	    	String num2 = FilenameUtils.removeExtension(b.getName()).replace("image-", "");

			if (Integer.parseInt(num1) > Integer.parseInt(num2)) {
	              return 1;
	    	}

	    	if (Integer.parseInt(num1) < Integer.parseInt(num2)) {
           return -1;
	    	}
	    	return 0;
	    }
}
