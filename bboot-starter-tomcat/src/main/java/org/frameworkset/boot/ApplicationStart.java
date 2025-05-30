package org.frameworkset.boot;


import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardThreadExecutor;
import org.apache.catalina.startup.Constants;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ApplicationStart extends BaseApplicationStart{
	private static Logger log = LoggerFactory.getLogger(ApplicationStart.class);
	private Tomcat tomcat = null;
	private static final String PROP_PROTOCOL = "org.apache.coyote.http11.Http11NioProtocol";

	private static final String DEFAULT_CHARSET = "UTF-8";

	public ApplicationStart() {
		// TODO Auto-generated constructor stub
	}
	@Override
	public String getServerType() {
		return "Tomcat";
	}
	private URL getWebappConfigFileFromDirectory(File docBase) {
		URL result = null;
		File webAppContextXml = new File(docBase, Constants.ApplicationWebXml);
		if (webAppContextXml.exists()) {
			try {
				result = webAppContextXml.toURI().toURL();
			} catch (MalformedURLException e) {
				log.info(
						"Unable to determine web application context.xml " + docBase, e);
			}
		}
		return result;
	}
    protected   String getDocBase(){
//		String docBase_ = System.getProperty("docBase","./WebRoot");
//		String docBase = CommonLauncher.getProperty("docBase",docBase_);
//		return docBase;
        String docBase = new File("WebRoot").getAbsolutePath();
        log.info("Default docbase:{}",docBase);
        return _getStringProperty("web.docBase",docBase);
    }
	@Override
	protected void startContainer(ApplicationBootContext applicationBootContext)  throws Exception{

        // 在启动代码前添加日志配置
//        System.setProperty("org.apache.catalina.level", "WARNING");
		tomcat = new Tomcat();
//		tomcat.setPort(this.getPort());
		tomcat.setBaseDir(".");
//		tomcat.setBaseDir(applicationBootContext.getDocBase());

		Connector connector = new Connector(PROP_PROTOCOL);
        connector.setProperty("address", applicationBootContext.getHost());
		connector.setPort(getPort());
		connector.setURIEncoding(DEFAULT_CHARSET);

		// 设置一下最大线程数
		this.tomcat.getService().addConnector(connector);
		StandardThreadExecutor executor = new StandardThreadExecutor();
		executor.setMaxThreads(this.getMaxThreads());

		connector.getService().addExecutor(executor);

		this.tomcat.setConnector(connector);

		this.tomcat.setHostname(getHost());
		this.tomcat.getEngine().setBackgroundProcessorDelay(30);
		tomcat.getHost().setAutoDeploy(false);
		tomcat.getHost().setAppBase(applicationBootContext.getDocBase());
		String contextPath = applicationBootContext.getContext();
        if(contextPath == null || contextPath.equals("/"))
            contextPath = "";
		StandardContext context = new StandardContext();
		context.setParentClassLoader(Thread.currentThread().getContextClassLoader());
		context.setPath(contextPath);
		context.setDelegate(false);
		context.setDocBase(applicationBootContext.getDocBase());
		context.setAltDDName(applicationBootContext.getDocBase()+"/WEB-INF/web.xml");
     

        context.setWorkDir(applicationBootContext.getWorkTempDir());
		context.setConfigFile(getWebappConfigFileFromDirectory(new File(applicationBootContext.getDocBase())));
		ContextConfig contextConfig = new ContextConfig();

		context.addLifecycleListener(contextConfig );
		context.addLifecycleListener(new Tomcat.DefaultWebXmlListener());
		context.addLifecycleListener(new Tomcat.FixContextListener());

//		context.addLifecycleListener(new StoreMergedWebXmlListener(applicationBootContext));
//		context.setDefaultWebXml(applicationBootContext.getDocBase()+"/WEB-INF/web.xml");
//		context.addWatchedResource(applicationBootContext.getDocBase()+"/WEB-INF/web.xml");
		log.info(applicationBootContext.getDocBase()+"/WEB-INF/web.xml");
//
//		WebResourceRoot resources = new StandardRoot(context);
//		resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
//				applicationBootContext.getDocBase(), "/"));
//		context.setResources(resources);
		((StandardJarScanner)context.getJarScanner()).setScanManifest(getScanManifest());
		tomcat.getHost().addChild(context);



		tomcat.start();

		log.info("configuring app with basedir: " + applicationBootContext.getDocBase());

		applicationBootContext.setServerStatus("started");
	}
	private static class StoreMergedWebXmlListener implements LifecycleListener {
		private ApplicationBootContext applicationBootContext;
		private static final String MERGED_WEB_XML = "org.apache.tomcat.util.scan.MergedWebXml";

		public StoreMergedWebXmlListener(ApplicationBootContext applicationBootContext){
			this.applicationBootContext = applicationBootContext;
		}

		@Override
		public void lifecycleEvent(LifecycleEvent event) {
			if (event.getType().equals(Lifecycle.CONFIGURE_START_EVENT)) {
				onStart((Context) event.getLifecycle());
			}
		}

		private void onStart(Context context) {
			ServletContext servletContext = context.getServletContext();

			if (servletContext.getAttribute(MERGED_WEB_XML) == null) {
				servletContext.setAttribute(MERGED_WEB_XML, getEmptyWebXml());
			}
		}

		private String getEmptyWebXml() {
			InputStream stream = null;
			try {
				try {
					stream = new FileInputStream(new File(applicationBootContext.getDocBase()+"/WEB-INF/web.xml"));
					if (stream == null) {
						throw new IllegalArgumentException("Unable to read "+applicationBootContext.getDocBase()+"/WEB-INF/web.xml");
					}
					StringBuilder out = new StringBuilder();
					InputStreamReader reader = new InputStreamReader(stream, DEFAULT_CHARSET);
					char[] buffer = new char[1024 * 4];
					int bytesRead = -1;
					while ((bytesRead = reader.read(buffer)) != -1) {
						out.append(buffer, 0, bytesRead);
					}
					return out.toString();
				} finally {
					stream.close();
				}
			} catch (IOException ex) {
				throw new IllegalStateException(ex);
			}
		}

	}
	@Override
	protected void afterStartContainer(ApplicationBootContext applicationBootContext) throws Exception{
		Thread tomcatAwaitThread = new Thread("container-1" ) {
			@Override
			public void run() {
				ApplicationStart.this.tomcat.getServer().await();
			}
		};
		tomcatAwaitThread.setContextClassLoader(getClass().getClassLoader());
		tomcatAwaitThread.setDaemon(false);
		tomcatAwaitThread.start();

	}


	public static void main(String[] args) {
		ApplicationStart applicationStart = new ApplicationStart();
		applicationStart.start();
	}

}
