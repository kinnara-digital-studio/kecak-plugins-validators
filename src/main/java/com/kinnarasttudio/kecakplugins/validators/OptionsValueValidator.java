package com.kinnarasttudio.kecakplugins.validators;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.*;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.plugin.base.PluginManager;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OptionsValueValidator extends FormValidator {
    public final static String LABEL = "Options Value Validator";

    @Override
    public String getElementDecoration() {
        String decoration = "";
        if (isMandatory()) {
            decoration += " * ";
        }
        if (decoration.trim().length() > 0) {
            decoration = decoration.trim();
        }
        return decoration;
    }
    @Override
    public boolean validate(Element element, FormData formData, String[] values) {
        final boolean isMandatory = isMandatory();

        if(isMandatory) {
            final boolean isValid = validateMandatory(values);
            if (!isValid) {
                formData.addFormError(FormUtil.getElementParameterName(element), ResourceBundleUtil.getMessage("form.defaultvalidator.err.missingValue"));
                return false;
            }
        }

        final Stream<String> valuesStream = Optional.ofNullable(values)
                .map(Arrays::stream)
                .orElseGet(Stream::empty);

        final Predicate<Predicate<String>> check;
        if(noneMatch()) {
            check = valuesStream::noneMatch;
        } else {
            check = valuesStream::anyMatch;
        }

        final Set<String> domain = getOptions(element, formData).stream()
                .map(r -> r.getProperty(FormUtil.PROPERTY_VALUE))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());

        final boolean isValid = check.test(domain::contains);
        if(!isValid) {
            final String message = getErrorMessage();
            formData.addFormError(FormUtil.getElementParameterName(element), message);
        }

        return isValid;
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
        return AppUtil.readPluginResource(getClass().getName(), "/properties/OptionsValueValidator.json", null, false, "/messages/OptionsValueValidator");
    }

    protected FormRowSet getOptions(Element element, FormData formData) {
        final FormRowSet optionsMap = new FormRowSet();

        final Object optionProperty = getProperty(FormUtil.PROPERTY_OPTIONS);
        if (optionProperty instanceof Collection) {
            optionsMap.addAll((FormRowSet) optionProperty);
        }

        final PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
        final FormLoadOptionsBinder optionsBinder = pluginManager.getPlugin((Map<String, Object>) getProperty("optionsBinder"));

        Optional.ofNullable(optionsBinder)
                .map(b -> b.load(element, formData.getPrimaryKeyValue(), formData))
                .ifPresent(optionsMap::addAll);

        return optionsMap;
    }

    protected boolean anyMatch() {
        return "anyMatch".equalsIgnoreCase(getPropertyString("matchingType"));
    }

    protected boolean noneMatch() {
        return "noneMatch".equalsIgnoreCase(getPropertyString("matchingType"));
    }

    protected boolean isMandatory() {
        return "true".equalsIgnoreCase(getPropertyString("mandatory"));
    }

    protected String getErrorMessage() {
        return getPropertyString("message");
    }

    protected boolean validateMandatory(String[] values) {
        boolean result = true;
        if (values == null || values.length == 0) {
            result = false;
        } else {
            for (String val : values) {
                if (val == null || val.trim().isEmpty()) {
                    result = false;
                    break;
                }
            }
        }
        return result;
    }
}
