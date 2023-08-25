package com.firstbank.arch.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Date;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RefactorDateUtil {

	public static final String DATE_HOUR_FORMAT_TT="yyMMddHHmmssSSSS";

	public static Long now(){
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_HOUR_FORMAT_TT);
		String format = simpleDateFormat.format(new Date());
		return Long.valueOf(format) ;
	}
}
