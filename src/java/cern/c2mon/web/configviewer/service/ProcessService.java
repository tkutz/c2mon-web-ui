package cern.c2mon.web.configviewer.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.shared.client.process.ProcessNameResponse;
import cern.c2mon.web.configviewer.util.XsltTransformUtility;

/**
 * Command service providing the XML representation of a given alarm
 * */
@Service
public class ProcessService {

  /**
   * ConfigLoaderService logger
   * */
  private static Logger logger = Logger.getLogger(ProcessService.class);
  
  /** the path to the xslt document */
  private static final String XSLT_PATH = "/generic_tag.xsl";
  
  /**
   * Performs xslt transformations. 
   * */
  @Autowired
  private XsltTransformUtility xsltTransformer;

  /**
   * Gateway to ConfigLoaderService 
   * */
  @Autowired
  private ServiceGateway gateway;

  @PostConstruct
  private void init() {

  }

  /**
   * Gets the XML representation of the process
   * @param processName processName
   * @return XML
   * @throws Exception if id not found or a non-numeric id was requested ({@link TagIdException}), or any other exception
   * thrown by the underlying service gateway.
   * */
  public String getProcessXml(final String processName) throws Exception {

    try {
      String  xml = getXml(processName);
      if (xml != null)
        return xml;
      else
        throw new TagIdException("No luck. Try another processName.");
    } catch (NumberFormatException e) {
      throw new TagIdException("Invalid processName");
    }
  }

  /**
   * Gets all available process names
   * @return a collection of all available process names
   * */
  public Collection<String> getProcessNames() {

    Collection <ProcessNameResponse> processNames = gateway.getTagManager().getProcessNames();
    Collection <String> names = new ArrayList<String>();

    Iterator<ProcessNameResponse> i = processNames.iterator();

    while (i.hasNext()) {

      ProcessNameResponse p = (ProcessNameResponse) i.next();
      names.add(p.getProcessName());
    }
    return names;
  }    

  
  public String generateHtmlResponse(final String processName) throws TagIdException {

    String xml = getXml(processName);
    
    String html = null;

    try {
      html = xsltTransformer.performXsltTransformation(xml);
    } catch (TransformerException e) {
      logger.error("Error while performing xslt transformation.");
      throw new TagIdException("Error while performing xslt transformation.");
    }

    return html;
  }


  /**
   * Private helper method. Gets the XML representation of the process
   * @param processName processName
   * @return XML
   * */
  private String getXml(final String processName) {

    String xml = gateway.getTagManager().getProcessXml(processName);

    logger.debug("getXml fetch for process " + processName + ": " + (xml == null ? "NULL" : "SUCCESS"));
    return xml;
  }
}
