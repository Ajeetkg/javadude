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
package com.javadude.antxr.preprocessor;

import java.io.IOException;

import com.javadude.antxr.CodeGenerator;
import com.javadude.antxr.Tool;
import com.javadude.antxr.collections.impl.IndexedVector;

class Grammar {
    protected String name;
    protected String fileName;		// where does it come from?
    protected String superGrammar;	// null if no super class
    protected String type;				// lexer? parser? tree parser?
    protected IndexedVector<Rule> rules;	// text of rules as they were read in
    protected IndexedVector<Option> options;// rule options
    protected String tokenSection;	// the tokens{} stuff
    protected String preambleAction;// action right before grammar
    protected String memberAction;	// action inside grammar
    protected Hierarchy hier;			// hierarchy of grammars
    protected boolean predefined = false;	// one of the predefined grammars?
    protected boolean alreadyExpanded = false;
    protected boolean specifiedVocabulary = false;	// found importVocab option?

    /** if not derived from another grammar, might still specify a non-ANTXR
     *  class to derive from like this "class T extends Parser(MyParserClass);"
     */
    protected String superClass = null;

    protected String importVocab = null;
    protected String exportVocab = null;
    protected Tool antxrTool;

    public Grammar(Tool tool, String name, String superGrammar, IndexedVector<Rule> rules) {
        this.name = name;
        this.superGrammar = superGrammar;
        this.rules = rules;
        this.antxrTool = tool;
    }

    public void addOption(Option o) {
        if (options == null) {	// if not already there, create it
            options = new IndexedVector<Option>();
        }
        options.appendElement(o.getName(), o);
    }

    public void addRule(Rule r) {
        rules.appendElement(r.getName(), r);
    }

    /** Copy all nonoverridden rules, vocabulary, and options into this grammar from
     *  supergrammar chain.  The change is made in place; e.g., this grammar's vector
     *  of rules gets bigger.  This has side-effects: all grammars on path to
     *  root of hierarchy are expanded also.
     */
    public void expandInPlace() {
        // if this grammar already expanded, just return
        if (alreadyExpanded) {
            return;
        }

        // Expand super grammar first (unless it's a predefined or subgrammar of predefined)
        Grammar superG = getSuperGrammar();
        if (superG == null) {
            return; // error (didn't provide superclass)
        }
        if (exportVocab == null) {
            // if no exportVocab for this grammar, make it same as grammar name
            exportVocab = getName();
        }
        if (superG.isPredefined()) {
            return; // can't expand Lexer, Parser, ...
        }
        superG.expandInPlace();

        // expand current grammar now.
        alreadyExpanded = true;
        // track whether a grammar file needed to have a grammar expanded
        GrammarFile gf = hier.getFile(getFileName());
        gf.setExpanded(true);

        // Copy rules from supergrammar into this grammar
        IndexedVector<Rule> inhRules = superG.getRules();
        for (Rule r : inhRules) {
            inherit(r, superG);
        }

        // Copy options from supergrammar into this grammar
        // Modify tokdef options so that they point to dir of enclosing grammar
        IndexedVector<Option> inhOptions = superG.getOptions();
        if (inhOptions != null) {
            for (Option o : inhOptions) {
                inherit(o, superG);
            }
        }

        // add an option to load the superGrammar's output vocab
        if ((options != null && options.getElement("importVocab") == null) || options == null) {
            // no importVocab found, add one that grabs superG's output vocab
            Option inputV = new Option("importVocab", superG.exportVocab + ";", this);
            addOption(inputV);
            // copy output vocab file to current dir
            String originatingGrFileName = superG.getFileName();
            String path = antxrTool.pathToFile(originatingGrFileName);
            String superExportVocabFileName = path + superG.exportVocab +
                CodeGenerator.TokenTypesFileSuffix +
                CodeGenerator.TokenTypesFileExt;
            String newImportVocabFileName = antxrTool.fileMinusPath(superExportVocabFileName);
            if (path.equals("." + System.getProperty("file.separator"))) {
                // don't copy tokdef file onto itself (must be current directory)
                // System.out.println("importVocab file same dir; leaving as " + superExportVocabFileName);
            }
            else {
                try {
                    antxrTool.copyFile(superExportVocabFileName, newImportVocabFileName);
                }
                catch (IOException io) {
                    antxrTool.toolError("cannot find/copy importVocab file " + superExportVocabFileName);
                    return;
                }
            }
        }

        // copy member action from supergrammar into this grammar
        inherit(superG.memberAction, superG);
    }

    public String getFileName() {
        return fileName;
    }

    public String getName() {
        return name;
    }

    public IndexedVector<Option> getOptions() {
        return options;
    }

    public IndexedVector<Rule> getRules() {
        return rules;
    }

    public Grammar getSuperGrammar() {
        if (superGrammar == null) {
            return null;
        }
        Grammar g = hier.getGrammar(superGrammar);
        return g;
    }

    public String getSuperGrammarName() {
        return superGrammar;
    }

    public String getType() {
        return type;
    }

    public void inherit(Option o, Grammar superG) {
        // do NOT inherit importVocab/exportVocab options under any circumstances
        if (o.getName().equals("importVocab") ||
            o.getName().equals("exportVocab")) {
            return;
        }

        Option overriddenOption = null;
        if (options != null) {	// do we even have options?
            overriddenOption = (Option)options.getElement(o.getName());
        }
        // if overridden, do not add to this grammar
        if (overriddenOption == null) { // not overridden
            addOption(o);	// copy option into this grammar--not overridden
        }
    }

    public void inherit(Rule r, Grammar superG) {
        // if overridden, do not add to this grammar
        Rule overriddenRule = (Rule)rules.getElement(r.getName());
        if (overriddenRule != null) {
            // rule is overridden in this grammar.
            if (!overriddenRule.sameSignature(r)) {
                // warn if different sig
                antxrTool.warning("rule " + getName() + "." + overriddenRule.getName() +
                                   " has different signature than " +
                                   superG.getName() + "." + overriddenRule.getName());
            }
        }
        else {  // not overridden, copy rule into this
            addRule(r);
        }
    }

    public void inherit(String memberActionToInherit, Grammar superG) {
        if (this.memberAction != null) {
            return;	// do nothing if already have member action
        }
        if (memberActionToInherit != null) { // don't have one here, use supergrammar's action
            this.memberAction = memberActionToInherit;
        }
    }

    public boolean isPredefined() {
        return predefined;
    }

    public void setFileName(String f) {
        fileName = f;
    }

    public void setHierarchy(Hierarchy hier) {
        this.hier = hier;
    }

    public void setMemberAction(String a) {
        memberAction = a;
    }

    public void setOptions(IndexedVector<Option> options) {
        this.options = options;
    }

    public void setPreambleAction(String a) {
        preambleAction = a;
    }

    public void setPredefined(boolean b) {
        predefined = b;
    }

    public void setTokenSection(String tk) {
        tokenSection = tk;
    }

    public void setType(String t) {
        type = t;
    }

    @Override
    public String toString() {
        StringBuffer s = new StringBuffer(10000);
        if (preambleAction != null) {
            s.append(preambleAction);
        }
        if (superGrammar == null) {
            return "class " + name + ";";
        }
        if ( superClass!=null ) {
            // replace with specified superclass not actual grammar
            // user must make sure that the superclass derives from super grammar class
            s.append("class " + name + " extends " + superClass + ";");
        }
        else {
            s.append("class " + name + " extends " + type + ";");
        }
        s.append(
            System.getProperty("line.separator") +
            System.getProperty("line.separator"));
        if (options != null) {
            s.append(Hierarchy.optionsToString(options));
        }
        if (tokenSection != null) {
            s.append(tokenSection + "\n");
        }
        if (memberAction != null) {
            s.append(memberAction + System.getProperty("line.separator"));
        }
        for (int i = 0; i < rules.size(); i++) {
            Rule r = (Rule)rules.elementAt(i);
            if (!getName().equals(r.enclosingGrammar.getName())) {
                s.append("// inherited from grammar " + r.enclosingGrammar.getName() + System.getProperty("line.separator"));
            }
            s.append(r +
                System.getProperty("line.separator") +
                System.getProperty("line.separator"));
        }
        return s.toString();
    }
}