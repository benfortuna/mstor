/*
 * $Id$
 *
 * Created on 15/05/2006
 *
 * Copyright (c) 2005, Ben Fortuna
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  o Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 *  o Neither the name of Ben Fortuna nor the names of any other contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.fortuna.mstor.search;

import java.io.StringReader;
import java.io.StringWriter;

import javax.mail.search.AddressStringTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.OrTerm;

import junit.framework.TestCase;
import net.fortuna.mstor.tag.Tags;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Ben Fortuna
 */
public class SearchTermsTest extends TestCase {
    
    private static final Log LOG = LogFactory.getLog(SearchTermsTest.class);

    /**
     * Test save method.
     */
    public void testSave() {
        String tag = "test 1";
        Tags tags = new Tags();
        tags.add(tag);
        
        TagsTerm term = new TagsTerm(tags);
        AddressStringTerm aterm = new FromStringTerm("fortuna@mstor.com");
        OrTerm orterm = new OrTerm(term, aterm);

        StringWriter out = new StringWriter();
        SearchTerms.save(orterm, out);
        String xml = out.getBuffer().toString();
        
        LOG.info(xml);
        
        OrTerm decoded = (OrTerm) SearchTerms.load(new StringReader(xml));
        
        TagsTerm decodedTags = (TagsTerm) decoded.getTerms()[0];
        assertTrue(decodedTags.getTags().contains(tag));
    }
}