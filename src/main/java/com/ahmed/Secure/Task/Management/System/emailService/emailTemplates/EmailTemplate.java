package com.ahmed.Secure.Task.Management.System.emailService.emailTemplates;

import java.util.Map;

public interface EmailTemplate {

    String to();

    String subject();

    String templateName();

    Map<String, Object> templateVariables ();

}
