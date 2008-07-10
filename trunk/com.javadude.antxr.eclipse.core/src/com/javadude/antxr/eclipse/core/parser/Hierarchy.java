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
 *******************************************************************************/
package com.javadude.antxr.eclipse.core.parser;

import java.util.Enumeration;
import java.util.Vector;

import com.javadude.antxr.Token;

/**
 * Represents superclass for the grammar
 */
public class Hierarchy extends AbstractModel {

    private Block fHeader = null;
    private Block fOptions = null;
    private Vector<IModel> fGrammars = new Vector<IModel>();
    private Exception fException = null;

    /**
     * Create the hiearchy
     * @param aName the name
     */
    public Hierarchy(String aName) {
        super(aName, null);
        setStartLine(1);
    }

    /** {@inheritDoc} */
    public boolean hasChildren() {
        return fHeader != null || fOptions != null || !fGrammars.isEmpty();
    }

    /** {@inheritDoc} */
    public Object[] getChildren() {
        Vector<IModel> children = new Vector<IModel>();
        if (fHeader != null) {
            children.add(fHeader);
        }
        if (fOptions != null) {
            children.add(fOptions);
        }
        children.addAll(fGrammars);
        return children.toArray();
    }

    /** {@inheritDoc} */
    public String getUniqueID() {
        return getName();
    }

    /** {@inheritDoc} */
    public boolean accept(ISegmentVisitor aVisitor) {
        boolean more = true;

        // At first visit all grammars of this hierarchy
        Enumeration grammars = fGrammars.elements();
        while (grammars.hasMoreElements() && more) {
            more = ((ISegment)grammars.nextElement()).accept(aVisitor);
        }

        // Now visit this hierarchy's header and option segment
        if (more && fHeader != null) {
            more = aVisitor.visit(fHeader);
        }
        if (more && fOptions != null) {
            more = aVisitor.visit(fOptions);
        }
        return more;
    }

    /**
     * set the file header
     * @param aToken the file header
     */
    public void setHeader(Token aToken) {
        fHeader = new Block(this, Block.HEADER, aToken.getLine(),
                            aToken.getColumn());
    }

    /**
     * get the file header
     * @return the file header
     */
    public Block getHeader() {
        return fHeader;
    }

    /**
     * set the file options
     * @param aToken the file options
     */
    public void setOptions(Token aToken) {
        fOptions = new Block(this, Block.OPTIONS, aToken.getLine(),
                             aToken.getColumn());
    }

    /**
     * get options for the file
     * @return the file options
     */
    public Block getOptions() {
        return fOptions;
    }

    /**
     * add a grammar for the file
     * @param aGrammar the grammar to add
     */
    public void addGrammar(Grammar aGrammar) {
        fGrammars.add(aGrammar);
    }

    /**
     * get all grammars in the file
     * @return the grammars
     */
    public Enumeration getGrammars() {
        return fGrammars.elements();
    }

    /**
     * get the last grammar in the file
     * @return the last grammar
     */
    public Grammar getLastGrammar() {
        return (fGrammars.isEmpty() ? null :
                                       (Grammar)fGrammars.lastElement());
    }

    /**
     * Set the exception
     * @param anException the exception
     */
    public void setException(Exception anException) {
        fException = anException;
    }

    /**
     * get the exception
     * @return the exception
     */
    public Exception getException() {
        return fException;
    }

    /** {@inheritDoc} */
    public String toString() {
        return getUniqueID() + " [" + getStartLine() + ":" + getEndLine() +
                "] with grammar(s) " + fGrammars;
    }
}
