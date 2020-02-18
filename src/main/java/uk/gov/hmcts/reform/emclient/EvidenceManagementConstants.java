package uk.gov.hmcts.reform.emclient;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Period;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EvidenceManagementConstants {

    public static final String LINE_SEPARATOR = System.lineSeparator();
    public static final String SPACE_SEPARATOR = " ";
    public static final String EMPTY_STRING = "";

    // maybe move to Test Constants file?
    public static final String MIME_TYPE = "mimeType";

}
