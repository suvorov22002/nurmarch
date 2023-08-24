package com.firstbank.arch.util;

import java.io.Serializable;


public class Data implements Serializable{

	private String foldername;
	private String filename;
	private String donne;
	private Boolean decode;

	public String getFoldername() {
		return foldername;
	}

	public void setFoldername(String foldername) {
		this.foldername = foldername;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getDonne() {
		return donne;
	}

	public void setDonne(String donne) {
		this.donne = donne;
	}

	public Boolean getDecode() {
		return decode;
	}

	public void setDecode(Boolean decode) {
		this.decode = decode;
	}
}
