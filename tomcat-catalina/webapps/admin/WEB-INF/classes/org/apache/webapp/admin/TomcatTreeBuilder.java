/*
 * $Header: /home/cvs/jakarta-tomcat-catalina/webapps/admin/WEB-INF/classes/org/apache/webapp/admin/TomcatTreeBuilder.java,v 1.9 2003/10/03 21:23:34 amyroh Exp $
 * $Revision: 1.9 $
 * $Date: 2003/10/03 21:23:34 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
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
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
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
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.webapp.admin;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.modeler.ManagedBean;
import org.apache.commons.modeler.Registry;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.MessageResources;
import javax.management.AttributeNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.QueryExp;
import javax.management.Query;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;

/**
 * <p> Implementation of TreeBuilder interface for Tomcat Tree Controller
 *     to build plugin components into the tree
 *
 * @author Jazmin Jonson
 * @author Manveen Kaur
 * @author Amy Roh
 * @version $Revision: 1.9 $ $Date: 2003/10/03 21:23:34 $
 */


public class TomcatTreeBuilder implements TreeBuilder{
    
    // This SERVER_LABEL needs to be localized
    private final static String SERVER_LABEL = "Tomcat Server";
    
    public final static String DEFAULT_DOMAIN = "Catalina";
    public final static String SERVER_TYPE = ":type=Server";
    public final static String FACTORY_TYPE = 
                        DEFAULT_DOMAIN + ":type=MBeanFactory";
    public final static String SERVICE_TYPE = ":type=Service";
    public final static String ENGINE_TYPE = ":type=Engine";
    public final static String CONNECTOR_TYPE = ":type=Connector";
    public final static String HOST_TYPE = ":type=Host";
    public final static String CONTEXT_TYPE = ":type=Context";
    public final static String DEFAULTCONTEXT_TYPE = ":type=DefaultContext";
    public final static String LOADER_TYPE = ":type=Loader";
    public final static String MANAGER_TYPE = ":type=Manager";
    public final static String LOGGER_TYPE = ":type=Logger";
    public final static String REALM_TYPE = ":type=Realm";
    public final static String VALVE_TYPE = ":type=Valve";

    public final static String WILDCARD = ",*";
    
    private static MBeanServer mBServer = null;

    public void buildTree(TreeControl treeControl,
                          ApplicationServlet servlet,
                          HttpServletRequest request) {

        try {
            mBServer = servlet.getServer();
            TreeControlNode root = treeControl.getRoot();
            MessageResources resources = (MessageResources)
            servlet.getServletContext().getAttribute(Action.MESSAGES_KEY);
            getServers(root, resources);
        } catch(Throwable t){
            t.printStackTrace(System.out);
        }

    }
    
    public static ObjectName getMBeanFactory() 
            throws MalformedObjectNameException {
        
        return new ObjectName(FACTORY_TYPE);
    }
    

    /**
     * Append nodes for all defined servers.
     *
     * @param rootNode Root node for the tree control 
     * @param resources The MessageResources for our localized messages
     *  messages
     *
     * @exception Exception if an exception occurs building the tree
     */
    public void getServers(TreeControlNode rootNode, MessageResources resources)
                        throws Exception {
        
        String domain = rootNode.getDomain();
        Iterator serverNames =
            Lists.getServers(mBServer,domain).iterator();
        while (serverNames.hasNext()) {
            String serverName = (String) serverNames.next();
            ObjectName objectName = new ObjectName(serverName);
            String nodeLabel = SERVER_LABEL;
            TreeControlNode serverNode =
                new TreeControlNode(serverName,
                                    "Server.gif",
                                    nodeLabel,
                                    "EditServer.do?select=" +
                                    URLEncoder.encode(serverName) +
                                    "&nodeLabel=" +
                                    URLEncoder.encode(nodeLabel),
                                    "content",
                                    true, domain);
            rootNode.addChild(serverNode);
            getServices(serverNode, serverName, resources);
        }
        
    }
    

    /**
     * Append nodes for all defined services for the specified server.
     *
     * @param serverNode Server node for the tree control
     * @param serverName Object name of the parent server
     * @param resources The MessageResources for our localized messages
     *  messages
     *
     * @exception Exception if an exception occurs building the tree
     */
    public void getServices(TreeControlNode serverNode, String serverName, 
                        MessageResources resources) throws Exception {

        String domain = serverNode.getDomain();
        Iterator serviceNames =
            Lists.getServices(mBServer, serverName).iterator();
        while (serviceNames.hasNext()) {
            String serviceName = (String) serviceNames.next();
            ObjectName objectName = new ObjectName(serviceName);
            String nodeLabel =
                "Service (" + objectName.getKeyProperty("serviceName") + ")";
            TreeControlNode serviceNode =
                new TreeControlNode(serviceName,
                                    "Service.gif",
                                    nodeLabel,
                                    "EditService.do?select=" +
                                    URLEncoder.encode(serviceName) +
                                    "&nodeLabel=" +
                                    URLEncoder.encode(nodeLabel),
                                    "content",
                                    false, domain);
            serverNode.addChild(serviceNode);
            getConnectors(serviceNode, serviceName);
            getDefaultContexts(serviceNode, serviceName, resources);
            getHosts(serviceNode, serviceName, resources);
            getLoggers(serviceNode, serviceName);
            getRealms(serviceNode, serviceName);
            getValves(serviceNode, serviceName);
        }

    }
    

    /**
     * Append nodes for all defined connectors for the specified service.
     *
     * @param serviceNode Service node for the tree control
     * @param serviceName Object name of the parent service
     *
     * @exception Exception if an exception occurs building the tree
     */
    public void getConnectors(TreeControlNode serviceNode, String serviceName)
                        throws Exception{
        
        String domain = serviceNode.getDomain();
        Iterator connectorNames =
            Lists.getConnectors(mBServer, serviceName).iterator();
        while (connectorNames.hasNext()) {
            String connectorName = (String) connectorNames.next();
            ObjectName objectName = new ObjectName(connectorName);
            String nodeLabel =
                "Connector (" + objectName.getKeyProperty("port") + ")";
            TreeControlNode connectorNode =
                new TreeControlNode(connectorName,
                                    "Connector.gif",
                                    nodeLabel,
                                    "EditConnector.do?select=" +
                                    URLEncoder.encode(connectorName) +
                                    "&nodeLabel=" +
                                    URLEncoder.encode(nodeLabel),
                                    "content",
                                    false, domain);
            serviceNode.addChild(connectorNode);
        }
    }
    

    /**
     * Append nodes for all defined hosts for the specified service.
     *
     * @param serviceNode Service node for the tree control
     * @param serviceName Object name of the parent service
     * @param resources The MessageResources for our localized messages
     *  messages
     *
     * @exception Exception if an exception occurs building the tree
     */
    public void getHosts(TreeControlNode serviceNode, String serviceName, 
        MessageResources resources) throws Exception {
        
        String domain = serviceNode.getDomain();
        Iterator hostNames =
            Lists.getHosts(mBServer, serviceName).iterator();
        while (hostNames.hasNext()) {
            String hostName = (String) hostNames.next();
            ObjectName objectName = new ObjectName(hostName);
            String nodeLabel =
                "Host (" + objectName.getKeyProperty("host") + ")";
            TreeControlNode hostNode =
                new TreeControlNode(hostName,
                                    "Host.gif",
                                    nodeLabel,
                                    "EditHost.do?select=" +
                                    URLEncoder.encode(hostName) +
                                    "&nodeLabel=" +
                                    URLEncoder.encode(nodeLabel),
                                    "content",
                                    false, domain);
            serviceNode.addChild(hostNode);
            getContexts(hostNode, hostName, resources);            
            getDefaultContexts(hostNode, hostName, resources);
            getLoggers(hostNode, hostName);
            getRealms(hostNode, hostName);
            getValves(hostNode, hostName);
        }

    }    

    
    /**
     * Append nodes for all defined contexts for the specified host.
     *
     * @param hostNode Host node for the tree control
     * @param hostName Object name of the parent host
     * @param resources The MessageResources for our localized messages
     *  messages
     *
     * @exception Exception if an exception occurs building the tree
     */
    public void getContexts(TreeControlNode hostNode, String hostName,
                        MessageResources resources) throws Exception {
        
        String domain = hostNode.getDomain();
        Iterator contextNames =
            Lists.getContexts(mBServer, hostName).iterator();
        while (contextNames.hasNext()) {
            String contextName = (String) contextNames.next();
            ObjectName objectName = new ObjectName(contextName);
            String name = objectName.getKeyProperty("name");
            name = name.substring(2);
            int i = name.indexOf("/");
            String path = name.substring(i);
            String nodeLabel =
                "Context (" + path + ")";
            TreeControlNode contextNode =
                new TreeControlNode(contextName,
                                    "Context.gif",
                                    nodeLabel,
                                    "EditContext.do?select=" +
                                    URLEncoder.encode(contextName) +
                                    "&nodeLabel=" +
                                    URLEncoder.encode(nodeLabel),
                                    "content",
                                    false, domain);
            hostNode.addChild(contextNode);
            getResources(contextNode, contextName, resources);
            getLoggers(contextNode, contextName);
            getRealms(contextNode, contextName);
            getValves(contextNode, contextName);
        }
    }
    
    
    /**
     * Append nodes for all defined default contexts for the specified host.
     *
     * @param hostNode Host node for the tree control
     * @param containerName Object name of the parent container
     * @param containerType The type of the parent container
     * @param resources The MessageResources for our localized messages
     *  messages
     *
     * @exception Exception if an exception occurs building the tree
     */
    public void getDefaultContexts(TreeControlNode hostNode, String containerName, 
                                    MessageResources resources) throws Exception {
        
        String domain = hostNode.getDomain();
        Iterator defaultContextNames =
            Lists.getDefaultContexts(mBServer, containerName).iterator();
        while (defaultContextNames.hasNext()) {
            String defaultContextName = (String) defaultContextNames.next();
            ObjectName objectName = new ObjectName(defaultContextName);
            String nodeLabel = "DefaultContext";
            TreeControlNode defaultContextNode =
                new TreeControlNode(defaultContextName,
                                    "DefaultContext.gif",
                                    nodeLabel,
                                    "EditDefaultContext.do?select=" +
                                    URLEncoder.encode(defaultContextName) +
                                    "&nodeLabel=" +
                                    URLEncoder.encode(nodeLabel),
                                    "content",
                                    false, domain);
            hostNode.addChild(defaultContextNode);
            getResources(defaultContextNode, defaultContextName, resources);
        }
    }  
    
    
    /**
     * Append nodes for any defined loggers for the specified container.
     *
     * @param containerNode Container node for the tree control
     * @param containerName Object name of the parent container
     *
     * @exception Exception if an exception occurs building the tree
     */
    public void getLoggers(TreeControlNode containerNode,
                           String containerName) throws Exception {

        String domain = containerNode.getDomain();
        Iterator loggerNames =
            Lists.getLoggers(mBServer, containerName).iterator();
        while (loggerNames.hasNext()) {
            String loggerName = (String) loggerNames.next();
            ObjectName objectName = new ObjectName(loggerName);
            String nodeLabel = "Logger for " + containerNode.getLabel();
            TreeControlNode loggerNode =
                new TreeControlNode(loggerName,
                                    "Logger.gif",
                                    nodeLabel,
                                    "EditLogger.do?select=" +
                                    URLEncoder.encode(loggerName) +
                                    "&nodeLabel=" +
                                    URLEncoder.encode(nodeLabel),
                                    "content",
                                    false, domain);
            containerNode.addChild(loggerNode);
        }

    }


    /**
     * Append nodes for any defined realms for the specified container.
     *
     * @param containerNode Container node for the tree control
     * @param containerName Object name of the parent container
     *
     * @exception Exception if an exception occurs building the tree
     */
    public void getRealms(TreeControlNode containerNode,
                          String containerName) throws Exception {

        String domain = containerNode.getDomain();
        Iterator realmNames =
            Lists.getRealms(mBServer, containerName).iterator();
        while (realmNames.hasNext()) {
            String realmName = (String) realmNames.next();
	    ObjectName objectName = new ObjectName(realmName);
            // Create tree nodes for non JAASRealm only
            try {
                mBServer.getAttribute(objectName, "validate");
            } catch (AttributeNotFoundException e) {
	        String nodeLabel = "Realm for " + containerNode.getLabel();
	        TreeControlNode realmNode =
		    new TreeControlNode(realmName,
                                    "Realm.gif",
                                    nodeLabel,
                                    "EditRealm.do?select=" +
                                    URLEncoder.encode(realmName) +
                                    "&nodeLabel=" +
                                    URLEncoder.encode(nodeLabel),
                                    "content",
                                    false, domain);
                containerNode.addChild(realmNode);
            }
        }
        
    }   
        
    
    /**
     * Append nodes for any define resources for the specified Context.
     *
     * @param containerNode Container node for the tree control
     * @param containerName Object name of the parent container
     * @param resources The MessageResources for our localized messages
     *  messages
     */
    public void getResources(TreeControlNode containerNode, String containerName,
                              MessageResources resources) throws Exception {

        String domain = containerNode.getDomain();
        ObjectName oname = new ObjectName(containerName);
        String type = oname.getKeyProperty("type");
        if (type == null) {
            type = oname.getKeyProperty("j2eeType");
            if (type.equals("WebModule")) {
                type = "Context";
            } else {
                type = "";
            }
        }
        String path = "";
        String host = "";
        String name = oname.getKeyProperty("name");
        if ((name != null) && (name.length() > 0)) {
            // context resource
            name = name.substring(2);
            int i = name.indexOf("/");
            host = name.substring(0,i);
            path = name.substring(i);
        }     
        TreeControlNode subtree = new TreeControlNode
            ("Context Resource Administration " + containerName,
             "folder_16_pad.gif",
             resources.getMessage("resources.treeBuilder.subtreeNode"),
             null,
             "content",
             true, domain);        
        containerNode.addChild(subtree);
        TreeControlNode datasources = new TreeControlNode
            ("Context Data Sources " + containerName,
            "Datasource.gif",
            resources.getMessage("resources.treeBuilder.datasources"),
            "resources/listDataSources.do?resourcetype=" + 
                URLEncoder.encode(type) + "&path=" +
                URLEncoder.encode(path) + "&host=" + 
                URLEncoder.encode(host) + "&domain=" + 
                URLEncoder.encode(domain) + "&forward=" +
                URLEncoder.encode("DataSources List Setup"),
            "content",
            false, domain);
        TreeControlNode mailsessions = new TreeControlNode
            ("Context Mail Sessions " + containerName,
            "Mailsession.gif",
            resources.getMessage("resources.treeBuilder.mailsessions"),
            "resources/listMailSessions.do?resourcetype=" + 
                URLEncoder.encode(type) + "&path=" +
                URLEncoder.encode(path) + "&host=" + 
                URLEncoder.encode(host) + "&domain=" + 
                URLEncoder.encode(domain) + "&forward=" +
                URLEncoder.encode("MailSessions List Setup"),
            "content",
            false, domain);
        TreeControlNode resourcelinks = new TreeControlNode
            ("Resource Links " + containerName,
            "ResourceLink.gif",
            resources.getMessage("resources.treeBuilder.resourcelinks"),
            "resources/listResourceLinks.do?resourcetype=" + 
                URLEncoder.encode(type) + "&path=" +
                URLEncoder.encode(path) + "&host=" + 
                URLEncoder.encode(host) + "&domain=" + 
                URLEncoder.encode(domain) + "&forward=" +
                URLEncoder.encode("ResourceLinks List Setup"),
            "content",
            false, domain);
        TreeControlNode envs = new TreeControlNode
            ("Context Environment Entries "+ containerName,
            "EnvironmentEntries.gif",
            resources.getMessage("resources.env.entries"),
            "resources/listEnvEntries.do?resourcetype=" + 
                URLEncoder.encode(type) + "&path=" +
                URLEncoder.encode(path) + "&host=" + 
                URLEncoder.encode(host) + "&domain=" + 
                URLEncoder.encode(domain) + "&forward=" +
                URLEncoder.encode("EnvEntries List Setup"),
            "content",
            false, domain);
        subtree.addChild(datasources);
        subtree.addChild(mailsessions);
        subtree.addChild(resourcelinks);
        subtree.addChild(envs);
    }
    
    
   /**
     * Append nodes for any defined valves for the specified container.
     *
     * @param containerNode Container node for the tree control
     * @param containerName Object name of the parent container
     *
     * @exception Exception if an exception occurs building the tree
     */
    public void getValves(TreeControlNode containerNode,
                          String containerName) throws Exception {

        String domain = containerNode.getDomain();
        Iterator valveNames =
                Lists.getValves(mBServer, containerName).iterator();        
        while (valveNames.hasNext()) {
            String valveName = (String) valveNames.next();
            ObjectName objectName = new ObjectName(valveName);
            String nodeLabel = "Valve for " + containerNode.getLabel();
            TreeControlNode valveNode =
                new TreeControlNode(valveName,
                                    "Valve.gif",
                                    nodeLabel,
                                    "EditValve.do?select=" +
                                    URLEncoder.encode(valveName) +
                                    "&nodeLabel=" +
                                    URLEncoder.encode(nodeLabel) +
                                    "&parent=" +
                                    URLEncoder.encode(containerName),
                                    "content",
                                    false, domain);
            containerNode.addChild(valveNode);
        }
    }
}
