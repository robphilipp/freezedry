/*
 * Copyright 2012 Robert Philipp
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.freezedry.persistence.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert Philipp
 */
public class Constants {

	//
	// Date Formats
	//
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final DateFormat DATE_FORMATTER = new SimpleDateFormat( DATE_FORMAT );
	public static final List< String > DATE_FORMATS;
	static {
		DATE_FORMATS = new ArrayList<>();
		DATE_FORMATS.add( "MMM dd, yyyy" );
		DATE_FORMATS.add( "MMM dd, yy" );
		DATE_FORMATS.add( "dd MMM yyyy" );
		DATE_FORMATS.add( "dd MMM yy" );

		DATE_FORMATS.add( "dd-MMM-yy" );
		DATE_FORMATS.add( "dd-MMM-yyyy" );
		DATE_FORMATS.add( "dd.MMM.yy" );
		DATE_FORMATS.add( "dd.MMM.yyyy" );
		
		DATE_FORMATS.add( "yyyy.MM.dd" );
		DATE_FORMATS.add( "yyyy-MM-dd" );
		DATE_FORMATS.add( "yyyyMMdd" );

		DATE_FORMATS.add( "MM/dd/yyyy" );
		DATE_FORMATS.add( "dd/MMM/yyyy" );
		
		// ISO 8601 (strict)
		DATE_FORMATS.add( "yyyy-MM-dd'T'HH:mm:ss.SSSZ" );
	}
	
	//
	// Newline
	//
	public static final String NEW_LINE = System.getProperty( "line.separator" );
	
}
