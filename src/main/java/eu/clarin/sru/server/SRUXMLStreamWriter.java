/**
 * This software is copyright (c) 2011 by
 *  - Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 * This is free software. You can redistribute it
 * and/or modify it under the terms described in
 * the GNU General Public License v3 of which you
 * should have received a copy. Otherwise you can download
 * it from
 *
 *   http://www.gnu.org/licenses/gpl-3.0.txt
 *
 * @copyright Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 *
 * @license http://www.gnu.org/licenses/gpl-3.0.txt
 *  GNU General Public License v3
 */
package eu.clarin.sru.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.z3950.zing.cql.CQLNode;

final class SRUXMLStreamWriter implements XMLStreamWriter {
    private static final SAXParserFactory factory;
    private final Writer writer;
    private final XMLStreamWriter xmlwriter;
    private boolean writingRecord = false;

    SRUXMLStreamWriter(OutputStream stream, XMLOutputFactory factory,
            final SRURecordPacking recordPacking) throws IOException,
            XMLStreamException {
        this.writer = new OutputStreamWriter(stream,
                SRUService.RESPONSE_ENCODING) {
            @Override
            public void write(int c) throws IOException {
                if (writingRecord &&
                        (recordPacking == SRURecordPacking.STRING)) {
                    /*
                     * NOTE: need to write single characters here, because
                     * super.write(String) will call us again, and we would
                     * create and endless loop here until stack blows up ...
                     */
                    switch (c) {
                    case '<':
                        super.write('&');
                        super.write('l');
                        super.write('t');
                        super.write(';');
                        return;
                    case '>':
                        super.write('&');
                        super.write('g');
                        super.write('t');
                        super.write(';');
                        return;
                    case '&':
                        super.write('&');
                        super.write('a');
                        super.write('m');
                        super.write('p');
                        super.write(';');
                        return;
                    default:
                        /* $FALL-THROUGH$ */
                    }
                }
                super.write(c);
            }

            @Override
            public void write(char[] c, int off, int len) throws IOException {
                if (writingRecord &&
                        (recordPacking == SRURecordPacking.STRING)) {
                    for (int i = off; i < len; i++) {
                        this.write(c[i]);
                    }
                } else {
                    super.write(c, off, len);
                }
            }

            @Override
            public void write(String s, int off, int len) throws IOException {
                if (writingRecord &&
                        (recordPacking == SRURecordPacking.STRING)) {
                    for (int i = off; i < len; i++) {
                        this.write(s.charAt(i));
                    }
                } else {
                    super.write(s, off, len);
                }
            }
        };

        /*
         * It is believed, that XMLWriterFactories, once configured,
         * are thread-save ...
         */
        this.xmlwriter = factory.createXMLStreamWriter(this.writer);
    }

    @Override
    public void writeStartElement(String localName) throws XMLStreamException {
        xmlwriter.writeStartElement(localName);
    }

    @Override
    public void writeStartElement(String namespaceURI, String localName)
            throws XMLStreamException {
        xmlwriter.writeStartElement(namespaceURI, localName);
    }

    @Override
    public void writeStartElement(String prefix, String localName,
            String namespaceURI) throws XMLStreamException {
        xmlwriter.writeStartElement(prefix, localName, namespaceURI);
    }

    @Override
    public void writeEmptyElement(String namespaceURI, String localName)
            throws XMLStreamException {
        xmlwriter.writeEmptyElement(namespaceURI, localName);
    }

    @Override
    public void writeEmptyElement(String prefix, String localName,
            String namespaceURI) throws XMLStreamException {
        xmlwriter.writeEmptyElement(prefix, localName, namespaceURI);
    }

    @Override
    public void writeEmptyElement(String localName) throws XMLStreamException {
        xmlwriter.writeEmptyElement(localName);
    }

    @Override
    public void writeEndElement() throws XMLStreamException {
        xmlwriter.writeEndElement();
    }

    @Override
    public void writeEndDocument() throws XMLStreamException {
        xmlwriter.writeEndDocument();
    }

    @Override
    public void close() throws XMLStreamException {
        xmlwriter.close();
    }

    @Override
    public void flush() throws XMLStreamException {
        xmlwriter.flush();
    }

    @Override
    public void writeAttribute(String localName, String value)
            throws XMLStreamException {
        xmlwriter.writeAttribute(localName, value);
    }

    @Override
    public void writeAttribute(String prefix, String namespaceURI,
            String localName, String value) throws XMLStreamException {
        xmlwriter.writeAttribute(prefix, namespaceURI, localName, value);
    }

    @Override
    public void writeAttribute(String namespaceURI, String localName,
            String value) throws XMLStreamException {
        xmlwriter.writeAttribute(namespaceURI, localName, value);
    }

    @Override
    public void writeNamespace(String prefix, String namespaceURI)
            throws XMLStreamException {
        xmlwriter.writeNamespace(prefix, namespaceURI);
    }

    @Override
    public void writeDefaultNamespace(String namespaceURI)
            throws XMLStreamException {
        xmlwriter.writeDefaultNamespace(namespaceURI);
    }

    @Override
    public void writeComment(String data) throws XMLStreamException {
        xmlwriter.writeComment(data);
    }

    @Override
    public void writeProcessingInstruction(String target)
            throws XMLStreamException {
        xmlwriter.writeProcessingInstruction(target);
    }

    @Override
    public void writeProcessingInstruction(String target, String data)
            throws XMLStreamException {
        xmlwriter.writeProcessingInstruction(target, data);
    }

    @Override
    public void writeCData(String data) throws XMLStreamException {
        xmlwriter.writeCData(data);
    }

    @Override
    public void writeDTD(String dtd) throws XMLStreamException {
        xmlwriter.writeDTD(dtd);
    }

    @Override
    public void writeEntityRef(String name) throws XMLStreamException {
        xmlwriter.writeEntityRef(name);
    }

    @Override
    public void writeStartDocument() throws XMLStreamException {
        xmlwriter.writeStartDocument();
    }

    @Override
    public void writeStartDocument(String version) throws XMLStreamException {
        xmlwriter.writeStartDocument(version);
    }

    @Override
    public void writeStartDocument(String encoding, String version)
            throws XMLStreamException {
        xmlwriter.writeStartDocument(encoding, version);
    }

    @Override
    public void writeCharacters(String text) throws XMLStreamException {
        xmlwriter.writeCharacters(text);
    }

    @Override
    public void writeCharacters(char[] text, int start, int len)
            throws XMLStreamException {
        xmlwriter.writeCharacters(text, start, len);
    }

    @Override
    public String getPrefix(String uri) throws XMLStreamException {
        return xmlwriter.getPrefix(uri);
    }

    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        xmlwriter.setPrefix(prefix, uri);
    }

    @Override
    public void setDefaultNamespace(String uri) throws XMLStreamException {
        xmlwriter.setDefaultNamespace(uri);
    }

    @Override
    public void setNamespaceContext(NamespaceContext context)
            throws XMLStreamException {
        xmlwriter.setNamespaceContext(context);
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return xmlwriter.getNamespaceContext();
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        return xmlwriter.getProperty(name);
    }


    public Writer getWriter() {
        return writer;
    }


    public void startRecord() throws XMLStreamException {
        if (writingRecord) {
            throw new IllegalStateException("was already writing record");
        }
        xmlwriter.flush();
        /*
         *  abuse writeCharacters to force writer to close finish
         *  any pending start or end elements
         */
        xmlwriter.writeCharacters("");
        writingRecord = true;
    }

    public void endRecord() throws XMLStreamException {
        if (!writingRecord) {
            throw new IllegalStateException("was not writing record");
        }
        /*
         *  abuse writeCharacters to force writer to close finish
         *  any pending start or end elements
         */
        xmlwriter.writeCharacters("");
        xmlwriter.flush();
        writingRecord = false;
    }


    public void writeXCQL(CQLNode query) throws XMLStreamException {
        /*
         * HACK: Parsing the XCQL to serialize is wasting resources.
         * Alternative would be to serialize to XCQL from CQLNode, but
         * I'm not yet enthusiastic on writing the serializer myself.
         */
        try {
            SAXParser parser = factory.newSAXParser();
            InputSource input =
                    new InputSource(new StringReader(query.toXCQL(0)));
            parser.parse(input, new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName,
                        String qName, Attributes attributes)
                        throws SAXException {
                    try {
                        xmlwriter.writeStartElement(qName);
                        for (int i = 0; i < attributes.getLength(); i++) {
                            xmlwriter.writeAttribute(attributes.getQName(i),
                                    attributes.getValue(i));
                        }
                    } catch (XMLStreamException e) {
                        throw new SAXException(e);
                    }
                }

                @Override
                public void endElement(String uri, String localName,
                        String qName) throws SAXException {
                    try {
                        xmlwriter.writeEndElement();
                    } catch (XMLStreamException e) {
                        throw new SAXException(e);
                    }
                }

                @Override
                public void characters(char[] text, int start, int length)
                        throws SAXException {
                    try {
                        boolean isWhitespaceOnly = true;
                        for (int i = start; i < start + length; i++) {
                            if (!Character.isWhitespace(text[i])) {
                                isWhitespaceOnly = false;
                                break;
                            }
                        }
                        if (!isWhitespaceOnly) {
                            xmlwriter.writeCharacters(text, start, length);
                        }
                    } catch (XMLStreamException e) {
                        throw new SAXException(e);
                    }
                }
            });
        } catch (Exception e) {
            throw new XMLStreamException("cannot write XCQL", e);
        }
    }


    static {
        factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setValidating(false);
        factory.setXIncludeAware(false);
    }

} // class SRUXMLStreamWriter
