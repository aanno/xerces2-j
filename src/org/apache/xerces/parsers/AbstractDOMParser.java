/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xerces" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.parsers;

import org.apache.xerces.dom.DeferredDocumentImpl;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.EntityReferenceImpl;
import org.apache.xerces.dom.TextImpl;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLParserConfiguration;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

/**
 * This is the base class of all DOM parsers. It implements the XNI
 * callback methods to create the DOM tree. After a successful parse of
 * an XML document, the DOM Document object can be queried using the
 * <code>getDocument</code> method. The actual pipeline is defined in
 * parser configuration.
 *
 * @author Stubs generated by DesignDoc on Mon Sep 11 11:10:57 PDT 2000
 * @author Arnaud Le Hors, IBM
 * @author Andy Clark, IBM
 *
 * @version $Id$
 */
public abstract class AbstractDOMParser
    extends AbstractXMLDocumentParser {

    //
    // Constants
    //

    /** Feature id: create entity ref nodes. */
    protected static final String CREATE_ENTITY_REF_NODES =
        "http://apache.org/xml/features/dom/create-entity-ref-nodes";

    protected static final String NAMESPACES =
        Constants.SAX_FEATURE_PREFIX+Constants.NAMESPACES_FEATURE;

    /** Feature id: include ignorable whitespace. */
    protected static final String INCLUDE_IGNORABLE_WHITESPACE =
        "http://apache.org/xml/features/dom/include-ignorable-whitespace";

    /** Feature id: defer node expansion. */
    protected static final String DEFER_NODE_EXPANSION =
        "http://apache.org/xml/features/dom/defer-node-expansion";

    /** Property id: document class name. */
    protected static final String DOCUMENT_CLASS_NAME =
        "http://apache.org/xml/properties/dom/document-class-name";

    protected static final String DEFAULT_DOCUMENT_CLASS_NAME =
        "org.apache.xerces.dom.DocumentImpl";

    private static final boolean DEBUG_ENTITY_REF = false;

    //
    // Data
    //

    // features

    /** Create entity reference nodes. */
    protected boolean fCreateEntityRefNodes;

    /** Include ignorable whitespace. */
    protected boolean fIncludeIgnorableWhitespace;

    // dom information

    /** The document. */
    protected Document fDocument;

    /** The default Xerces document implementation, if used. */
    protected DocumentImpl fDocumentImpl;

    /** The document class name to use. */
    protected String  fDocumentClassName;

    /** Current node. */
    protected Node fCurrentNode;
    protected CDATASection fCurrentCDATASection;


    protected String               fCurrentEntityName;
    protected String               fCurrentEntityNode;

    // deferred expansion data

    protected boolean              fDeferNodeExpansion;
    protected boolean              fNamespaceAware;
    protected DeferredDocumentImpl fDeferredDocumentImpl;
    protected int                  fDocumentIndex;
    protected int                  fDocumentTypeIndex;
    protected int                  fCurrentNodeIndex;
    protected int                  fCurrentCDATASectionIndex;

    // state

    /** True if inside document. */
    protected boolean fInDocument;

    /** True if inside CDATA section. */
    protected boolean fInCDATASection;

    // data

    /** Attribute QName. */
    private QName fAttrQName = new QName();

    //
    // Constructors
    //

    /** Default constructor. */
    protected AbstractDOMParser(XMLParserConfiguration config) {
        super(config);

        // add recognized features
        final String[] recognizedFeatures = {
            CREATE_ENTITY_REF_NODES,
            INCLUDE_IGNORABLE_WHITESPACE,
            DEFER_NODE_EXPANSION
        };
        fConfiguration.addRecognizedFeatures(recognizedFeatures);

        // set default values
        fConfiguration.setFeature(CREATE_ENTITY_REF_NODES, true);
        fConfiguration.setFeature(INCLUDE_IGNORABLE_WHITESPACE, true);
        fConfiguration.setFeature(DEFER_NODE_EXPANSION, true);

        // add recognized properties
        final String[] recognizedProperties = {
            DOCUMENT_CLASS_NAME
        };
        fConfiguration.addRecognizedProperties(recognizedProperties);

        // set default values
        fConfiguration.setProperty(DOCUMENT_CLASS_NAME,
                                   DEFAULT_DOCUMENT_CLASS_NAME);

    } // <init>(XMLParserConfiguration)

    /**
     * This method allows the programmer to decide which document
     * factory to use when constructing the DOM tree. However, doing
     * so will lose the functionality of the default factory. Also,
     * a document class other than the default will lose the ability
     * to defer node expansion on the DOM tree produced.
     *
     * @param documentClassName The fully qualified class name of the
     *                      document factory to use when constructing
     *                      the DOM tree.
     *
     * @see #getDocumentClassName
     * @see #setDeferNodeExpansion
     * @see #DEFAULT_DOCUMENT_CLASS_NAME
     */
    protected void setDocumentClassName(String documentClassName) {

        // normalize class name
        if (documentClassName == null) {
            documentClassName = DEFAULT_DOCUMENT_CLASS_NAME;
        }

        // verify that this class exists and is of the right type
        try {
            Class _class = Class.forName(documentClassName);
            //if (!_class.isAssignableFrom(Document.class)) {
            if (!Document.class.isAssignableFrom(_class)) {
                // REVISIT: message
                throw new IllegalArgumentException("PAR002 Class, \"" +
                                                   documentClassName +
                                 "\", is not of type org.w3c.dom.Document.\n" +
                                                   documentClassName);
            }
        }
        catch (ClassNotFoundException e) {
            // REVISIT: message
            throw new IllegalArgumentException("PAR003 Class, \"" +
                                               documentClassName +
                                               "\", not found.\n" +
                                               documentClassName);
        }

        // set document class name
        fDocumentClassName = documentClassName;
        if (!documentClassName.equals(DEFAULT_DOCUMENT_CLASS_NAME)) {
            fDeferNodeExpansion = false;
        }

    } // setDocumentClassName(String)

    //
    // Public methods
    //

    /** Returns the DOM document object. */
    public Document getDocument() {
        return fDocument;
    } // getDocument():Document

    //
    // XMLDocumentParser methods
    //

    /**
     * Resets the parser state.
     *
     * @throws SAXException Thrown on initialization error.
     */
    public void reset() throws XNIException {
        super.reset();

        // get feature state
        fCreateEntityRefNodes =
            fConfiguration.getFeature(CREATE_ENTITY_REF_NODES);

        fIncludeIgnorableWhitespace =
            fConfiguration.getFeature(INCLUDE_IGNORABLE_WHITESPACE);

        fDeferNodeExpansion =
            fConfiguration.getFeature(DEFER_NODE_EXPANSION);

        fNamespaceAware = fConfiguration.getFeature(NAMESPACES);

        // get property
        setDocumentClassName((String)
                             fConfiguration.getProperty(DOCUMENT_CLASS_NAME));

        // reset dom information
        fDocument = null;
        fDocumentImpl = null;
        fDeferredDocumentImpl = null;
        fCurrentNode = null;

        // reset state information
        fInDocument = false;
        fInDTD = false;
        fInCDATASection = false;
        fCurrentCDATASection = null;
        fCurrentCDATASectionIndex = -1;

    } // reset()

    //
    // XMLDocumentHandler methods
    //

    /**
     * This method notifies of the start of an entity. The DTD has the
     * pseudo-name of "[dtd]; parameter entity names start with '%'; and
     * general entity names are just the entity name.
     * <p>
     * <strong>Note:</strong> Since the DTD is an entity, the handler
     * will be notified of the start of the DTD entity by calling the
     * startEntity method with the entity name "[dtd]" <em>before</em> calling
     * the startDTD method.
     * <p>
     * <strong>Note:</strong> This method is not called for entity references
     * appearing as part of attribute values.
     *
     * @param name     The name of the entity.
     * @param publicId The public identifier of the entity if the entity
     *                 is external, null otherwise.
     * @param systemId The system identifier of the entity if the entity
     *                 is external, null otherwise.
     * @param encoding The auto-detected IANA encoding name of the entity
     *                 stream. This value will be null in those situations
     *                 where the entity encoding is not auto-detected (e.g.
     *                 internal parameter entities).
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void startEntity(String name, String publicId, String systemId,
                            String baseSystemId,
                            String encoding) throws XNIException {

        if (fInDocument && !fInDTD && fCreateEntityRefNodes) {
            if (!fDeferNodeExpansion) {
                EntityReference er = fDocument.createEntityReference(name);
                fCurrentNode.appendChild(er);
                fCurrentNode = er;
            }
            else {
                int er =
                    fDeferredDocumentImpl.createDeferredEntityReference(name);
                fDeferredDocumentImpl.appendChild(fCurrentNodeIndex, er);
                fCurrentNodeIndex = er;
            }
        }

    } // startEntity(String,String,String,String)

    /**
     * A comment.
     *
     * @param text The text in the comment.
     *
     * @throws XNIException Thrown by application to signal an error.
     */
    public void comment(XMLString text) throws XNIException {

        if (!fDeferNodeExpansion) {
            Comment comment = fDocument.createComment(text.toString());
            fCurrentNode.appendChild(comment);
        }
        else {
            int comment =
                fDeferredDocumentImpl.createDeferredComment(text.toString());
            fDeferredDocumentImpl.appendChild(fCurrentNodeIndex, comment);
        }

    } // comment(XMLString)

    /**
     * A processing instruction. Processing instructions consist of a
     * target name and, optionally, text data. The data is only meaningful
     * to the application.
     * <p>
     * Typically, a processing instruction's data will contain a series
     * of pseudo-attributes. These pseudo-attributes follow the form of
     * element attributes but are <strong>not</strong> parsed or presented
     * to the application as anything other than text. The application is
     * responsible for parsing the data.
     *
     * @param target The target.
     * @param data   The data or null if none specified.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void processingInstruction(String target, XMLString data)
        throws XNIException {

        if (!fDeferNodeExpansion) {
            ProcessingInstruction pi =
                fDocument.createProcessingInstruction(target, data.toString());
            fCurrentNode.appendChild(pi);
        }
        else {
            int pi = fDeferredDocumentImpl.
                createDeferredProcessingInstruction(target, data.toString());
            fDeferredDocumentImpl.appendChild(fCurrentNodeIndex, pi);
        }

    } // processingInstruction(String,XMLString)

    /**
     * The start of the document.
     *
     * @param systemId The system identifier of the entity if the entity
     *                 is external, null otherwise.
     * @param encoding The auto-detected IANA encoding name of the entity
     *                 stream. This value will be null in those situations
     *                 where the entity encoding is not auto-detected (e.g.
     *                 internal entities or a document entity that is
     *                 parsed from a java.io.Reader).
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void startDocument(XMLLocator locator, String encoding)
        throws XNIException {

        fInDocument = true;
        if (!fDeferNodeExpansion) {
            if (fDocumentClassName.equals(DEFAULT_DOCUMENT_CLASS_NAME)) {
                fDocument = new DocumentImpl();
                fDocumentImpl = (DocumentImpl)fDocument;
                // set DOM error checking off
                fDocumentImpl.setErrorChecking(false);
            }
            else {
                // use specified document class
                try {
                    Class documentClass = Class.forName(fDocumentClassName);
                    fDocument = (Document)documentClass.newInstance();
                    // if subclass of our own class that's cool too
                    Class defaultDocClass =
                        Class.forName(DEFAULT_DOCUMENT_CLASS_NAME);
                    if (defaultDocClass.isAssignableFrom(documentClass)) {
                        fDocumentImpl = (DocumentImpl)fDocument;
                        // set DOM error checking off
                        fDocumentImpl.setErrorChecking(false);
                    }
                }
                catch (ClassNotFoundException e) {
                    // won't happen we already checked that earlier
                }
                catch (Exception e) {
                    // REVISIT: Localize this message.
                    throw new RuntimeException(
                                 "Failed to create document object of class: "
                                 + fDocumentClassName);
                }
            }
            fCurrentNode = fDocument;
        }
        else {
            fDeferredDocumentImpl = new DeferredDocumentImpl(fNamespaceAware);
            fDocument = fDeferredDocumentImpl;
            fDocumentIndex = fDeferredDocumentImpl.createDeferredDocument();
            fCurrentNodeIndex = fDocumentIndex;
        }

    } // startDocument(String,String)

    /**
     * Notifies of the presence of the DOCTYPE line in the document.
     *
     * @param rootElement The name of the root element.
     * @param publicId    The public identifier if an external DTD or null
     *                    if the external DTD is specified using SYSTEM.
     * @param systemId    The system identifier if an external DTD, null
     *                    otherwise.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void doctypeDecl(String rootElement,
                            String publicId, String systemId)
        throws XNIException {

        if (!fDeferNodeExpansion) {
            if (fDocumentImpl != null) {
                DocumentType doctype =
                    fDocumentImpl.createDocumentType(rootElement,
                                                     publicId, systemId);
                fCurrentNode.appendChild(doctype);
            }
        }
        else {
            int doctype = fDeferredDocumentImpl.
                createDeferredDocumentType(rootElement, publicId, systemId);
            fDeferredDocumentImpl.appendChild(fCurrentNodeIndex, doctype);
        }

    } // doctypeDecl(String,String,String)

    /**
     * The start of an element. If the document specifies the start element
     * by using an empty tag, then the startElement method will immediately
     * be followed by the endElement method, with no intervening methods.
     *
     * @param element    The name of the element.
     * @param attributes The element attributes.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void startElement(QName element, XMLAttributes attributes)
        throws XNIException {

        if (!fDeferNodeExpansion) {
            Element el;
            if (fNamespaceAware) {
                el = fDocument.createElementNS(element.uri, element.rawname);
            }
            else {
                el = fDocument.createElement(element.rawname);
            }
            int attrCount = attributes.getLength();
            for (int i = 0; i < attrCount; i++) {
                attributes.getName(i, fAttrQName);
                Attr attr;
                if (fNamespaceAware) {
                    attr = fDocument.createAttributeNS(fAttrQName.uri,
                                                   fAttrQName.rawname);
                }
                else {
                    attr = fDocument.createAttribute(fAttrQName.rawname);
                }
                String attrValue = attributes.getValue(i);
                attr.setNodeValue(attrValue);
                el.setAttributeNode(attr);
                // REVISIT: Handle entities in attribute value.
            }
            fCurrentNode.appendChild(el);
            fCurrentNode = el;
        }
        else {
            int el = fDeferredDocumentImpl.
                createDeferredElement(fNamespaceAware ?
                                      element.uri : null,
                                      element.rawname, attributes);

            fDeferredDocumentImpl.appendChild(fCurrentNodeIndex, el);
            fCurrentNodeIndex = el;
        }
    } // startElement(QName,XMLAttributes)

    /**
     * Character content.
     *
     * @param text The content.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void characters(XMLString text) throws XNIException {

        if (!fDeferNodeExpansion) {
            if (fInCDATASection) {
                if (fCurrentCDATASection == null) {
                    fCurrentCDATASection =
                        fDocument.createCDATASection(text.toString());
                    fCurrentNode.appendChild(fCurrentCDATASection);
                    fCurrentNode = fCurrentCDATASection;
                }
                else {
                    fCurrentCDATASection.appendData(text.toString());
                }
            }
            else if (!fInDTD) {
                Node child = fCurrentNode.getLastChild();
                if (child != null && child.getNodeType() == Node.TEXT_NODE) {
                    Text textNode = (Text)child;
                    textNode.appendData(text.toString());
                }
                else {
                    Text textNode = fDocument.createTextNode(text.toString());
                    fCurrentNode.appendChild(textNode);
                }
            }
        }
        else {
            // The Text and CDATASection normalization is taken care of within
            // the DOM in the deferred case.
            if (fInCDATASection) {
                if (fCurrentCDATASectionIndex == -1) {
                    int cs = fDeferredDocumentImpl.
                        createDeferredCDATASection(text.toString());

                    fDeferredDocumentImpl.appendChild(fCurrentNodeIndex, cs);
                    fCurrentCDATASectionIndex = cs;
                    fCurrentNodeIndex = cs;
                }
                else {
                    int txt = fDeferredDocumentImpl.
                        createDeferredTextNode(text.toString(), false);
                    fDeferredDocumentImpl.appendChild(fCurrentNodeIndex, txt);
                }
            } if (!fInDTD) {
                int txt = fDeferredDocumentImpl.
                    createDeferredTextNode(text.toString(), false);
                fDeferredDocumentImpl.appendChild(fCurrentNodeIndex, txt);
            }
        }
    } // characters(XMLString)

    /**
     * Ignorable whitespace. For this method to be called, the document
     * source must have some way of determining that the text containing
     * only whitespace characters should be considered ignorable. For
     * example, the validator can determine if a length of whitespace
     * characters in the document are ignorable based on the element
     * content model.
     *
     * @param text The ignorable whitespace.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void ignorableWhitespace(XMLString text) throws XNIException {

        if (!fIncludeIgnorableWhitespace) {
            return;
        }

        if (!fDeferNodeExpansion) {
            /*
              REVISIT: This doesn't make sense - CDATASections are never
              ignorable whitespace - ALH!!!

              if (fInCDATASection) {
              CDATASection cdataSection = (CDATASection)fCurrentNode;
              cdataSection.appendData(text.toString());
              return;
              }
            */

            Node child = fCurrentNode.getLastChild();
            if (child != null && child.getNodeType() == Node.TEXT_NODE) {
                Text textNode = (Text)child;
                textNode.appendData(text.toString());
            }
            else {
                Text textNode = fDocument.createTextNode(text.toString());
                if (fDocumentImpl != null) {
                    TextImpl textNodeImpl = (TextImpl)textNode;
                    textNodeImpl.setIgnorableWhitespace(true);
                }
                fCurrentNode.appendChild(textNode);
            }
        }
        else {
            // The Text normalization is taken care of within the DOM in the
            // deferred case.
            int txt = fDeferredDocumentImpl.
                createDeferredTextNode(text.toString(), true);
            fDeferredDocumentImpl.appendChild(fCurrentNodeIndex, txt);
        }

    } // ignorableWhitespace(XMLString)

    /**
     * The end of an element.
     *
     * @param element The name of the element.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void endElement(QName element) throws XNIException {

        if (!fDeferNodeExpansion) {
            fCurrentNode = fCurrentNode.getParentNode();
        }
        else {
            fCurrentNodeIndex =
                fDeferredDocumentImpl.getParentNode(fCurrentNodeIndex, false);
        }


    } // endElement(QName)

    /**
     * The end of a namespace prefix mapping. This method will only be
     * called when namespace processing is enabled.
     *
     * @param prefix The namespace prefix.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void endPrefixMapping(String prefix) throws XNIException {
    } // endPrefixMapping(String)

    /**
     * The start of a CDATA section.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void startCDATA() throws XNIException {

        fInCDATASection = true;

    } // startCDATA()

    /**
     * The end of a CDATA section.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void endCDATA() throws XNIException {

        fInCDATASection = false;
        if (!fDeferNodeExpansion) {
            fCurrentNode = fCurrentNode.getParentNode();
            fCurrentCDATASection = null;
        }
        else {
            fCurrentNodeIndex =
                fDeferredDocumentImpl.getParentNode(fCurrentNodeIndex, false);
            fCurrentCDATASectionIndex = -1;
        }

    } // endCDATA()

    /**
     * The end of the document.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void endDocument() throws XNIException {

        fInDocument = false;
        if (!fDeferNodeExpansion) {
            // set DOM error checking back on
            if (fDocumentImpl != null) {
                fDocumentImpl.setErrorChecking(true);
            }
            fCurrentNode = null;
        }
        else {
            fCurrentNodeIndex = -1;
        }

    } // endDocument()

    /**
     * This method notifies the end of an entity. The DTD has the pseudo-name
     * of "[dtd]; parameter entity names start with '%'; and general entity
     * names are just the entity name.
     * <p>
     * <strong>Note:</strong> Since the DTD is an entity, the handler
     * will be notified of the end of the DTD entity by calling the
     * endEntity method with the entity name "[dtd]" <em>after</em> calling
     * the endDTD method.
     * <p>
     * <strong>Note:</strong> This method is not called for entity references
     * appearing as part of attribute values.
     *
     * @param name The name of the entity.
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void endEntity(String name) throws XNIException {

        if (fInDocument && !fInDTD && fCreateEntityRefNodes) {
            if (!fDeferNodeExpansion) {
                fCurrentNode = fCurrentNode.getParentNode();
            }
            else {
                fCurrentNodeIndex =
                    fDeferredDocumentImpl.getParentNode(fCurrentNodeIndex,
                                                        false);
            }
        }

    } // endEntity(String)

} // class AbstractDOMParser
