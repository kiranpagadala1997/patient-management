package org.pm.patientservice.exception;

import java.util.UUID;

public class PatientNotFoundException extends RuntimeException{
    public PatientNotFoundException(String s) {
        super(s);
    }
}
