package org.datacite.mds.tools;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractTool {
    
    private static final String APPLICATION_CONTEXT = "META-INF/spring/applicationContext.xml";
    
    @Autowired
    private LocalContainerEntityManagerFactoryBean emFactory;
    
    protected void disableValidation() {
        emFactory.getJpaPropertyMap().put("javax.persistence.validation.mode", "ddl");
        emFactory.afterPropertiesSet();
    }
    
    @SuppressWarnings("unchecked")
    public static final void initAndRun(String[] args) {
        AbstractApplicationContext context = null;
        try {
            context = new ClassPathXmlApplicationContext(APPLICATION_CONTEXT);
            String callingClassName = Thread.currentThread().getStackTrace()[2].getClassName();
            Class<AbstractTool> callingClass = (Class<AbstractTool>) Class.forName(callingClassName);
            AbstractTool tool = context.getBean(callingClass);
            tool.run(args);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (context != null)
                context.close();
        }
    }
    
    public abstract void run(String[] args) throws Exception;
}
