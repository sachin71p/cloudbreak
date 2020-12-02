package com.sequenceiq.cloudbreak.template;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TemplatePreparationObjectTest {

    private static final String SSL_CERTS_FILE_PATH = "/foo/bar.pem";

    @Test
    void getRdsSslCertificateFileTestWhenFileAbsent() {
        TemplatePreparationObject tpo = new TemplatePreparationObject.Builder()
                .build();

        assertThat(tpo.getRdsSslCertificateFile()).isNull();
    }

    @Test
    void getRdsSslCertificateFileTestWhenFilePresent() {
        TemplatePreparationObject tpo = new TemplatePreparationObject.Builder()
                .withRdsSslCertificateFile(SSL_CERTS_FILE_PATH)
                .build();

        assertThat(tpo.getRdsSslCertificateFile()).isEqualTo(SSL_CERTS_FILE_PATH);
    }

}