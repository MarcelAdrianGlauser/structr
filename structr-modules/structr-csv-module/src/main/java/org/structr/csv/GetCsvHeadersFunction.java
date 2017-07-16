/**
 * Copyright (C) 2010-2017 Structr GmbH
 *
 * This file is part of Structr <http://structr.org>.
 *
 * Structr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Structr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Structr.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.structr.csv;

import java.io.StringReader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.QuoteMode;
import org.structr.schema.action.ActionContext;
import org.structr.web.function.UiFunction;

/**
 *
 */
public class GetCsvHeadersFunction extends UiFunction {

	public static final String ERROR_MESSAGE_FROM_CSV    = "Usage: ${get_csv_headers(source[, delimiter[, quoteChar[, recordSeparator]]])}. Example: ${get_csv_headers('COL1;COL2;COL3\none;two;three')}";
	public static final String ERROR_MESSAGE_FROM_CSV_JS = "Usage: ${{Structr.getCsvHeaders(source[, delimiter[, quoteChar[, recordSeparator]]])}}. Example: ${{Structr.getCsvHeaders('COL1;COL2;COL3\none;two;three')}}";

	@Override
	public String getName() {
		return "get_csv_headers()";
	}

	@Override
	public Object apply(final ActionContext ctx, final Object caller, final Object[] sources) {

		if (arrayHasMinLengthAndMaxLengthAndAllElementsNotNull(sources, 1, 4)) {

			try {

				final String source    = sources[0].toString();
				String delimiter       = ";";
				String quoteChar       = "\"";
				String recordSeparator = "\n";

				switch (sources.length) {

					case 4: recordSeparator = (String)sources[3];
					case 3: quoteChar = (String)sources[2];
					case 2: delimiter = (String)sources[1];
						break;
				}

				CSVFormat format = CSVFormat.newFormat(delimiter.charAt(0)).withHeader();
				format = format.withQuote(quoteChar.charAt(0));
				format = format.withRecordSeparator(recordSeparator);
				format = format.withIgnoreEmptyLines(true);
				format = format.withIgnoreSurroundingSpaces(true);
				format = format.withQuoteMode(QuoteMode.ALL);

				try (final CSVParser parser = new CSVParser(new StringReader(source), format)) {

					return parser.getHeaderMap().keySet();
				}

			} catch (Throwable t) {

				logException(t, "{}: Exception for parameter: {}", new Object[] { getName(), getParametersAsString(sources) });

			}

			return "";

		} else {

			logParameterError(caller, sources, ctx.isJavaScriptContext());

		}

		return usage(ctx.isJavaScriptContext());
	}

	@Override
	public String usage(boolean inJavaScriptContext) {
		return (inJavaScriptContext ? ERROR_MESSAGE_FROM_CSV_JS : ERROR_MESSAGE_FROM_CSV);
	}

	@Override
	public String shortDescription() {
		return "Parses the given CSV string and returns a list of column headers";
	}
}