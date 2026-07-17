/*
 * Copyright 2010 by Thomas Mauch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id: StringAdder.java 3425 2016-12-22 20:33:47Z origo $
 */
package org.magicwerk.strings;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.magicwerk.brownies.collections.GapList;
import org.magicwerk.brownies.collections.IList;
import org.magicwerk.strings.format.StringFormatter;
import org.magicwerk.strings.helper.CheckTools;
import org.magicwerk.strings.helper.PrintTools;
import org.magicwerk.strings.text.TextTools;

/**
 * Class {@link String printer} implements a sophisticated string printer.
 * <p>
 * It supports <br>
 * - format strings with arguments <br>
 * - replacing null values with a configured string ({@link #setNullString})<br>
 * - indenting ({@link #indent} / {@link #unindent}, use {@link #setDetectNewLines} to scan all strings before printing) <br>
 * - adding begin/join/end markers between elements<br>
 * - skip invalid elements on printing (use a variant of addIf} <br> 
 * - handle elements represented by parts where parts are separated by part markers <br>
 * - skip elements with invalid parts on printing (use a a variant of addPartsIf) <br>
 * Use toString() to get the formatted result.
 * <p>
 * There are static methods to make printing a single call operation, e.g. <br>
 * - StringPrinter.formatLines(list); <br>
 * Besides the methods accepting a collection, there are others one accepting a stream.
 * This can be used to format the entries differently or sort them , e.g. <br>
 * - StringPrinter.formatComma(list.stream().map(this::printInfo)); <br>
 * - StringPrinter.formatArray(list.stream().sorted()); <br>
 */
public class StringPrinter {

	// Design:
	// - The two different implementations for taking a single and varargs parameters have different names
	// - Reason is that if there are methods add(Object) and add(Object...), add(null) calls the add(Object...) variant 
	// - Method addAll(T... objs) must be defined using generic type T, or calling with String... reports a warning
	// - Method addAll() for adding multiple elements are provided for both Iterable and Stream
	// - Reason is that even if both Iterable and Stream offer a method iterator(), Stream does not implement Iterable
	// - (because Stream is designed for single use, i.e. iterator() method can only called once, or an error occurs:
	//   java.lang.IllegalStateException: stream has already been operated upon or closed)
	// - It would be possible to provided a single method accepting Iterator, but it would like cumbersome

	public static final String EOL = "\n";
	public static final String INDENT = "\t";
	public static final String NO_INDENT = "";
	public static final String NULL_STRING = "null";

	static class Config {
		// Configuration
		/** String used to represent EOL, used for println etc. (default is "\n") */
		String eol = EOL;
		/** If true the text to be printed is analyzed for line endings to indent them properly */
		boolean detectNewLines;
		/** String used to represent indentation (default is "\t") */
		String indent = INDENT;
		/** String used to represent null in output (default is "null"). If this string is also null, the null string is not printed */
		String nullString = NULL_STRING;
		/** Marker to print before elements */
		String beginMarker;
		/** Marker to print after elements */
		String endMarker;
		/** Marker to print between elements */
		String elemMarker;
		/** Marker to print between parts of an element */
		String partMarker;
		/** If true begin/end marker are also printed if no element is printed */
		boolean printMarkersAlways;
		/** If true, a trailing EOL at the end will be removed */
		boolean removeTrailingEol;
		Predicate<Object> printable;
		Function<Object, String> formatter;

		public Config() {
		}

		public Config(Config that) {
			assign(that);
		}

		public void assign(Config that) {
			this.eol = that.eol;
			this.detectNewLines = that.detectNewLines;
			this.indent = that.indent;
			this.nullString = that.nullString;
			this.beginMarker = that.beginMarker;
			this.endMarker = that.endMarker;
			this.elemMarker = that.elemMarker;
			this.partMarker = that.partMarker;
			this.printMarkersAlways = that.printMarkersAlways;
			this.removeTrailingEol = that.removeTrailingEol;
			this.printable = that.printable;
			this.formatter = that.formatter;
		}
	}

	// State
	Config config;
	int configLevel;
	IList<Config> configStack;
	boolean skipMarker;
	boolean beginDone;
	boolean endDone;
	StringBuilder buf;
	/** If true a new line will be started if the next output will be written, i.e. indent is written out first */
	boolean startLine;
	// Indent
	int indentLevel;
	/** String currently used to indent text (each invocation of indent() adds string indent to currentIndent) */
	String currentIndent;
	/** Stack of indent strings */
	IList<String> indentStack;

	// Static methods

	public static String formatComma(Iterable<?> args) {
		return newStringPrinterComma().addAll(args).toString();
	}

	public static String formatComma(Stream<?> stream) {
		return newStringPrinterComma().addAll(stream).toString();
	}

	static StringPrinter newStringPrinterComma() {
		return new StringPrinter().setElemMarker(", ");
	}

	public static String formatLines(Iterable<?> args) {
		return newStringPrinterLines().addAll(args).toString();
	}

	/**
	 *
	 * @param stream stream producing arguments to format
	 * @return	string holding result
	 */
	public static String formatLines(Stream<?> stream) {
		return newStringPrinterLines().addAll(stream).toString();
	}

	public static StringPrinter newStringPrinterLines() {
		return new StringPrinter().setElemMarker(EOL);
	}

	public static String formatArray(Iterable<?> args) {
		return newStringPrinterArray().addAll(args).toString();
	}

	public static String formatArray(Stream<?> stream) {
		return newStringPrinterArray().addAll(stream).toString();
	}

	public static StringPrinter newStringPrinterArray() {
		return new StringPrinter().setBeginMarker("[").setEndMarker("]").setElemMarker(", ").setPrintMarkersAlways(true);
	}

	public static String formatMap(Stream<?> stream) {
		return newStringPrinterMap().addAll(stream).toString();
	}

	public static StringPrinter newStringPrinterMap() {
		return new StringPrinter().setBeginMarker("{").setEndMarker("}").setElemMarker(", ").setPrintMarkersAlways(true);
	}

	// Construction

	public static StringPrinter fromConfig(StringPrinter stringPrinter) {
		return new StringPrinter(stringPrinter.config);
	}

	StringPrinter(Config config) {
		init(config);
	}

	/** Constructor */
	public StringPrinter() {
		init(new Config());
	}

	/** Reset printer. The content is cleared and the configuration reset to the default. */
	public void reset() {
		init(new Config());
	}

	void init(Config config) {
		this.config = config;
		buf = new StringBuilder();
		clear();
	}

	/** Clear content of printer. The formatter configuration remains unchanged. */
	public void clear() {
		buf.setLength(0);
		configStack = null;
		configLevel = 0;
		startLine = true;
		indentLevel = 0;
		currentIndent = NO_INDENT;
		indentStack = null;
		beginDone = false;
		endDone = false;
	}

	/** Reset beginDone and endDone flags, i.e. another beginMarker will be written out instead of a elemMarker. */
	public StringPrinter endMarker() {
		end();
		beginDone = false;
		endDone = false;
		return this;
	}

	/**
	 * If this method is called, printing of the next marker will be skipped.
	 * This can be used to add several strings which will not be separated by a marker.
	 * Example: printer.add("start").skipMarker().add("end")
	 */
	public StringPrinter skipMarker() {
		this.skipMarker = true;
		return this;
	}

	// Configuration

	public StringPrinter setNullString(String nullString) {
		config.nullString = nullString;
		return this;
	}

	/** Setter for {@link #indent} */
	public StringPrinter setIndent(String indent) {
		config.indent = indent;
		return this;
	}

	public StringPrinter setEol(String eol) {
		config.eol = eol;
		return this;
	}

	public StringPrinter setElemMarker(String elemMarker) {
		config.elemMarker = elemMarker;
		return this;
	}

	public StringPrinter setPartMarker(String partMarker) {
		config.partMarker = partMarker;
		return this;
	}

	public StringPrinter setBeginMarker(String beginMarker) {
		config.beginMarker = beginMarker;
		return this;
	}

	public StringPrinter setEndMarker(String endMarker) {
		config.endMarker = endMarker;
		return this;
	}

	public StringPrinter setMarkers(String beginMarker, String elemMarker, String endMarker) {
		config.beginMarker = beginMarker;
		config.elemMarker = elemMarker;
		config.endMarker = endMarker;
		return this;
	}

	public StringPrinter setMarkers(String beginMarker, String elemMarker, String endMarker, String partMarker) {
		config.beginMarker = beginMarker;
		config.elemMarker = elemMarker;
		config.endMarker = endMarker;
		config.partMarker = partMarker;
		return this;
	}

	/**
	 * Setter for {@link Config#printMarkersAlways}
	 */
	public StringPrinter setPrintMarkersAlways(boolean printMarkersAlways) {
		config.printMarkersAlways = printMarkersAlways;
		return this;
	}

	/**
	 * Setter for {@link Config#detectNewLines}
	 */
	public StringPrinter setDetectNewLines(boolean detectNewLines) {
		config.detectNewLines = detectNewLines;
		return this;
	}

	/** Setter for {@link Config#formatter} */
	@SuppressWarnings("unchecked")
	public <T extends Object> StringPrinter setFormatter(Function<T, String> formatter) {
		config.formatter = (Function<Object, String>) formatter;
		return this;
	}

	/** Setter for {@link Config#printable} */
	public StringPrinter setPrintable(Predicate<Object> printable) {
		config.printable = printable;
		return this;
	}

	public StringPrinter setPrintableIfNotEmpty() {
		setPrintable(o -> !(o == null || o.toString().isEmpty()));
		return this;
	}

	/** Setter for {@link Config#removeTrailingEol} */
	public StringPrinter setRemoveTrailingEol(boolean removeTrailingEol) {
		config.removeTrailingEol = removeTrailingEol;
		return this;
	}

	/** Getter for {@link #currentIndent} */
	public String getCurrentIndent() {
		return currentIndent;
	}

	/** Setter for {@link #currentIndent} */
	public void setCurrentIndent(String currentIndent) {
		this.currentIndent = currentIndent;
		if (indentStack != null) {
			indentStack.remove(indentLevel, indentStack.size() - indentLevel);
		}
	}

	// Method addFormat...

	/**
	 * Print formatted message as element surrounded.
	 * The element is surrounded by the defined markers.
	 */
	public StringPrinter addFormat(String format, Object... args) {
		String str = format(format, args);
		return add(str);
	}

	/**
	 * Print formatted message as element if valid for printing.
	 * Validity is determined by {@link #canPrint}.
	 * If printed, the element is surrounded by the defined markers.
	 */
	public StringPrinter addFormatIf(String format, Object... args) {
		String str = format(format, args);
		return addIf(str);
	}

	// Method addFormat...Cond

	/**
	 * Print string representation of object as element if cond is true.
	 * Validity is determined by {@link #isTrue}.
	 * If printed, the element is surrounded by the defined markers.
	 */
	public StringPrinter addFormatIfCond(Object cond, String format, Object... args) {
		if (isTrue(cond)) {
			String str = format(format, args);
			printElem(str);
		}
		return this;
	}

	/**
	 * Print string representation of object as element if cond is true.
	 * Validity is determined by {@link #isTrue}.
	 * If printed, the element is surrounded by the defined markers.
	 */
	public StringPrinter addIfCond(Object cond, Object obj) {
		if (isTrue(cond)) {
			printElem(obj);
		}
		return this;
	}

	/**
	 * Determine whether condition is true for printing for methods accepting a condition.
	 * 
	 * @param cond	condition to check
	 * @return		true if condition is true, false otherwise
	 */
	boolean isTrue(Object cond) {
		if (cond == null) {
			return false;
		}
		if (cond instanceof Boolean) {
			return ((Boolean) cond);
		}
		return true;
	}

	// Elements

	/**
	 * Print string representation of object as element.
	 * The element is surrounded by the defined markers.
	 */
	public <T> StringPrinter add(T obj) {
		printElem(obj);
		return this;
	}

	/**
	 * Print string representation of object as element if valid for printing.
	 * Validity is determined by {@link #canPrint}.
	 * If printed, the element is surrounded by the defined markers.
	 */
	public StringPrinter addIf(Object obj) {
		if (canPrint(obj)) {
			printElem(obj);
		}
		return this;
	}

	/**
	 * Print string representation of all objects as element.
	 * The elements are surrounded by the defined markers.
	 */
	@SuppressWarnings("unchecked")
	public <T> StringPrinter addAll(T... args) {
		for (Object arg : args) {
			add(arg);
		}
		return this;
	}

	/**
	 * Print string representation of all objects as element if valid for printing.
	 * Validity is determined by {@link #canPrint}.
	 * If printed, the element is surrounded by the defined markers.
	 */
	@SuppressWarnings("unchecked")
	public <T> StringPrinter addAllIf(T... args) {
		for (Object arg : args) {
			addIf(arg);
		}
		return this;
	}

	/**
	 * Print string representation of all objects as element.
	 * The elements are surrounded by the defined markers.
	 */
	public StringPrinter addAll(Iterable<?> args) {
		for (Object arg : args) {
			add(arg);
		}
		return this;
	}

	/**
	 * Print string representation of all objects which are valid for printing.
	 * Invalid objects are skipped with the correct markers being printed.
	 * Validity is determined by {@link #canPrint}.
	 */
	public StringPrinter addAllIf(Iterable<?> args) {
		for (Object arg : args) {
			addIf(arg);
		}
		return this;
	}

	/**
	 * Print string representation of all objects as element.
	 * The elements are surrounded by the defined markers.
	 * <p>
	 * This method can also be used if the entries of a collection must be formatted in a different way than using toString().
	 * Example: addAll(data.stream().map(this::printInfo));
	 */
	public StringPrinter addAll(Stream<?> stream) {
		stream.forEach(this::add);
		return this;
	}

	/**
	 * Print string representation of all objects which are valid for printing.
	 * Invalid objects are skipped with the correct markers being printed.
	 * Validity is determined by {@link #canPrint}.
	 * <p>
	 * This method can also be used if the entries of a collection must be formatted in a different way than using toString().
	 * Example: addAllIf(data.stream().map(this::printInfo));
	 */
	public StringPrinter addAllIf(Stream<?> stream) {
		stream.forEach(this::addIf);
		return this;
	}

	// Parts

	/**
	 * Print string representation of all parts as single element.
	 * The parts are be separated by part markers, where as the element itself is surrounded by element markers.
	 */
	@SuppressWarnings("unchecked")
	public <T> StringPrinter addParts(T... args) {
		// faster variant of "return doAddPartsAll(Iterate.of(args).iterator())"
		begin();
		for (int i = 0; i < args.length; i++) {
			if (i > 0) {
				doPrint(config.partMarker);
			}
			printString(args[i]);
		}
		return this;
	}

	/**
	 * Print string representation of all parts as single element if all parts are valid.
	 * Validity of each part is determined by {@link #canPrint}, if one part is invalid the whole element is skipped.
	 * If printed, the parts are be separated by part markers, where as the element itself is surrounded by element markers.
	 */
	public StringPrinter addPartsIf(Object... args) {
		if (!canPrint(args)) {
			return this;
		}
		return addParts(args);
	}

	public StringPrinter addPartsIfCond(Object cond, Object... args) {
		if (!isTrue(cond)) {
			return this;
		}
		return addParts(args);
	}

	/**
	 * Print string representation of all parts as single element.
	 * The parts are be separated by part markers, where as the element itself is surrounded by element markers.
	 */
	public StringPrinter addParts(Iterable<?> iterable) {
		doAddParts(iterable.iterator(), false);
		return this;
	}

	public StringPrinter addPartsIfCond(Object cond, Iterable<?> iterable) {
		if (!isTrue(cond)) {
			return this;
		}
		return addParts(iterable);
	}

	/**
	 * Print string representation of all parts as single element.
	 * The parts are be separated by part markers, where as the element itself is surrounded by element markers.
	 * <p>
	 * This method can also be used if the entries of a collection must be formatted in a different way than using toString().
	 * Example: addPartsAll(data.stream().map(this::printInfo));
	 */
	public StringPrinter addParts(Stream<?> stream) {
		doAddParts(stream.iterator(), false);
		return this;
	}

	public StringPrinter addPartsIfCond(Object cond, Stream<?> stream) {
		if (!isTrue(cond)) {
			return this;
		}
		return addParts(stream);
	}

	/**
	 * Print string representation of all parts as single element if all parts are valid.
	 * Validity of each part is determined by {@link #canPrint}, if one part is invalid the whole element is skipped.
	 * If printed, the parts are be separated by part markers, where as the element itself is surrounded by element markers.
	 */
	public StringPrinter addPartsIf(Iterable<?> iterable) {
		doAddPartsIf(iterable.iterator());
		return this;
	}

	/**
	 * Print string representation of all parts as single element if all parts are valid.
	 * Validity of each part is determined by {@link #canPrint}, if one part is invalid the whole element is skipped.
	 * If printed, the parts are be separated by part markers, where as the element itself is surrounded by element markers.
	 * <p>
	 * This method can also be used if the entries of a collection must be formatted in a different way than using toString().
	 * Example: addAllIf(data.stream().map(this::printInfo));
	 */
	public StringPrinter addPartsIf(Stream<?> stream) {
		doAddPartsIf(stream.iterator());
		return this;
	}

	void doAddPartsIf(Iterator<?> iter) {
		int oldLen = buf.length();
		boolean oldBeginDone = beginDone;
		boolean oldStartLine = startLine;

		if (!doAddParts(iter, true)) {
			buf.setLength(oldLen);
			beginDone = oldBeginDone;
			startLine = oldStartLine;
		}
	}

	boolean doAddParts(Iterator<?> iter, boolean testCanPrint) {
		begin();
		boolean first = true;
		while (iter.hasNext()) {
			if (first) {
				first = false;
			} else {
				doPrint(config.partMarker);
			}
			Object obj = iter.next();
			if (testCanPrint && !canPrint(obj)) {
				return false;
			}
			printString(obj);
		}
		return true;
	}

	//

	void begin() {
		if (beginDone) {
			doPrintMarker(config.elemMarker, true);
		} else {
			doPrintMarker(config.beginMarker, false);
			beginDone = true;
		}
	}

	void end() {
		if (!beginDone && config.printMarkersAlways) {
			begin();
		}
		if (beginDone) {
			if (!endDone) {
				doPrintMarker(config.endMarker, false);
				endDone = true;
			}
		}
	}

	void doPrintMarker(String marker, boolean allowSkip) {
		if (!(skipMarker && allowSkip)) {
			doPrint(marker);
		}
		skipMarker = false;
	}

	String format(String format, Object... args) {
		return StringFormatter.format(format, args);
	}

	//

	public StringPrinter printIfCond(boolean cond, Object obj) {
		if (cond) {
			print(obj);
		}
		return this;
	}

	public StringPrinter printIfCond(boolean cond, String format, Object... args) {
		if (cond) {
			print(format, args);
		}
		return this;
	}

	public StringPrinter printlnIfCond(boolean cond, Object obj) {
		if (cond) {
			println(obj);
		}
		return this;
	}

	public StringPrinter printlnIfCond(boolean cond, String format, Object... args) {
		if (cond) {
			println(format, args);
		}
		return this;
	}

	/**
	 * Print object to buffer.
	 * If indenting is active, the string is indented. 
	 */
	public StringPrinter print(Object obj) {
		printString(obj);
		return this;
	}

	/**
	 * Print formatted message to buffer.
	 * If indenting is active, the string is indented.
	 */
	public StringPrinter print(String format, Object... args) {
		String str = format(format, args);
		return print(str);
	}

	/**
	 * Print object to buffer.
	 * Then the line is ended by printing the configured EOL string.
	 * If object is null, string "null" is printed.
	 * If indenting is active, the string is indented. 
	 */
	public StringPrinter println(Object obj) {
		print(obj);
		doEndLine();
		return this;
	}

	/**
	 * Print formatted message to buffer.
	 * Then the line is ended by printing the configured EOL string.
	 * If indenting is active, the string is indented.
	 */
	public StringPrinter println(String format, Object... args) {
		print(format, args);
		doEndLine();
		return this;
	}

	/**
	 * End line by printing the configured EOL string.
	 */
	public StringPrinter println() {
		doEndLine();
		return this;
	}

	// Printing internals

	protected void printElem(Object obj) {
		begin();
		printString(obj);
	}

	protected void printString(Object obj) {
		String str = format(obj);
		doPrint(str);
	}

	protected String format(Object obj) {
		if (obj == null) {
			return config.nullString;
		} else if (config.formatter == null) {
			return PrintTools.toString(obj, config.nullString);
		} else {
			return config.formatter.apply(obj);
		}
	}

	/**
	 * Print string. If input is null, nothing is printed.
	 */
	protected void doPrint(String str) {
		if (str == null) {
			return;
		}

		if (indentLevel == 0) {
			buf.append(str);
			startLine = false;
			return;
		}

		if (!config.detectNewLines) {
			doPrintToLine(str);
		} else {
			for (TextTools.LineIterator iter = new TextTools.LineIterator(str); iter.hasNext();) {
				doPrintToLine(iter.next());
				if (iter.hasEol()) {
					startLine = true;
				}
			}
		}
	}

	void doPrintToLine(String str) {
		if (startLine) {
			buf.append(currentIndent);
			startLine = false;
		}
		buf.append(str);
	}

	void doEndLine() {
		buf.append(config.eol);
		startLine = true;
		beginDone = false;
	}

	boolean canPrint(Object... args) {
		if (args.length == 0) {
			return false;
		}
		for (Object arg : args) {
			if (!canPrint(arg)) {
				return false;
			}
		}
		return true;
	}

	boolean canPrint(Iterator<?> iter) {
		boolean canPrint = false;
		while (iter.hasNext()) {
			if (!canPrint(iter.next())) {
				return false;
			}
			canPrint = true;
		}
		return canPrint;
	}

	/**
	 * Determine whether object is valid for printing for methods supporting conditional printing.
	 * 
	 * @param obj	object to check
	 * @return		true if object should be added, false otherwise
	 */
	boolean canPrint(Object obj) {
		if (config.printable == null) {
			return obj != null;
		} else {
			return config.printable.test(obj);
		}
	}

	/**
	 * End current line.
	 */
	public StringPrinter endLine() {
		if (!startLine) {
			doEndLine();
		}
		return this;
	}

	// Config

	public StringPrinter nestConfig(boolean end) {
		if (end) {
			endMarker();
		}

		if (configStack == null) {
			configStack = GapList.create();
		}
		Config newConfig;
		if (configLevel < configStack.size()) {
			newConfig = configStack.get(configLevel);
		} else {
			newConfig = new Config();
			configStack.addLast(newConfig);
		}
		newConfig.assign(config);
		configLevel++;
		return this;
	}

	public StringPrinter unnestConfig(boolean end) {
		CheckTools.check(configLevel > 0);

		if (end) {
			endMarker();
		}

		configLevel--;
		Config newConfig = configStack.removeLast();
		config = newConfig;
		return this;
	}

	// Indent

	/**
	 * Execute runnable with nested indentation.
	 */
	public StringPrinter withIndent(Runnable runnable) {
		try {
			indent();
			runnable.run();
		} finally {
			unindent();
		}
		return this;
	}

	/**
	 * End current line and increase indentation level for next line.
	 */
	public StringPrinter indent() {
		return indent(true);
	}

	public StringPrinter indent(boolean endLine) {
		if (endLine) {
			endLine();
		}

		if (indentStack == null) {
			indentStack = GapList.create();
		}
		if (indentLevel < indentStack.size()) {
			currentIndent = indentStack.get(indentLevel);
		} else {
			currentIndent += config.indent;
			indentStack.add(currentIndent);
		}
		indentLevel++;
		return this;
	}

	/**
	 * End current line and decrease indentation level for next line.
	 */
	public StringPrinter unindent() {
		return unindent(1);
	}

	public StringPrinter unindent(boolean endLine) {
		return unindent(1, endLine);
	}

	/**
	 * End current line and decrease indentation level by the specified offset for next line.
	 * If you want to decrease the indentation level by default of 1, use {@link #unindent}.
	 * 
	 * @param offset	offset to decrease indentation level (typically 1)
	 */
	public StringPrinter unindent(int offset) {
		return unindent(offset, true);
	}

	public StringPrinter unindent(int offset, boolean endLine) {
		CheckTools.check(offset >= 0);
		CheckTools.check(indentLevel >= offset);
		if (endLine) {
			endLine();
		}
		indentLevel -= offset;
		if (indentLevel == 0) {
			currentIndent = NO_INDENT;
		} else {
			currentIndent = indentStack.get(indentLevel - 1);
		}
		return this;
	}

	//

	/**
	 * Returns true if no content has been added to the buffer.
	 * Note that toString() can return an empty string even if the buffer contains some content,
	 * e.g. if the option to remove trailing EOL characters is active.
	 */
	public boolean isEmpty() {
		end();
		return buf.length() == 0;
	}

	/**
	 * Return string collected in buffer.
	 */
	@Override
	public String toString() {
		end();
		String str = buf.toString();
		if (config.removeTrailingEol) {
			str = TextTools.removeEol(str);
		}
		return str;
	}
}