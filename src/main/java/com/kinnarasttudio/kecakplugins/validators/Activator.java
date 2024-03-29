package com.kinnarasttudio.kecakplugins.validators;

import java.util.ArrayList;
import java.util.Collection;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

    protected Collection<ServiceRegistration> registrationList;

    public void start(BundleContext context) {
        registrationList = new ArrayList<ServiceRegistration>();

        //Register plugin here
        registrationList.add(context.registerService(OptionsValueValidator.class.getName(), new OptionsValueValidator(), null));
        registrationList.add(context.registerService(MultiFieldValidator.class.getName(), new MultiFieldValidator(), null));
        registrationList.add(context.registerService(FileTypeValidator.class.getName(), new FileTypeValidator(), null));
    }

    public void stop(BundleContext context) {
        for (ServiceRegistration registration : registrationList) {
            registration.unregister();
        }
    }
}