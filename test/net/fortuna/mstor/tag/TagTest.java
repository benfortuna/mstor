/*
 * $Id$
 *
 * Created on 6/05/2006
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
package net.fortuna.mstor.tag;

import java.util.Iterator;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.URLName;

import net.fortuna.mstor.MStorTest;
import net.fortuna.mstor.tag.Tags;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Unit tests for Tag support.
 * @author Ben Fortuna
 */
public class TagTest extends MStorTest {
    
    private static final Log LOG = LogFactory.getLog(TagTest.class);
    
    /**
     * Default constructor.
     */
    public TagTest() {
        super(new URLName("mstor:etc/samples/Tags"));
    }
    
    /**
     * Logs a summary of all messages in the specified folder.
     * @param folder
     * @throws MessagingException
     */
    private void logMessages(final Folder folder) throws MessagingException {
        if (!folder.isOpen()) {
            folder.open(Folder.READ_ONLY);
        }
        
        for (int i = 1; i <= folder.getMessageCount(); i++) {
            Message message = folder.getMessage(i);
            LOG.info("Message [" + i + "]: " + message.getSubject());
            for (Iterator it = Tags.getTags(message).iterator(); it.hasNext();) {
                LOG.info("Tag: " + it.next());
            }
        }
    }
    
    /**
     * A unit test that tags a message.
     */
    public void testTagMessage() throws MessagingException {
        Folder inbox = store.getDefaultFolder().getFolder("Inbox");
        inbox.open(Folder.READ_WRITE);
        
        String tag = "Test 1";
        
        Message message = inbox.getMessage(1);
        Tags.addTag(tag, message);
        assertTrue(Tags.getTags(message).contains(tag));
        
        logMessages(inbox);
    }
    
    /**
     * A unit test that untags a message.
     */
    public void testUntagMessage() throws MessagingException {
        Folder inbox = store.getDefaultFolder().getFolder("Inbox");
        inbox.open(Folder.READ_WRITE);
        
        String tag = "Test 1";
        
        Message message = inbox.getMessage(1);
        assertTrue(Tags.getTags(message).contains(tag));
        Tags.removeTag(tag, message);
        assertFalse(Tags.getTags(message).contains(tag));
        
        logMessages(inbox);
    }
}