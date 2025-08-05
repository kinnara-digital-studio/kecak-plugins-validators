package com.kinnarastudio.kecakplugins.form.validator;

import com.kinnarastudio.commons.Try;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.lib.DatePicker;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormValidator;
import org.joget.commons.util.SetupManager;
import org.joget.plugin.base.PluginManager;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.context.i18n.LocaleContextHolder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Predicate;

public class DateTimeValidator extends FormValidator {
    public final static String LABEL = "Date Time Validator";

    @Override
    public boolean validate(Element element, FormData formData, String[] values) {
        final String elementId = element.getPropertyString("id");
        final DateFormat dateFormat = getDateFormat();
        final Date dateValue = Arrays.stream(values)
                .filter(Predicate.not(String::isEmpty))
                .findFirst()
                .map(Try.onFunction(source -> {
                    if (element instanceof DatePicker) {
                        final String elementFormat = getDatePickerFormat((DatePicker) element);
                        final DateFormat inputFormat = new SimpleDateFormat(getJavaDateFormat(elementFormat));
                        return inputFormat.parse(source);
                    } else {
                        return dateFormat.parse(source);
                    }
                }))
                .orElse(null);

        if (dateValue == null) {
            formData.addFormError(elementId, "Invalid date");
            return false;
        }

        boolean valid;
        switch (getOperator()) {
            case "exact":
                valid = getDateFrom().equals(dateValue);
                break;

            case "after":
                valid = dateValue.after(getDateFrom());
                break;

            case "before":
                valid = dateValue.before(getDateTo());
                break;

            case "between":
                valid = dateValue.after(getDateFrom()) && dateValue.before(getDateTo());
                break;

            default:
                valid = true;
        }

        if (!valid) {
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
        return AppUtil.readPluginResource(getClassName(), "/properties/DateTimeValidator.json", null, true, "/messages/DateTimeValidator");
    }

    protected DateFormat getDateFormat() {
        return new SimpleDateFormat(getPropertyString("dateFormat"));
    }

    protected Date getDateFrom() {
        return Optional.of(getPropertyString("dateFrom"))
                .filter(Predicate.not(String::isEmpty))
                .map(Try.onFunction(getDateFormat()::parse))
                .orElseGet(Date::new);
    }

    protected Date getDateTo() {
        return Optional.of(getPropertyString("dateTo"))
                .filter(Predicate.not(String::isEmpty))
                .map(Try.onFunction(getDateFormat()::parse))
                .orElseGet(Date::new);
    }

    protected String getOperator() {
        return getPropertyString("operator");
    }

    protected String getErrorMessage() {
        return "Invalid Date";
    }

    @Override
    public String getElementDecoration() {
        return "*";
    }

    /**
     * Copy from {@link DatePicker#getJavaDateFormat(String)}
     *
     * @param javascriptFormat
     * @return
     */
    protected String getJavaDateFormat(String javascriptFormat) {
        if (javascriptFormat.contains("DD")) {
            javascriptFormat = javascriptFormat.replaceAll("DD", "EEEE");
        } else {
            javascriptFormat = javascriptFormat.replaceAll("D", "EEE");
        }

        if (javascriptFormat.contains("MM")) {
            javascriptFormat = javascriptFormat.replaceAll("MM", "MMMMM");
        } else {
            javascriptFormat = javascriptFormat.replaceAll("M", "MMM");
        }

        if (javascriptFormat.contains("mm")) {
            javascriptFormat = javascriptFormat.replaceAll("mm", "MM");
        } else {
            javascriptFormat = javascriptFormat.replaceAll("m", "M");
        }

        if (javascriptFormat.contains("yy")) {
            javascriptFormat = javascriptFormat.replaceAll("yy", "yyyy");
        } else {
            javascriptFormat = javascriptFormat.replaceAll("y", "yy");
        }

        if (javascriptFormat.contains("tt") || javascriptFormat.contains("TT")) {
            javascriptFormat = javascriptFormat.replaceAll("tt", "a");
            javascriptFormat = javascriptFormat.replaceAll("TT", "a");
        }

        return javascriptFormat;
    }

    /**
     * Copy from {@link DatePicker#getFormat()}
     *
     * @return
     */
    protected String getDatePickerFormat(DatePicker datePicker) {
        String format = datePicker.getPropertyString("format");
        if (format.isEmpty()) {
            Locale locale = LocaleContextHolder.getLocale();
            if (locale != null && locale.toString().startsWith("zh")) {
                WorkflowUtil.getHttpServletRequest().setAttribute("currentLocale", locale);
                format = "yy-mm-dd";
            } else {
                SetupManager setupManager = (SetupManager) AppUtil.getApplicationContext().getBean("setupManager");
                if ("true".equalsIgnoreCase(setupManager.getSettingValue("dateFormatFollowLocale"))) {
                    DateFormat dateInstance = DateFormat.getDateInstance(DateFormat.SHORT, locale);
                    if (dateInstance instanceof SimpleDateFormat) {
                        format = ((SimpleDateFormat) dateInstance).toPattern();
                        format = format.replaceAll("MM", "M");
                        format = format.replaceAll("M", "mm");
                        format = format.replaceAll("dd", "d");
                        format = format.replaceAll("d", "dd");
                        format = format.replaceAll("YYYY", "yy");
                    }
                }
            }

            if (format.isEmpty()) {
                format = "mm/dd/yy";
            }
        }
        return format;
    }
}
