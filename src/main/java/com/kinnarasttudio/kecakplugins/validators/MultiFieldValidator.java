package com.kinnarasttudio.kecakplugins.validators;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormValidator;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author aristo
 *
 * Validate if current element's value(s) have been assigned to other elements
 *
 */
public class MultiFieldValidator extends FormValidator {
    @Override
    public boolean validate(Element element, FormData formData, String[] strings) {
        String id = FormUtil.getElementParameterName(element);
        String label = element.getPropertyString("label");

        if("true".equalsIgnoreCase(getPropertyString("mandatory"))) {
            boolean valid = validateMandatory(formData, id, label, strings, getPropertyString("message"));
            if(!valid) {
                return false;
            }
        }

        Form rootForm = FormUtil.findRootForm(element);

        Set<String> values = Arrays.stream(strings)
                .map(s -> s.split(";"))
                .flatMap(Arrays::stream)
                .filter(s -> !s.isEmpty())
                .distinct()
                .peek(s -> LogUtil.info(getClassName(), "values ["+s+"]"))
                .collect(Collectors.toSet());

        final String elementId = element.getPropertyString("id");

        for(Object o : (Object[])getProperty("elements")) {
            Map<String, Object> map = (Map<String, Object>) o;
            String elementName = String.valueOf(map.get("name"));
            Element checkElement = FormUtil.findElement(elementName, rootForm, formData, true);
            String checkValue = FormUtil.getRequestParameter(checkElement, formData);

            Set<String> checkedValues = Optional.ofNullable(checkValue)
                    .map(s -> s.split(";"))
                    .map(Arrays::stream)
                    .orElseGet(Stream::empty)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());

            boolean alreadyAssigned = checkedValues
                    .stream()
                    .anyMatch(values::contains);

            if(alreadyAssigned) {
                String elementLabel = Optional.ofNullable(checkElement.getPropertyString("label"))
                        .filter(s -> !s.isEmpty())
                        .orElse(elementName);

                String message = Optional.ofNullable(getPropertyString("message"))
                        .filter(s -> !s.isEmpty())
                        .map(s -> AppUtil.processHashVariable(s, null, null, null))
                        .orElse("Value already assigned to field " + elementLabel);

                formData.addFormError(elementId, message);

                return false;
            }

            values.addAll(checkedValues);
        }

        return true;
    }

    protected boolean validateMandatory(FormData data, String id, String label, String[] values, String message) {
        boolean result = true;
        if (message == null || message.isEmpty()) {
            message = ResourceBundleUtil.getMessage("form.defaultvalidator.err.missingValue");
        }

        if (values == null || values.length == 0) {
            result = false;
            if (id != null) {
                data.addFormError(id, message);
            }
        } else {
            for (String val : values) {
                if (val == null || val.trim().length() == 0) {
                    result = false;
                    data.addFormError(id, message);
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public String getElementDecoration() {
        String decoration = "";
        @SuppressWarnings("unused")
        String type = (String) getProperty("type");
        String mandatory = (String) getProperty("mandatory");
        if ("true".equals(mandatory)) {
            decoration += " * ";
        }
        if (decoration.trim().length() > 0) {
            decoration = decoration.trim();
        }
        return decoration;
    }

    @Override
    public String getName() {
        return "Multivalue Multi Field Validator";
    }

    @Override
    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    @Override
    public String getDescription() {
        return getClass().getPackage().getImplementationTitle();
    }

    @Override
    public String getLabel() {
        return getName();
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/MultiFieldValidator.json", null, false, "/messages/MultiFieldValidator");
    }
}
