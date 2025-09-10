package org.frameworkset.boot;

import org.frameworkset.boot.event.ApplicationListener;
import org.frameworkset.runtime.CommonLauncher;
import org.frameworkset.runtime.OSInfo;
import org.frameworkset.spi.assemble.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseApplicationStart {
	protected static Logger log = LoggerFactory.getLogger(BaseApplicationStart.class);
	protected static File appdir ;
	protected List<ApplicationListener> applicationListeners;
	public BaseApplicationStart() {
		// TODO Auto-generated constructor stub
	}



	protected  void initApplicationListeners(){
		String applicationBootListeners = PropertiesUtil.getPropertiesContainer().getProperty("web.applicationBootListeners");
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

	protected    ApplicationBootContext buildApplicationBootContext(String context,int port,File appdir,String docBase,String host,String workTempDir){
		DefaultApplicationBootContext applicationBootContext = new DefaultApplicationBootContext();
		applicationBootContext.setAppdir(appdir);
		applicationBootContext.setContext(context);
		applicationBootContext.setPort(port);
		applicationBootContext.setHost(host);
		applicationBootContext.setDocBase(docBase);
        applicationBootContext.setWorkTempDir(workTempDir);
		return applicationBootContext;
	}
	protected   int getThreadPoolIdleTimeout(){
//		String threadPoolIdleTimeout_ = System.getProperty("threadPoolIdleTimeout","60000");
//		String threadPoolIdleTimeout = CommonLauncher.getProperty("threadPoolIdleTimeout", threadPoolIdleTimeout_);
//		int p = Integer.parseInt(threadPoolIdleTimeout.trim());
//		return p;
		return _getIntProperty("web.threadPoolIdleTimeout",60000);
	}

	protected   int getMinThreads(){
//		String minThreads_ = System.getProperty("minThreads","10");
//		String minThreads = CommonLauncher.getProperty("minThreads", minThreads_);
//		int p = Integer.parseInt(minThreads.trim());
//		return p;
		return _getIntProperty("web.minThreads",10);
	}

	protected   int getMaxThreads(){
//		String maxThreads_ = System.getProperty("maxThreads","200");
//		String maxThreads = CommonLauncher.getProperty("maxThreads", maxThreads_);
//		int p = Integer.parseInt(maxThreads.trim());
//		return p;
		return _getIntProperty("web.maxThreads",200);
	}

	protected   int getIdleTimeout(){
		return _getIntProperty("web.idleTimeout",30000);
//		String idleTimeout_ = System.getProperty("idleTimeout","30000");
//		String idleTimeout = CommonLauncher.getProperty("idleTimeout", idleTimeout_);
//		int p = Integer.parseInt(idleTimeout.trim());
//		return p;
	}
	protected   String getContextPath(){
//		String contextPath_ = System.getProperty("contextPath","");
//		String contextPath = CommonLauncher.getProperty("contextPath",
//				contextPath_);
		String contextPath = _getStringProperty("web.contextPath","");
		if (contextPath.equals(""))
			;
		else if(!contextPath.startsWith("/")){
			contextPath = "/"+contextPath;
		}
		return contextPath;
	}

	protected   String getDocBase(){
//		String docBase_ = System.getProperty("docBase","./WebRoot");
//		String docBase = CommonLauncher.getProperty("docBase",docBase_);
//		return docBase;
		return _getStringProperty("web.docBase","./WebRoot");
	}
    
    protected String getWorkTempDir(){
        return _getStringProperty("web.workTempDir","./temp");
    }
	/**
	 * 先从配置文件获取属性，如果配置文件中没有，则从系统jvm变量中取，如果系统变量中没有，则采用默认值
	 * @param propertyName
	 * @param defaultValue
	 * @return
	 */
	private int _getIntProperty(String propertyName,int defaultValue){

		return PropertiesUtil.getPropertiesContainer().getIntSystemEnvProperty(propertyName,defaultValue);

	}
	/**
	 * 先从配置文件获取属性，如果配置文件中没有，则从系统jvm变量中取，如果系统变量中没有，则采用默认值
	 * @param propertyName
	 * @param defaultValue
	 * @return
	 */
	private boolean _getBooleanProperty(String propertyName,boolean defaultValue){
		return PropertiesUtil.getPropertiesContainer().getBooleanSystemEnvProperty(propertyName,defaultValue);
	}
	/**
	 * 先从配置文件获取属性，如果配置文件中没有，则从系统jvm变量中取，如果系统变量中没有，则采用默认值
	 * @param propertyName
	 * @param defaultValue
	 * @return
	 */
	protected String _getStringProperty(String propertyName,String defaultValue){
		return PropertiesUtil.getPropertiesContainer().getSystemEnvProperty(propertyName,defaultValue);
	}
	public abstract  String getServerType();

	protected   int getPort(){
//		String port_ = System.getProperty("port","8080");
//		String port = CommonLauncher.getProperty("port", port_);
//		int p = Integer.parseInt(port.trim());
//		return p;

		return _getIntProperty("web.port",8080);
	}
	public   String getHost(){
//		String host_ = System.getProperty("host","127.0.0.1");
//		String host = CommonLauncher.getProperty("host",host_);
//		return host;

		return _getStringProperty("web.host","0.0.0.0");
	}

	public   boolean getScanManifest(){
//		String host_ = System.getProperty("host","127.0.0.1");
//		String host = CommonLauncher.getProperty("host",host_);
//		return host;

		return _getBooleanProperty("web.scanManifest",false);
	}
    
    public boolean isUpgradeProtocol() {
        return _getBooleanProperty("web.upgradeProtocol",false);
    }

  

	protected abstract void startContainer(ApplicationBootContext applicationBootContext)  throws Exception;
	protected abstract void afterStartContainer(ApplicationBootContext applicationBootContext)  throws Exception;
 


    private static File computeApplicationDir(URL location, File defaultDir) {

        if (location == null) {
            log.warn("Warning: Cannot locate the program directory. Assuming default.");
            return defaultDir;
        }
        if (!"file".equalsIgnoreCase(location.getProtocol())) {
            log.warn("Warning: Unrecognized location type. Assuming default.");
            return new File(".");
        }
        String file = location.getFile();
        if (!file.endsWith(".jar") && !file.endsWith(".zip")) {
            try {
                return (new File(URLDecoder.decode(location.getFile(), "UTF-8"))).getParentFile();
            } catch (UnsupportedEncodingException e) {

            }

            log.warn("Warning: Unrecognized location type. Assuming default.");
            return new File(location.getFile());
        } else {

            try {
                File path = null;// new
                // File(URLDecoder.decode(location.toExternalForm().substring(6),
                // "UTF-8")).getParentFile();
                // if(!CommonLauncher.isLinux() && !CommonLauncher.isOSX())
                if (OSInfo.isWindows()) {
                    path = new File(URLDecoder.decode(location.toExternalForm().substring(6), "UTF-8")).getParentFile();
                } else {
                    path = new File(URLDecoder.decode(location.toExternalForm().substring(5), "UTF-8")).getParentFile();
                }
                // System.out.println("path: " + path.getAbsolutePath());
                // System.out.println("location: " + location.getPath());
                // System.out.println("external from location: " +
                // URLDecoder.decode(location.toExternalForm().substring(6),
                // "UTF-8"));
                // System.out.println("external from location + 6: " +
                // URLDecoder.decode(location.toExternalForm(), "UTF-8"));

                return path;
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
               log.error("",e);
            }
        }

        log.warn("Warning: Unrecognized location type. Assuming default.");
        return new File(location.getFile());
    }
	public void start() {

		ApplicationBootContext applicationBootContext = null;
		try {
			initApplicationListeners();
			// 服务器的监听端口

           
			String contextPath = getContextPath();

			String docBase = getDocBase();
            String workTempDir = getWorkTempDir();
            log.info("workTempDir:{}",workTempDir);
			int port = getPort();
			String host = this.getHost();
			log.info("start "+getServerType()+" server on "+host+":"+port+ " with contextpath "+contextPath);
            
			applicationBootContext = buildApplicationBootContext(  contextPath,  port,  appdir,docBase,host,workTempDir);

            applicationBootContext.setUpgradeProtocol(isUpgradeProtocol());
			beforeStartHandler(  applicationBootContext);
			startContainer(applicationBootContext);
			log.info("http://"+host+":"+port+contextPath);
			afterStartContainer( applicationBootContext);
		}  catch (Exception e) {
			log.error("",e);
			failureHandler(e,  applicationBootContext);
		}
		catch (Throwable e) {
			log.error("",e);
			failureHandler(e,  applicationBootContext);
		}

		

	}




	protected   void beforeStartHandler(ApplicationBootContext applicationBootContext){
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
	protected   void afterStartHandler(ApplicationBootContext applicationBootContext){
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
	protected   void failureHandler(Throwable throwable,ApplicationBootContext applicationBootContext){
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
		BaseApplicationStart.appdir = appdir;
	}

}
