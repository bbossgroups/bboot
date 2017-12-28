package org.frameworkset.boot;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.frameworkset.boot.event.ApplicationListener;
import org.frameworkset.runtime.CommonLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ApplicationStart {
	private static Logger log = LoggerFactory.getLogger(ApplicationStart.class);
	private static File appdir ;
	private static List<ApplicationListener> applicationListeners;
	public ApplicationStart() {
		// TODO Auto-generated constructor stub
	}

	private static void initApplicationListeners(){
		String applicationBootListeners = CommonLauncher.getProperty("applicationBootListeners");
		if(applicationBootListeners != null && !applicationBootListeners.trim().equals("")){
			String[] applicationBootListeners_ = applicationBootListeners.split(",");
			ApplicationListener applicationListener = null;
			applicationListeners = new ArrayList<ApplicationListener>();
			for(int i = 0; i < applicationBootListeners_.length; i ++){
				String listener = applicationBootListeners_[i].trim();
				try {
					applicationListener = (ApplicationListener)Class.forName(listener).newInstance();
					applicationListeners.add(applicationListener);
				} catch (InstantiationException e) {
					log.error("Instantiation Exception:"+listener,e);
				} catch (IllegalAccessException e) {
					log.error("Instantiation Exception:"+listener,e);
				} catch (ClassNotFoundException e) {
					log.error("Instantiation Exception:"+listener,e);
				}
			}
		}

	}

	private static  ApplicationBootContext buildApplicationBootContext(String context,int port,File appdir,String docBase){
		DefaultApplicationBootContext applicationBootContext = new DefaultApplicationBootContext();
		applicationBootContext.setAppdir(appdir);
		applicationBootContext.setContext(context);
		applicationBootContext.setPort(port);
		applicationBootContext.setDocBase(docBase);
		return applicationBootContext;
	}

	public static void main(String[] args) {

		ApplicationBootContext applicationBootContext = null;
		try {
			initApplicationListeners();
			// 服务器的监听端口
			String port_ = System.getProperty("port","8080");
			String port = CommonLauncher.getProperty("port", port_);

			String contextPath_ = System.getProperty("contextPath","");
			String contextPath = CommonLauncher.getProperty("contextPath",
					contextPath_);
			if (contextPath.equals(""))
				;
			else if(!contextPath.startsWith("/")){
				contextPath = "/"+contextPath;
			}
			String docBase_ = System.getProperty("docBase","./WebRoot");
			String docBase = CommonLauncher.getProperty("docBase",docBase_);

			int p = Integer.parseInt(port);

			applicationBootContext = buildApplicationBootContext(  contextPath,  p,  appdir,docBase);
			beforeStartHandler(  applicationBootContext);
			Server server = new Server(p);
			// 关联一个已经存在的上下文
			WebAppContext context = new WebAppContext();
			// 设置描述符位置
			//context.setDescriptor("./"+webroot+"/WEB-INF/web.xml");
			// 设置Web内容上下文路径

			context.setResourceBase(docBase);
			// 设置上下文路径
			context.setContextPath(contextPath);
			context.setParentLoaderPriority(true);
			ContextHandlerCollection contexts = new ContextHandlerCollection();
			log.info("WebAppContext:"+context.toString());

			contexts.setHandlers(new Handler[] { context });

			// This webapp will use jsps and jstl. We need to enable the
			// AnnotationConfiguration in order to correctly
			// set up the jsp container
			Configuration.ClassList classlist = Configuration.ClassList
					.setServerDefault( server );
			classlist.addBefore(
					"org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
					"org.eclipse.jetty.annotations.AnnotationConfiguration" );

			// Set the ContainerIncludeJarPattern so that jetty examines these
			// container-path jars for tlds, web-fragments etc.
			// If you omit the jar that contains the jstl .tlds, the jsp engine will
			// scan for them instead.
			context.setAttribute(
					"org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
					".*/[^/]*servlet-api-[^/]*\\.jar$|.*/javax.servlet.jsp.jstl-.*\\.jar$|.*/[^/]*taglibs.*\\.jar$" );



			// Configure a LoginService.
			// Since this example is for our test webapp, we need to setup a
			// LoginService so this shows how to create a very simple hashmap based
			// one. The name of the LoginService needs to correspond to what is
			// configured in the webapp's web.xml and since it has a lifecycle of
			// its own we register it as a bean with the Jetty server object so it
			// can be started and stopped according to the lifecycle of the server
			// itself.
//			HashLoginService loginService = new HashLoginService();
//			loginService.setName( "Test Realm" );
//			loginService.setConfig( "src/test/resources/realm.properties" );
//			server.addBean( loginService );

			server.setHandler(contexts);


			// 启动
			server.start();
			applicationBootContext.setServerStatus(server.getState());
			afterStartHandler(  applicationBootContext);
			log.info("http://localhost:"+port+contextPath);
			server.join();
		} catch (FileNotFoundException e) {
			log.error("",e);
			failureHandler(e,  applicationBootContext);
		} catch (SAXException e) {
			log.error("",e);
			failureHandler(e,  applicationBootContext);
		} catch (IOException e) {
			log.error("",e);
			failureHandler(e,  applicationBootContext);
		} catch (Exception e) {
			log.error("",e);
			failureHandler(e,  applicationBootContext);
		}
		catch (Throwable e) {
			log.error("",e);
			failureHandler(e,  applicationBootContext);
		}

		

	}




	private static void beforeStartHandler(ApplicationBootContext applicationBootContext){
		ApplicationListener applicationListener = null;
		for(int i = 0; applicationListeners != null && i < applicationListeners.size(); i ++){
			applicationListener = applicationListeners.get(i);
			try{
				applicationListener.beforeStart(applicationBootContext);
			}
			catch (Exception e){
				log.error("beforeStart failed:",e);

			}

		}
	}
	private static void afterStartHandler(ApplicationBootContext applicationBootContext){
		ApplicationListener applicationListener = null;
		for(int i = 0; applicationListeners != null && i < applicationListeners.size(); i ++){
			applicationListener = applicationListeners.get(i);
			try{
				applicationListener.afterStart(applicationBootContext);
			}
			catch (Exception e){
				log.error("beforeStart failed:",e);

			}

		}
	}
	private static void failureHandler(Throwable throwable,ApplicationBootContext applicationBootContext){
		ApplicationListener applicationListener = null;
		for(int i = 0; applicationListeners != null && i < applicationListeners.size(); i ++){
			applicationListener = applicationListeners.get(i);
			try{
				applicationListener.failureStart(applicationBootContext,throwable);
			}
			catch (Exception e){
				log.error("beforeStart failed:",e);

			}

		}
	}
	
	public static void setAppdir(File appdir) {
		ApplicationStart.appdir = appdir;
	}

}
