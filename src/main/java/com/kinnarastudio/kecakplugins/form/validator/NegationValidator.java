package com.kinnarastudio.kecakplugins.form.validator;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormValidator;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.plugin.base.PluginManager;

import java.util.Arrays;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class NegationValidator extends FormValidator {
    public final static String LABEL = "Negation Validator";

    @Override
    public boolean validate(Element element, FormData formData, String[] strings) {
        final String elementId = element.getPropertyString("id");
        final PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        final Map<String, Object> elementValidator = getValidator();
        final FormValidator validator = pluginManager.getPlugin(elementValidator);

        if(isMandatory()) {
            final boolean isSupplied = Arrays.stream(strings)
                    .anyMatch(Predicate.not(String::isEmpty));

            if(!isSupplied) {
                formData.addFormError(elementId, ResourceBundleUtil.getMessage("form.defaultvalidator.err.missingValue"));
                return false;
            }
        }
        final boolean isValid = validator.validate(element, formData, strings);
        if(isValid) {
            formData.addFormError(elementId, getErrorMessage());
            return false;
        } else {
            formData.getFormErrors().remove(elementId);
            return true;
        }
    }

    @Override
    public String getName() {
        return LABEL;
    }

    @Override
    public String getVersion() {
        PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        ResourceBundle resourceBundle = pluginManager.getPluginMessageBundle(getClassName(), "/messages/BuildNumber");
        String buildNumber = resourceBundle.getString("buildNumber");
        return buildNumber;
    }

    @Override
    public String getDescription() {
        return getClass().getPackage().getImplementationTitle();
    }

    @Override
    public String getLabel() {
        return LABEL;
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/NegationValidator.json", null, false, "/messages/NegationValidator");
    }

    protected Map<String, Object> getValidator() {
        return (Map<String, Object>) getProperty("validator");
    }

    protected String getErrorMessage() {
        return ifEmpty(getPropertyString("errorMessage"), () -> "Invalid value");
    }

    protected boolean isMandatory() {
        return "true".equalsIgnoreCase(getPropertyString("isMandatory"));
    }

    protected String ifEmpty(String value, Supplier<String> failover) {
        return value == null || value.isEmpty() ? failover.get() : value;
    }

    @Override
    public String getElementDecoration() {
        return isMandatory() ? "*" : "";
    }
}
