package com.kinnarasttudio.kecakplugins.validators;

import com.kinnarastudio.commons.Try;
import org.apache.tika.Tika;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FileDownloadSecurity;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormValidator;
import org.joget.apps.form.service.FileUtil;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.FileManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.plugin.base.PluginManager;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * File Type Validator
 */
public class FileTypeValidator extends FormValidator {
    public final static String LABEL = "File Type Validator";

    @Override
    public String getElementDecoration() {
        return isMandatory() ? "*" : "";
    }

    @Override
    public boolean validate(Element element, FormData formData, String[] values) {
        final String elementName = FormUtil.getElementParameterName(element);
        if(!(element instanceof FileDownloadSecurity)) {
            formData.addFileError(elementName, "Field is not a File Download Security");
            return false;
        }

        if(isMandatory()) {
            boolean isEmpty = Optional.ofNullable(values)
                    .map(Arrays::stream)
                    .orElseGet(Stream::empty)
                    .filter(Objects::nonNull)
                    .allMatch(String::isEmpty);

            if(isEmpty) {
                formData.addFormError(elementName, ResourceBundleUtil.getMessage("form.defaultvalidator.err.missingValue"));
                return false;
            }
        }

        final Set<String> includes = getMimeProperty("includes");
        final Set<String> excludes = getMimeProperty("excludes");

        final String customErrorMessage = getPropertyString("errorMessage").trim();
        Optional.ofNullable(values)
                .map(Arrays::stream)
                .orElseGet(Stream::empty)
                .filter(Objects::nonNull)
                .map(Try.onFunction(path -> {
                    File file = FileManager.getFileByPath(path);
                    if(file == null) {
                        // not a temporary file
                        file = FileUtil.getFile(path, element, formData.getPrimaryKeyValue());
                    }

                    return file;
                }))
                .filter(Objects::nonNull)
                .forEach(file -> Stream.of(file)
                        .map(Try.onFunction(f -> new Tika().detect(f)))
                        .filter(Objects::nonNull)
                        .map(this::cleanUpMimeType)
                        .forEach(s -> {
                            boolean result = (includes.isEmpty() || includes.stream().anyMatch(s::equalsIgnoreCase)) && (excludes.isEmpty() || excludes.stream().noneMatch(s::equalsIgnoreCase));
                            if (!result) {
                                LogUtil.warn(getClassName(), "Element ["+ elementName + "] : Invalid mime type [" + s + "] for file [" + file.getName()+"]");
                                if(customErrorMessage.isEmpty()) {
                                    formData.addFileError(elementName, "Invalid type for file " + file.getName());
                                } else {
                                    formData.addFileError(elementName, file.getName() + " " + customErrorMessage);
                                }
                            }
                        }));

        return !formData.getFileErrors().containsKey(elementName);
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
        return AppUtil.readPluginResource(getClassName(), "/properties/FileTypeValidator.json", null, false, "/messages/FileTypeValidator");
    }

    protected Set<String> getMimeProperty(String propertyName) {
        return Optional.ofNullable(getProperty(propertyName))
                .map(o -> (Object[])o)
                .map(Arrays::stream)
                .orElseGet(Stream::empty)
                .map(o -> (Map<String, Object>)o)
                .map(m -> m.getOrDefault("mimeType", ""))
                .map(String::valueOf)
                .map(this::cleanUpMimeType)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    protected boolean isMandatory() {
        return "true".equalsIgnoreCase(getPropertyString("mandatory"));
    }

    protected String cleanUpMimeType(String mimeType) {
        return mimeType.replaceAll(";.*", "").trim();
    }
}
