package com.kinnarastudio.kecakplugins.form.validator;

import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormValidator;
import org.joget.plugin.base.PluginManager;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ValuesMatcherValidator extends FormValidator {
    public final static String LABEL = "Values Matcher Validator";

    @Override
    public boolean validate(Element element, FormData formData, String[] elementValues) {
        final Collection<String> values = ifEmpty(getValues(), () -> List.of(elementValues));
        final Collection<String> withValues = getWithValues();

        final boolean valid;
        if(isAllMatch()) {
            valid = values.containsAll(withValues);
        }else if (isAnyMatch()) {
            valid = withValues.stream().anyMatch(values::contains);
        } else if(isNoneMatch()) {
            valid = withValues.stream().noneMatch(values::contains);
        } else if(isRegexMatch()) {
            final String pattern = getRegexPattern();
            valid = values.stream().allMatch(s -> s.matches(pattern));
        } else {
            valid = false;
        }

        if(!valid) {
            final String elementId = element.getPropertyString("id");
            formData.addFormError(elementId, getErrorMessage());
        }

        return valid;
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
        return AppUtil.readPluginResource(getClassName(), "/properties/ValuesMatcherValidator.json", null, true, "/messages/ValuesMatcherValidator");
    }

    protected Collection<String> getValues() {
        return Optional.of("values")
                .map(this::getPropertyString)
                .filter(Predicate.not(String::isEmpty))
                .map(s -> s.split(";"))
                .stream()
                .flatMap(Arrays::stream)
                .collect(Collectors.toSet());
    }

    protected Collection<String> getWithValues() {
        return Optional.of("withValues")
                .map(this::getPropertyString)
                .filter(Predicate.not(String::isEmpty))
                .map(s -> s.split(";"))
                .stream()
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());
    }

    protected String getMatchType() {
        return getPropertyString("matchType");
    }

    protected boolean isAllMatch() {
        return "all".equalsIgnoreCase(getMatchType());
    }

    protected boolean isAnyMatch() {
        return "any".equalsIgnoreCase(getMatchType());
    }

    protected boolean isNoneMatch() {
        return "none".equalsIgnoreCase(getMatchType());
    }

    protected boolean isRegexMatch() {
        return "regex".equalsIgnoreCase(getMatchType());
    }

    protected String getRegexPattern() {
        return getPropertyString("withValues");
    }

    protected String getErrorMessage() {
        return getPropertyString("errorMessage");
    }

    protected <T> T[] ifEmpty(T[] values, Supplier<T[]> ifEmpty) {
        return values.length == 0 ? ifEmpty.get() : values;
    }

    protected <T, V extends Collection<T>> V ifEmpty(V values, Supplier<V> ifEmpty) {
        return values.isEmpty() ? ifEmpty.get() : values;
    }
}
