/*******************************************************************************
 *  Copyright 2008 Scott Stanchfield.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * Contributors:
 *   Based on the ANTLR parser generator by Terence Parr, http://antlr.org
 *   Ric Klaren <klaren@cs.utwente.nl>
 *******************************************************************************/
package com.javadude.antxr;

import java.util.Map;

import com.javadude.antxr.collections.impl.BitSet;

public abstract class CharScanner implements TokenStream {
    static final char NO_CHAR = 0;
    public static final char EOF_CHAR = (char)-1;
    protected ANTXRStringBuffer text; // text of current token

    protected boolean saveConsumedInput = true; // does consume() save characters?
    protected Class<?> tokenObjectClass; // what kind of tokens to create?
    protected boolean caseSensitive = true;
    protected boolean caseSensitiveLiterals = true;
    protected Map<ANTXRHashString, Integer> literals; // set by subclass

    /** Tab chars are handled by tab() according to this value; override
     *  method to do anything weird with tabs.
     */
    protected int tabsize = 8;

    protected Token _returnToken = null; // used to return tokens w/o using return val.

    // Hash string used so we don't new one every time to check literals table
    protected ANTXRHashString hashString;

    protected LexerSharedInputState inputState;

    /** Used during filter mode to indicate that path is desired.
     *  A subsequent scan error will report an error as usual if
     *  acceptPath=true;
     */
    protected boolean commitToPath = false;

    /** Used to keep track of indentdepth for traceIn/Out */
    protected int traceDepth = 0;

    public CharScanner() {
        text = new ANTXRStringBuffer();
        hashString = new ANTXRHashString(this);
        setTokenObjectClass("com.javadude.antxr.CommonToken");
    }

    public CharScanner(InputBuffer cb) { // SAS: use generic buffer
        this();
        inputState = new LexerSharedInputState(cb);
    }

    public CharScanner(LexerSharedInputState sharedState) {
        this();
        inputState = sharedState;
    }

    public void append(char c) {
        if (saveConsumedInput) {
            text.append(c);
        }
    }

    public void append(String s) {
        if (saveConsumedInput) {
            text.append(s);
        }
    }

    public void commit() {
        inputState.input.commit();
    }

    public void consume() throws CharStreamException {
        if (inputState.guessing == 0) {
            char c = LA(1);
            if (caseSensitive) {
                append(c);
            }
            else {
                // use input.LA(), not LA(), to get original case
                // CharScanner.LA() would toLower it.
                append(inputState.input.LA(1));
            }
            if (c == '\t') {
                tab();
            }
            else {
                inputState.column++;
            }
        }
        inputState.input.consume();
    }

    /** Consume chars until one matches the given char */
    public void consumeUntil(int c) throws CharStreamException {
        while (LA(1) != EOF_CHAR && LA(1) != c) {
            consume();
        }
    }

    /** Consume chars until one matches the given set */
    public void consumeUntil(BitSet set) throws CharStreamException {
        while (LA(1) != EOF_CHAR && !set.member(LA(1))) {
            consume();
        }
    }

    public boolean getCaseSensitive() {
        return caseSensitive;
    }

    public final boolean getCaseSensitiveLiterals() {
        return caseSensitiveLiterals;
    }

    public int getColumn() {
        return inputState.column;
    }

    public void setColumn(int c) {
        inputState.column = c;
    }

    public boolean getCommitToPath() {
        return commitToPath;
    }

    public String getFilename() {
        return inputState.filename;
    }

    public InputBuffer getInputBuffer() {
        return inputState.input;
    }

    public LexerSharedInputState getInputState() {
        return inputState;
    }

    public void setInputState(LexerSharedInputState state) {
        inputState = state;
    }

    public int getLine() {
        return inputState.line;
    }

    /** return a copy of the current text buffer */
    public String getText() {
        return text.toString();
    }

    public Token getTokenObject() {
        return _returnToken;
    }

    public char LA(int i) throws CharStreamException {
        if (caseSensitive) {
	        return inputState.input.LA(i);
        }
        return toLower(inputState.input.LA(i));
    }

    protected Token makeToken(int t) {
        try {
            Token tok = (Token)tokenObjectClass.newInstance();
            tok.setType(t);
            tok.setColumn(inputState.tokenStartColumn);
            tok.setLine(inputState.tokenStartLine);
            // tracking real start line now: tok.setLine(inputState.line);
            return tok;
        }
        catch (InstantiationException ie) {
            panic("can't instantiate token: " + tokenObjectClass);
        }
        catch (IllegalAccessException iae) {
            panic("Token class is not accessible" + tokenObjectClass);
        }
        return Token.badToken;
    }

    public int mark() {
        return inputState.input.mark();
    }

    public void match(char c) throws MismatchedCharException, CharStreamException {
        if (LA(1) != c) {
            throw new MismatchedCharException(LA(1), c, false, this);
        }
        consume();
    }

    public void match(BitSet b) throws MismatchedCharException, CharStreamException {
        if (!b.member(LA(1))) {
	        throw new MismatchedCharException(LA(1), b, false, this);
        }
        consume();
    }

    public void match(String s) throws MismatchedCharException, CharStreamException {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            if (LA(1) != s.charAt(i)) {
                throw new MismatchedCharException(LA(1), s.charAt(i), false, this);
            }
            consume();
        }
    }

    public void matchNot(char c) throws MismatchedCharException, CharStreamException {
        if (LA(1) == c) {
            throw new MismatchedCharException(LA(1), c, true, this);
        }
        consume();
    }

    public void matchRange(char c1, char c2) throws MismatchedCharException, CharStreamException {
        if (LA(1) < c1 || LA(1) > c2) {
	        throw new MismatchedCharException(LA(1), c1, c2, false, this);
        }
        consume();
    }

    public void newline() {
        inputState.line++;
        inputState.column = 1;
    }

    /** advance the current column number by an appropriate amount
     *  according to tab size. This method is called from consume().
     */
    public void tab() {
        int c = getColumn();
        int nc = ( ((c-1)/tabsize) + 1) * tabsize + 1;  // calculate tab stop
        setColumn( nc );
    }

    public void setTabSize( int size ) {
          tabsize = size;
    }

    public int getTabSize() {
        return tabsize;
    }

    /** @see #panic(String)
     */
    public void panic() {
        System.err.println("CharScanner: panic");
        Utils.error("");
    }

    /** This method is executed by ANTXR internally when it detected an illegal
     *  state that cannot be recovered from.
     *  The default implementation of this method calls
     *  {@link java.lang.System.exit(int)} and writes directly to
     *  {@link java.lang.System.err)} , which is usually not appropriate when
     *  a translator is embedded into a larger application. <em>It is highly
     *  recommended that this method be overridden to handle the error in a
     *  way appropriate for your application (e.g. throw an unchecked
     *  exception)</em>.
     */
    public void panic(String s) {
        System.err.println("CharScanner; panic: " + s);
        Utils.error(s);
    }

    /** Parser error-reporting function can be overridden in subclass */
    public void reportError(RecognitionException ex) {
        System.err.println(ex);
    }

    /** Parser error-reporting function can be overridden in subclass */
    public void reportError(String s) {
        if (getFilename() == null) {
            System.err.println("error: " + s);
        }
        else {
            System.err.println(getFilename() + ": error: " + s);
        }
    }

    /** Parser warning-reporting function can be overridden in subclass */
    public void reportWarning(String s) {
        if (getFilename() == null) {
            System.err.println("warning: " + s);
        }
        else {
            System.err.println(getFilename() + ": warning: " + s);
        }
    }

    public void resetText() {
        text.setLength(0);
        inputState.tokenStartColumn = inputState.column;
        inputState.tokenStartLine = inputState.line;
    }

    public void rewind(int pos) {
         inputState.input.rewind(pos);
         // RK: should not be here, it is messing up column calculation
         // setColumn(inputState.tokenStartColumn);
    }

    public void setCaseSensitive(boolean t) {
        caseSensitive = t;
    }

    public void setCommitToPath(boolean commit) {
        commitToPath = commit;
    }

    public void setFilename(String f) {
        inputState.filename = f;
    }

    public void setLine(int line) {
        inputState.line = line;
    }

    public void setText(String s) {
        resetText();
        text.append(s);
    }

    public void setTokenObjectClass(String cl) {
        try {
            tokenObjectClass = Utils.loadClass(cl);
        }
        catch (ClassNotFoundException ce) {
            panic("ClassNotFoundException: " + cl);
        }
    }

    // Test the token text against the literals table
    // Override this method to perform a different literals test
    public int testLiteralsTable(int ttype) {
        hashString.setBuffer(text.getBuffer(), text.length());
        Integer literalsIndex = literals.get(hashString);
        if (literalsIndex != null) {
            ttype = literalsIndex.intValue();
        }
        return ttype;
    }

    /** Test the text passed in against the literals table
     * Override this method to perform a different literals test
     * This is used primarily when you want to test a portion of
     * a token.
     */
    public int testLiteralsTable(String testToTest, int ttype) {
        ANTXRHashString s = new ANTXRHashString(testToTest, this);
        Integer literalsIndex = literals.get(s);
        if (literalsIndex != null) {
            ttype = literalsIndex.intValue();
        }
        return ttype;
    }

    // Override this method to get more specific case handling
    public char toLower(char c) {
        return Character.toLowerCase(c);
    }

    public void traceIndent() {
        for (int i = 0; i < traceDepth; i++) {
	        System.out.print(" ");
        }
    }

    public void traceIn(String rname) throws CharStreamException {
        traceDepth += 1;
        traceIndent();
        System.out.println("> lexer " + rname + "; c==" + LA(1));
    }

    public void traceOut(String rname) throws CharStreamException {
        traceIndent();
        System.out.println("< lexer " + rname + "; c==" + LA(1));
        traceDepth -= 1;
    }

    /** This method is called by YourLexer.nextToken() when the lexer has
     *  hit EOF condition.  EOF is NOT a character.
     *  This method is not called if EOF is reached during
     *  syntactic predicate evaluation or during evaluation
     *  of normal lexical rules, which presumably would be
     *  an IOException.  This traps the "normal" EOF condition.
     *
     *  uponEOF() is called after the complete evaluation of
     *  the previous token and only if your parser asks
     *  for another token beyond that last non-EOF token.
     *
     *  You might want to throw token or char stream exceptions
     *  like: "Heh, premature eof" or a retry stream exception
     *  ("I found the end of this file, go back to referencing file").
     */
    public void uponEOF() {
        // nothing
    }
}