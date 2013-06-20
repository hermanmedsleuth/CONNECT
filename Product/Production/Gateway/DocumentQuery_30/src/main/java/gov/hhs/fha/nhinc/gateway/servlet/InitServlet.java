/*
 * Copyright (c) 2009-2013, United States Government, as represented by the Secretary of Health and Human Services. 
 * All rights reserved. 
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met: 
 *     * Redistributions of source code must retain the above 
 *       copyright notice, this list of conditions and the following disclaimer. 
 *     * Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimer in the documentation 
 *       and/or other materials provided with the distribution. 
 *     * Neither the name of the United States Government nor the 
 *       names of its contributors may be used to endorse or promote products 
 *       derived from this software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY 
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND 
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
package gov.hhs.fha.nhinc.gateway.servlet;

import gov.hhs.fha.nhinc.configuration.jmx.DocumentQuery30WebServices;
import gov.hhs.fha.nhinc.gateway.AbstractJMXEnabledServlet;
import gov.hhs.fha.nhinc.gateway.executorservice.ExecutorServiceHelper;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;

/**
 * Started on webapplication init, creates the main ExecutorService and CamelContext instances Note the following: 1.
 * Main ExecutorService creates a new thread pool of size specified on construction, independent/in addition to
 * glassfish thread pool(s) set in domain.xml. 2. ExecutorService automatically handles any thread death condition and
 * creates a new thread in this case
 * 
 * 3. Also creates a second largeJobExecutor with a fixed size thread pool (largeJobExecutor is used for TaskExecutors
 * that get a callable list of size comparable to the size of the main ExecutorService)
 * 
 * 4. See {@link gov.fha.hhs.nhinc.gateway.AbstractJMXEnabledServlet} for JMX init and destroy functionality.
 * 
 * @author paul.eftis, msw
 */
public class InitServlet extends AbstractJMXEnabledServlet {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -4229185731377926278L;

    /** The Constant LOG. */
    private static final Logger LOG = Logger.getLogger(InitServlet.class);

    /** The Constant MBEAN_NAME. */
    private static final String MBEAN_NAME = NhincConstants.JMX_DOCUMENT_QUERY_30_BEAN_NAME;

    /** The executor. */
    private static ExecutorService executor = null;
    
    /** The large job executor. */
    private static ExecutorService largeJobExecutor = null;

    /*
     * (non-Javadoc)
     * 
     * @see gov.hhs.fha.nhinc.gateway.AbstractJMXEnabledServlet#getMBeanName()
     */
    @Override
    public String getMBeanName() {
        return MBEAN_NAME;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.hhs.fha.nhinc.gateway.AbstractJMXEnabledServlet#getMBeanInstance(javax.servlet.ServletContext)
     */
    @Override
    public Object getMBeanInstance(ServletContext sc) {
        return new DocumentQuery30WebServices(sc);
    }

    /**
     * Initializes the servlet with parallel fanout executors as well as the DocumentQuery30WebServices JMX bean.
     * 
     * @param config the config
     * @throws ServletException the servlet exception
     * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
     */
    @Override
    @SuppressWarnings("static-access")
    public void init(ServletConfig config) throws ServletException {
        LOG.debug("InitServlet start...");
        executor = Executors.newFixedThreadPool(ExecutorServiceHelper.getInstance().getExecutorPoolSize());
        largeJobExecutor = Executors.newFixedThreadPool(ExecutorServiceHelper.getInstance()
                .getLargeJobExecutorPoolSize());
        
        super.init(config);
    }

    /**
     * Gets the executor service.
     *
     * @return the executor service
     */
    public static ExecutorService getExecutorService() {
        return executor;
    }

    /**
     * Gets the large job executor service.
     *
     * @return the large job executor service
     */
    public static ExecutorService getLargeJobExecutorService() {
        return largeJobExecutor;
    }

    /**
     * Servlet destroy method. Since we don't want to hault the serlvet from coming down we are not propogating errors
     * which are caught in this method.
     * 
     * @see javax.servlet.GenericServlet#destroy()
     */
    @Override
    public void destroy() {
        LOG.debug("InitServlet shutdown stopping executor(s)....");
        if (executor != null) {
            try {
                executor.shutdown();
            } catch (Exception e) {
                LOG.error("Error shutting down executor.", e);
            }
        }
        if (largeJobExecutor != null) {
            try {
                largeJobExecutor.shutdown();
            } catch (Exception e) {
                LOG.error("Error shutting down large jobs executor.", e);
            }
        }

        super.destroy();
    }

}