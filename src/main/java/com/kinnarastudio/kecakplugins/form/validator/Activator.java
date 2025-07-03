package com.kinnarastudio.kecakplugins.form.validator;

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
        registrationList.add(context.registerService(FileTypeValidator.class.getName(), new FileTypeValidator(), null));
        registrationList.add(context.registerService(DateTimeValidator.class.getName(), new DateTimeValidator(), null));
        registrationList.add(context.registerService(MultiFieldValidator.class.getName(), new MultiFieldValidator(), null));
        registrationList.add(context.registerService(OptionsValueValidator.class.getName(), new OptionsValueValidator(), null));
        registrationList.add(context.registerService(ValuesMatcherValidator.class.getName(), new ValuesMatcherValidator(), null));
        registrationList.add(context.registerService(NegationValidator.class.getName(), new NegationValidator(), null));
    }

    public void stop(BundleContext context) {
        for (ServiceRegistration registration : registrationList) {
            registration.unregister();
        }
    }
}