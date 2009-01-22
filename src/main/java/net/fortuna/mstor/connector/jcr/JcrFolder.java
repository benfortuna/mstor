/*
 * $Id$
 *
 * Created on 22/01/2009
 *
 * Copyright (c) 2009, Ben Fortuna
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
package net.fortuna.mstor.connector.jcr;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import net.fortuna.mstor.connector.DelegateException;
import net.fortuna.mstor.connector.FolderDelegate;
import net.fortuna.mstor.connector.MessageDelegate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jcrom.AbstractJcrEntity;
import org.jcrom.JcrMappingException;
import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrProperty;

/**
 * @author Ben
 *
 */
public class JcrFolder extends AbstractJcrEntity implements FolderDelegate<JcrMessage> {

    private static final Log LOG = LogFactory.getLog(JcrFolder.class);
    
    /**
     * 
     */
    private static final long serialVersionUID = 4533514532820747829L;

    @JcrProperty private String folderName;

    @JcrProperty private Integer type;

    @JcrProperty private Long lastUid;

    @JcrProperty private Long uidValidity;

    @JcrChildNode private List<JcrFolder> folders;

    @JcrChildNode private List<JcrMessage> messages;
    
    private JcrFolder parent;
    
    private JcrConnector connector;
    
    /**
     * 
     */
    public JcrFolder() {
        this.folders = new ArrayList<JcrFolder>();
        this.messages = new ArrayList<JcrMessage>();
    }
    
    /* (non-Javadoc)
     * @see net.fortuna.mstor.connector.FolderDelegate#allocateUid(net.fortuna.mstor.connector.MessageDelegate)
     */
    public synchronized long allocateUid(MessageDelegate message) throws UnsupportedOperationException, DelegateException {
        Long uid = lastUid + 1;
        message.setUid(uid);
        lastUid = uid;
        return uid;
    }

    /* (non-Javadoc)
     * @see net.fortuna.mstor.connector.FolderDelegate#appendMessages(javax.mail.Message[])
     */
    @SuppressWarnings("unchecked")
    public void appendMessages(Message[] messages) throws MessagingException {
        for (Message message : messages) {
            try {
                JcrMessage jcrMessage = new JcrMessage();
                jcrMessage.setMessageNumber(this.messages.size() + 1);
                jcrMessage.setFlags(message.getFlags());
                jcrMessage.setHeaders(message.getAllHeaders());
                jcrMessage.setReceived(message.getReceivedDate());
                jcrMessage.setExpunged(message.isExpunged());
                jcrMessage.setMessage(message);
                this.messages.add(jcrMessage);
            }
            catch (IOException e) {
                LOG.error("Unexpected error", e);
            }
        }
    }

    /* (non-Javadoc)
     * @see net.fortuna.mstor.connector.FolderDelegate#close()
     */
    public void close() throws MessagingException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see net.fortuna.mstor.connector.FolderDelegate#create(int)
     */
    public boolean create(int type) throws MessagingException {
        this.type = type;
        if ((Folder.HOLDS_FOLDERS & type) > 0) {
            folders = new ArrayList<JcrFolder>();
        }
        if ((Folder.HOLDS_MESSAGES & type) > 0) {
            messages = new ArrayList<JcrMessage>();
        }
        try {
            if (parent != null) {
                parent.saveChanges();
            }
            else {
                connector.getSession().save();
            }
            saveChanges();
        }
        catch (RepositoryException e) {
            throw new MessagingException("Unexpected error", e);
        }
        return true;
    }

    /* (non-Javadoc)
     * @see net.fortuna.mstor.connector.FolderDelegate#delete()
     */
    public boolean delete() {
        return parent.folders.remove(this);
    }

    /* (non-Javadoc)
     * @see net.fortuna.mstor.connector.FolderDelegate#exists()
     */
    public boolean exists() {
        try {
            Node node = getNode();
            return node != null && !node.isNew();
        }
        catch (Exception e) {
            LOG.error("Unexpected error", e);
        }
        return false;
    }

    /* (non-Javadoc)
     * @see net.fortuna.mstor.connector.FolderDelegate#expunge(javax.mail.Message[])
     */
    public void expunge(Message[] deleted) throws MessagingException {
        for (JcrMessage jcrMessage : messages) {
            for (Message message : deleted) {
                if (jcrMessage.getMessageNumber() == message.getMessageNumber()) {
                    jcrMessage.setExpunged(true);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see net.fortuna.mstor.connector.FolderDelegate#getFolder(java.lang.String)
     */
    public FolderDelegate<JcrMessage> getFolder(String name) throws MessagingException {
        JcrFolder retVal = null;
        for (JcrFolder folder : folders) {
            if (folder.getName().equals(name)) {
                retVal = folder;
                break;
            }
        }
        
        if (retVal == null) {
            retVal = new JcrFolder();
            retVal.folderName = name;
            retVal.setName(name);
            retVal.setParent(this);
            folders.add(retVal);
        }
        retVal.setConnector(connector);
        return retVal;
    }
    
    /* (non-Javadoc)
     * @see net.fortuna.mstor.connector.FolderDelegate#getFullName()
     */
    public String getFullName() {
        return connector.getJcrom().getPath(this);
    }

    /* (non-Javadoc)
     * @see net.fortuna.mstor.connector.FolderDelegate#getLastModified()
     */
    public long getLastModified() throws UnsupportedOperationException {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see net.fortuna.mstor.connector.FolderDelegate#getLastUid()
     */
    public long getLastUid() throws UnsupportedOperationException {
        return lastUid;
    }

    /* (non-Javadoc)
     * @see net.fortuna.mstor.connector.FolderDelegate#getMessage(int)
     */
    public JcrMessage getMessage(int messageNumber) throws DelegateException {
        for (JcrMessage message : messages) {
            if (message.getMessageNumber() == messageNumber) {
                return message;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see net.fortuna.mstor.connector.FolderDelegate#getMessageAsStream(int)
     */
    public InputStream getMessageAsStream(int index) throws IOException {
        return messages.get(index).getMessageAsStream();
    }

    /* (non-Javadoc)
     * @see net.fortuna.mstor.connector.FolderDelegate#getMessageCount()
     */
    public int getMessageCount() throws MessagingException {
        return messages.size();
    }

    /* (non-Javadoc)
     * @see net.fortuna.mstor.connector.FolderDelegate#getParent()
     */
    public FolderDelegate<JcrMessage> getParent() {
        return parent;
    }

    /* (non-Javadoc)
     * @see net.fortuna.mstor.connector.FolderDelegate#getSeparator()
     */
    public char getSeparator() {
        return '/';
    }

    /* (non-Javadoc)
     * @see net.fortuna.mstor.connector.FolderDelegate#getType()
     */
    public int getType() {
        return type;
    }

    /* (non-Javadoc)
     * @see net.fortuna.mstor.connector.FolderDelegate#getUidValidity()
     */
    public long getUidValidity() throws UnsupportedOperationException, MessagingException {
        return uidValidity;
    }

    /* (non-Javadoc)
     * @see net.fortuna.mstor.connector.FolderDelegate#list(java.lang.String)
     */
    public FolderDelegate<JcrMessage>[] list(String pattern) {
        return folders.toArray(new JcrFolder[folders.size()]);
    }

    /* (non-Javadoc)
     * @see net.fortuna.mstor.connector.FolderDelegate#open(int)
     */
    public void open(int mode) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see net.fortuna.mstor.connector.FolderDelegate#renameTo(java.lang.String)
     */
    public boolean renameTo(String name) {
        this.folderName = name;
        return true;
    }

    /**
     * @return the folderName
     */
    public final String getFolderName() {
        return folderName;
    }

    /**
     * @return
     * @throws PathNotFoundException
     * @throws JcrMappingException
     * @throws RepositoryException
     */
    private Node getNode() throws PathNotFoundException, JcrMappingException, RepositoryException {
        String path = connector.getJcrom().getPath(this);
        if (path != null) {
            return connector.getSession().getRootNode().getNode(path.substring(1));
        }
        return null;
    }

    /**
     * @param connector the connector to set
     */
    void setConnector(JcrConnector connector) {
        this.connector = connector;
    }
    
    /**
     * @throws RepositoryException 
     * @throws PathNotFoundException 
     * @throws JcrMappingException 
     * 
     */
    private void saveChanges() throws JcrMappingException, PathNotFoundException, RepositoryException {
        connector.getJcrom().updateNode(getNode(), this);
        getNode().save();
    }

    /**
     * @param parent the parent to set
     */
    void setParent(JcrFolder parent) {
        this.parent = parent;
    }
}