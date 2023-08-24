package com.firstbank.arch.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RefactorDateUtil {
	
	public final static String DATE_HOUR_FORMAT_TT="yyMMddHHmmssSSSS";
	
	public static Long now(){
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_HOUR_FORMAT_TT);
		String format = simpleDateFormat.format(new Date());
		return Long.valueOf(format) ;
	}
}
